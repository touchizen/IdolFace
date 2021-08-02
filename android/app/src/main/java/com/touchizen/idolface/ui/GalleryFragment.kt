/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.touchizen.idolface.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.google.mlkit.vision.face.Face
import com.touchizen.idolface.BuildConfig
import com.touchizen.idolface.MainActivity
import com.touchizen.idolface.R
import com.touchizen.idolface.env.Logger
import com.touchizen.idolface.facedetector.FaceDetectorProcessor
import com.touchizen.idolface.facedetector.GraphicOverlay
import com.touchizen.idolface.facedetector.PreferenceUtils
import com.touchizen.idolface.facedetector.VisionImageProcessor
import com.touchizen.idolface.tflite.Classifier
import com.touchizen.idolface.utils.*
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

val EXTENSION_WHITELIST = arrayOf("JPG")

/** Fragment used to present the user with a gallery of photos taken */

@AndroidEntryPoint
class GalleryFragment internal constructor() : Fragment() {

    /** AndroidX navigation arguments */
    private val args: GalleryFragmentArgs by navArgs()
    public lateinit var mediaList: MutableList<File>
    public lateinit var mediaViewPager: ViewPager

    public val isFragmentCreated = MutableLiveData(false)
    public val mapResults: HashMap<Int, List<Classifier.Recognition>> = HashMap()
    public val mapTimes: HashMap<Int, Long> = HashMap()

    /** Adapter class used to present a fragment containing one photo or video as a page */
    inner class MediaPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = mediaList.size
        override fun getItem(position: Int): Fragment = PhotoFragment.create(mediaList[position], position)
        override fun getItemPosition(obj: Any): Int = POSITION_NONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mark this as a retain fragment, so the lifecycle does not get restarted on config change
        retainInstance = true

        // Get root directory of media from navigation arguments
        val rootDirectory = File(args.rootDirectory)

        // Walk through all files in the root directory
        // We reverse the order of the list to present the last photos first
        mediaList = rootDirectory.listFiles { file ->
            EXTENSION_WHITELIST.contains(file.extension.toUpperCase(Locale.ROOT))
        }?.sortedDescending()?.toMutableList() ?: mutableListOf()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_gallery, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity?)!!.setDrawerLockedClosed()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()

        //Checking media files list
        if (mediaList.isEmpty()) {
            view.findViewById<ImageButton>(R.id.delete_button).isEnabled = false
            view.findViewById<ImageButton>(R.id.share_button).isEnabled = false
        }
        // Populate the ViewPager and implement a cache of two media items
        mediaViewPager = view.findViewById<ViewPager>(R.id.photo_view_pager).apply {
            offscreenPageLimit = 2
            adapter = MediaPagerAdapter(childFragmentManager)
        }

        mediaViewPager.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                sendResultsToBottomSheet(position)
            }
        })



        // Make sure that the cutout "safe area" avoids the screen notch if any
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Use extension method to pad "inside" view containing UI using display cutout's bounds
            view.findViewById<ConstraintLayout>(R.id.cutout_safe_area).padWithDisplayCutout()
        }

        // Handle back button press
        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            (activity as MainActivity?)!!.setDrawerUnlocked()
            (activity as AppCompatActivity?)!!.supportActionBar!!.show()
            if (args.rootDirectory.isNotEmpty()) {
                Navigation.findNavController(requireActivity(), R.id.fragment_container)
                    .navigateUp()
            }
        }

        // Handle share button press
        view.findViewById<ImageButton>(R.id.share_button).setOnClickListener {

            mediaList.getOrNull(mediaViewPager.currentItem)?.let { mediaFile ->
                ShareUtils.shareScreenshot(requireActivity() as AppCompatActivity,this)
            }
        }

        // Handle delete button press
        view.findViewById<ImageButton>(R.id.delete_button).setOnClickListener {

            mediaList.getOrNull(mediaViewPager.currentItem)?.let { mediaFile ->

                AlertDialog.Builder(view.context, android.R.style.Theme_Material_Dialog)
                        .setTitle(getString(R.string.delete_title))
                        .setMessage(getString(R.string.delete_dialog))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes) { _, _ ->

                            // Delete current photo
                            mediaFile.delete()

                            // Send relevant broadcast to notify other apps of deletion
                            MediaScannerConnection.scanFile(
                                    view.context, arrayOf(mediaFile.absolutePath), null, null)

                            // Notify our view pager
                            mediaList.removeAt(mediaViewPager.currentItem)
                            mediaViewPager.adapter?.notifyDataSetChanged()

                            // If all photos have been deleted, return to camera
                            if (mediaList.isEmpty()) {
                                (activity as MainActivity?)!!.setDrawerUnlocked()
                                (activity as AppCompatActivity?)!!.supportActionBar!!.show()
                                Navigation.findNavController(requireActivity(), R.id.fragment_container).navigateUp()
                            }
                        }
                        .setNegativeButton(android.R.string.no, null)
                        .create().showImmersive()
            }
        }

        isFragmentCreated.observe(viewLifecycleOwner, { isCreated ->
            if (isCreated) {
                sendResultsToBottomSheet(mediaViewPager.currentItem)
            }
        })
    }

    fun sendResultsToBottomSheet(position:Int) {
        if (mapResults.get(position) != null) {
            (requireActivity() as MainActivity).showResultsInBottomSheet(
                mapResults.get(position)
            )
        }
        else {

        }

        if (mapTimes.get(position) != null) {
            (requireActivity() as MainActivity).showInference(
                mapTimes.get(position).toString() + "ms"
            )
        }
    }

    companion object {

        private const val TAG = "GalleryFragment"
        const val RESIZED_WIDTH = 640
        const val RESIZED_HEIGHT = 480
        const val IMAGE_URI = "image_uri"

    }
}
