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

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.mlkit.vision.face.Face
import com.mikhaellopez.circularimageview.CircularImageView
import com.touchizen.idolface.ClassifierActivity
import com.touchizen.idolface.MainActivity
import com.touchizen.idolface.R
import com.touchizen.idolface.databinding.FragmentPhotoBinding
import com.touchizen.idolface.facedetector.FaceDetectorProcessor
import com.touchizen.idolface.facedetector.GraphicOverlay
import com.touchizen.idolface.facedetector.PreferenceUtils
import com.touchizen.idolface.facedetector.VisionImageProcessor
import com.touchizen.idolface.tflite.Classifier
import com.touchizen.idolface.utils.BitmapUtils
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.lang.String
import java.util.*


/** Fragment used for each individual page showing a photo inside of [GalleryFragment] */
@AndroidEntryPoint
open class PhotoFragment internal constructor() :
    Fragment()
    ,RequestListener<Drawable>
    ,FaceDetectorProcessor.OnFaceDetectListener, ClassifierActivity.OnClassifierListener {
    private lateinit var binding: FragmentPhotoBinding
    protected lateinit var fullImage: ImageView
    protected lateinit var idolImage: CircularImageView
    private lateinit var faceImage: CircularImageView
    protected lateinit var txtDesc: TextView

    private lateinit var graphicOverlay: GraphicOverlay
    private lateinit var imageProcessor: VisionImageProcessor
    private lateinit var recognitions: List<Classifier.Recognition>
    private lateinit var originalBitmap: Bitmap
    private var inferenceTimeMs: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhotoBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fullImage = binding.photoImage
        idolImage = binding.idolImage
        faceImage = binding.faceImage
        txtDesc = binding.txtDesc
        graphicOverlay = binding.graphicOverlay

        fullImage.scaleType = ImageView.ScaleType.CENTER

        onImageLoadFromArguments()
    }

    open fun onImageLoadFromArguments() {
        val args = arguments ?: return
        val resource = args.getString(FILE_NAME_KEY)?.let { File(it) } ?: R.drawable.ic_photo
        Glide.with(this)
            .load(resource)
            .listener(this)
            .into(fullImage)
    }

    /**
     * The events of Glide for loading images.
     */

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean
    ): Boolean {
        return false
    }

    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {

        val bitmap = resource?.toBitmap()
        if (bitmap != null) {
            processImage(bitmap)
        }

        return false
    }

    override fun onResume() {
        super.onResume()

        if (!::imageProcessor.isInitialized) {
            createImageProcessor()
        }

        if (::recognitions.isInitialized) {
            val activity = requireActivity() as MainActivity
            activity.showResultsInBottomSheet(recognitions)
            activity.showInference(inferenceTimeMs.toString() + "ms")
        }

    }
    public override fun onPause() {
        super.onPause()
        imageProcessor.run {
            this.stop()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()

        if (!::imageProcessor.isInitialized) {
            createImageProcessor()
        }
        imageProcessor.run {
            this.stop()
        }
    }

    private fun createImageProcessor() {

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val displayMode = sharedPreferences.getString(
            requireContext().getString(R.string.pref_key_face_detection_display_mode),
            "2"
        )

        val isFaceVisible = displayMode != "1"

        try {
            val faceDetectorOptions = PreferenceUtils.getFaceDetectorOptions(requireContext())
            imageProcessor = FaceDetectorProcessor(requireContext(), faceDetectorOptions)
            (imageProcessor as FaceDetectorProcessor).setOnFaceDetectListener(this)
            (imageProcessor as FaceDetectorProcessor).setFaceVisible(isFaceVisible)
        } catch (e: Exception) {
            Toast.makeText(
                requireActivity(),
                "Can not create image processor: " + e.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showFaceDetectOveray(bitmap: Bitmap, rotationDegrees:Int) {

        originalBitmap = bitmap

        //val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
        if (rotationDegrees == 0 || rotationDegrees == 180) {
            graphicOverlay.setImageSourceInfo(bitmap.width, bitmap.height, false)
        } else {
            graphicOverlay.setImageSourceInfo(bitmap.height, bitmap.width, false)
        }

        try {
            // Clear the overlay first
            //graphicOverlay!!.clear()

            imageProcessor.processBitmap(bitmap , graphicOverlay)
        } catch (e: IOException) {
            Log.e(TAG,"Error retrieving saved image")
        }

    }

    fun sendNoFaceMessage() {

        val unknownFaces : ArrayList<Classifier.Recognition> = ArrayList<Classifier.Recognition>();

        unknownFaces.add(Classifier.Recognition("0",getString(R.string.no_face),1.0f,null) )
        unknownFaces.add(Classifier.Recognition("1",getString(R.string.no_face),1.0f,null) )
        unknownFaces.add(Classifier.Recognition("2",getString(R.string.no_face),1.0f,null) )

        //unknownFaces.ap
        onRecognitionReceived(unknownFaces,100)
    }

    override fun onFaceAvailable(faces: List<Face>, graphicOverlay: GraphicOverlay) {
        if (faces.size == 0) {
            sendNoFaceMessage()
            return
        }

        if ((activity as MainActivity).isProcessingFrame) {
            return
        }

        (activity as MainActivity).isProcessingFrame = true

        for (face in faces) {

            val left = face.boundingBox.left
            val right = face.boundingBox.right
            val top = face.boundingBox.top
            val bottom = face.boundingBox.bottom

            val width = right - left
            val height = bottom - top

            if (left < 0 || top < 0 ||
                left + width > originalBitmap.width ||
                top + height> originalBitmap.height) {
                (activity as MainActivity).isProcessingFrame = false
                return
            }

            val croppedBmp = Bitmap.createBitmap(originalBitmap,
                left, top,
                width, height
            )

            faceImage.setImageBitmap(croppedBmp)

            (activity as MainActivity?)!!.processImage(
                croppedBmp,
                croppedBmp.width,
                croppedBmp.height,
                0,
                this
            )

            break
        }
    }

    private fun processImage(bitmap: Bitmap) {
        if (!::imageProcessor.isInitialized) {
            createImageProcessor()
        }

        showFaceDetectOveray(bitmap, 0)
    }

    override fun onRecognitionReceived(
        results: List<Classifier.Recognition>,
        inferenceTimeMs: Long
    ) {
        this.recognitions = results
        this.inferenceTimeMs = inferenceTimeMs

        txtDesc.text = "[" + resources.getText(R.string.app_name).toString() + "] " +
                results[0].title +
                " --- " +
                String.format("%.2f", 100 * results[0].confidence).toString() + "%"
    }

    companion object {
        private const val FILE_NAME_KEY = "file_name"
        private const val TAG = "PhotoFragment"
        const val RESIZED_WIDTH = 640
        const val RESIZED_HEIGHT = 480
        fun create(image: File) = PhotoFragment().apply {
            arguments = Bundle().apply {
                putString(FILE_NAME_KEY, image.absolutePath)
            }
        }
    }
}