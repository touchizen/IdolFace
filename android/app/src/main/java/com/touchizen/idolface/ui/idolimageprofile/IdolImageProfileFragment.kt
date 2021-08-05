package com.touchizen.idolface.ui.idolimageprofile

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.touchizen.idolface.MainActivity
import com.touchizen.idolface.R
import com.touchizen.idolface.databinding.FragmentIdolImageProfileBinding
import com.touchizen.idolface.model.IdolImage
import com.touchizen.idolface.model.IdolProfile
import com.touchizen.idolface.ui.CustomProgressView
import com.touchizen.idolface.utils.*
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@AndroidEntryPoint
class IdolImageProfileFragment : Fragment() {

    private var instance : IdolImageProfileFragment? = null
    private lateinit var binding: FragmentIdolImageProfileBinding
    private var preference: MPreference? = null

    private lateinit var context: Activity
    private lateinit var fab: FloatingActionButton

    private var progressView: CustomProgressView? = null

    private val viewModel: IdolImageProfileViewModel by viewModels()

    fun getInstance() : IdolImageProfileFragment {
        if (instance == null) {
            instance = IdolImageProfileFragment()
        }
        return instance as IdolImageProfileFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initPreference()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        arguments?.getParcelable<IdolImage>(NavigationHelper.IDOLIMAGE)?.let {
            viewModel.idolImage.value = it
        }
        arguments?.getParcelable<IdolProfile>(NavigationHelper.IDOLPROFILE)?.let {
            viewModel.idolProfile.value = it
        }

        // Inflate the layout for this fragment
        binding = FragmentIdolImageProfileBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context = requireActivity()
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        progressView = CustomProgressView(context)
//        binding.fab.setOnClickListener{
//            onSaveButton()
//        }

        initView()

        //subscribeObservers()
    }

    private fun initView() {
        // Displayed and saved to cache image, as needs for post detail.

        var idolImage = viewModel.idolImage.value
        var idolProfile = viewModel.idolProfile.value

        Glide.with(requireActivity())
            .load(idolImage?.imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.ic_error)
            .into(binding.idolImage)

        binding.backButton.setOnClickListener {
            onBackButton()
        }
        binding.reportButton.setOnClickListener {
            onReportButton()
        }
    }
    private fun subscribeObservers() {

//        viewModel.imageUpdateState.observe(viewLifecycleOwner, {
//            when (it) {
//                is LoadState.OnSuccess -> {
//                    if (findNavController().isValidDestination(R.id.idolgallery_fragment)) {
//                        progressView?.dismiss()
//                        mAdapter.refresh()
//                    }
//                }
//                is LoadState.OnFailure -> {
//                    progressView?.dismiss()
//                }
//                is LoadState.OnLoading -> {
//                    progressView?.show()
//                }
//                else -> {}
//            }
//        })
//
//        viewModel.checkIdolImageState.observe(viewLifecycleOwner,{
//            when (it) {
//                is LoadState.OnSuccess -> {
//                    progressView?.dismiss()
//                }
//                is LoadState.OnFailure -> {
//                    progressView?.dismiss()
//                }
//                is LoadState.OnLoading -> {
//                    progressView?.show()
//                }
//                else -> {}
//            }
//        })
    }


    private fun initPreference() {
        if (preference == null) {
            preference = MPreference(requireContext())
        }
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).setDrawerLockedClosed()
        (requireActivity() as MainActivity).supportActionBar!!.hide()
        (requireActivity() as MainActivity).setBottomsheetHidden(true)
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).setDrawerUnlocked()
        (requireActivity() as MainActivity).supportActionBar!!.show()
        (requireActivity() as MainActivity).setBottomsheetHidden(false)
    }

    fun onBackButton() {
        NavigationHelper.navigateUp(requireActivity())
    }

    fun onReportButton() {

        NavigationHelper.openMyIdolProfile(requireActivity(),
            viewModel.idolProfile.value,
            viewModel.idolImage.value
        )
    }

    fun onSaveButton() {
        var idolImage = viewModel.idolImage.value

        Glide.with(this)
            .asBitmap()
            .load(idolImage!!.imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                    saveBitmap(bitmap)
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    @Throws(IOException::class)
    fun saveBitmap(bitmap: Bitmap): Uri {

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, ""+ System.currentTimeMillis()+".jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        }

        val resolver = context.contentResolver
        var uri: Uri? = null

        try {
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Failed to create new MediaStore record.")

            resolver.openOutputStream(uri)?.use {
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it))
                    throw IOException("Failed to save bitmap.")
            } ?: throw IOException("Failed to open output stream.")

            return uri

        } catch (e: IOException) {

            uri?.let { orphanUri ->
                // Don't leave an orphan entry in the MediaStore
                resolver.delete(orphanUri, null, null)
            }

            throw e
        }
    }
}