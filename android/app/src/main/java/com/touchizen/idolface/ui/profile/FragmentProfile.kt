package com.touchizen.idolface.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.CollectionReference
import com.theartofdev.edmodo.cropper.CropImage
import com.touchizen.idolface.MainActivity
import com.touchizen.idolface.R
import com.touchizen.idolface.databinding.FragmentProfileBinding
import com.touchizen.idolface.model.UserStatus
import com.touchizen.idolface.ui.CustomProgressView
import com.touchizen.idolface.utils.*
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@AndroidEntryPoint
class FragmentProfile : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var context: Activity

    @Inject
    lateinit var preference: MPreference

    @Inject
    lateinit var userCollection: CollectionReference

    private var progressView: CustomProgressView? = null

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context = requireActivity()
        UserUtils.updatePushToken(context,userCollection,true)
        EventBus.getDefault().post(UserStatus())
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        progressView = CustomProgressView(context)
        binding.imgProPic.setOnClickListener { ImageUtils.askPermission(this) }
        binding.fab.setOnClickListener { validate() }
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.progressProPic.observe(viewLifecycleOwner, { uploaded ->
            binding.progressPro.toggle(uploaded)
        })

        viewModel.profileUpdateState.observe(viewLifecycleOwner, {
            when (it) {
                is LoadState.OnSuccess -> {
                    if (findNavController().isValidDestination(R.id.FProfile)) {
                        progressView?.dismiss()
                        val preference = MPreference(requireActivity().baseContext)
                        if (preference.getFragmentAfterLogin() == NavigationHelper.CAMERA) {
                            findNavController().navigate(R.id.action_to_camera_fragment)
                        }
                        else if (preference.getFragmentAfterLogin() == NavigationHelper.IDOLPROFILE) {
                            val idolProfile = preference.getIdolProfile()
                            NavigationHelper.openIdolProfile(requireActivity(),idolProfile)
                        }
                        else if (preference.getFragmentAfterLogin() == NavigationHelper.IDOLGALLERY) {
                            val idolProfile = preference.getIdolProfile()
                            NavigationHelper.openIdolGalllery(requireActivity(),idolProfile)
                        }
                        else if (preference.getFragmentAfterLogin() == NavigationHelper.IDOLIMAGE) {
                            val idolImage = preference.getIdolImage()
                            val idolProfile = preference.getIdolProfile()
                            NavigationHelper.openIdolImageProfile(
                                requireActivity(), idolImage, idolProfile
                            )
                        }
                        else if (preference.getFragmentAfterLogin() == NavigationHelper.MYIDOLPROFILE) {
                            val idolImage = preference.getIdolImage()
                            val idolProfile = preference.getIdolProfile()
                            NavigationHelper.openMyIdolProfile(
                                requireActivity(), idolProfile, idolImage
                            )
                        }
                        else {
                            findNavController().navigate(R.id.action_to_idol_fragment)
                        }
                    }
                }
                is LoadState.OnFailure -> {
                    progressView?.dismiss()
                }
                is LoadState.OnLoading -> {
                    progressView?.show()
                }
                else -> {}
            }
        })

        viewModel.checkUserNameState.observe(viewLifecycleOwner,{
            when (it) {
                is LoadState.OnFailure -> {
                    progressView?.dismiss()
                }
                is LoadState.OnLoading -> {
                    progressView?.show()
                }
                else -> {}
            }
        })
    }

    private fun validate() {
        val name = viewModel.name.value
        if (!name.isNullOrEmpty() && name.length > 1 && !viewModel.progressProPic.value!!)
            viewModel.storeProfileData()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
            onCropResult(data)
        else
            ImageUtils.cropImage(context, data, true)
    }

    private fun onCropResult(data: Intent?) {
        try {
            val imagePath: Uri? = ImageUtils.getCroppedImage(data)
            imagePath?.let {
                viewModel.uploadProfileImage(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ImageUtils.onImagePerResult(this, *grantResults)
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

    override fun onDestroy() {
        try {
            progressView?.dismissIfShowing()
            super.onDestroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}