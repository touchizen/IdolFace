package com.touchizen.idolface.ui.myidolprofile

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.angads25.toggle.interfaces.OnToggledListener
import com.github.angads25.toggle.model.ToggleableView
import com.google.firebase.firestore.CollectionReference
import com.touchizen.idolface.MainActivity
import com.touchizen.idolface.R
import com.touchizen.idolface.databinding.FragmentMyIdolProfileBinding
import com.touchizen.idolface.model.IdolImage
import com.touchizen.idolface.model.IdolProfile
import com.touchizen.idolface.model.UserStatus
import com.touchizen.idolface.ui.CustomProgressView
import com.touchizen.idolface.utils.*
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@AndroidEntryPoint
class MyIdolProfileFragment : Fragment(), OnToggledListener {

    private lateinit var binding: FragmentMyIdolProfileBinding

    private lateinit var context: Activity

    @Inject
    lateinit var preference: MPreference

    @Inject
    lateinit var userCollection: CollectionReference

    private var progressView: CustomProgressView? = null

    private val viewModel: MyIdolProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.getParcelable<IdolProfile>(NavigationHelper.IDOLPROFILE)?.let {
            viewModel.idolProfile.value = it
            viewModel.idolProfile.value!!.oldName = viewModel.idolProfile.value!!.name
        }
        arguments?.getParcelable<IdolImage>(NavigationHelper.IDOLIMAGE)?.let {
            viewModel.idolProfile.value!!.image = it.imageUrl
        }

        binding = FragmentMyIdolProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context = requireActivity()
        UserUtils.updatePushToken(context, userCollection, true)
        EventBus.getDefault().post(UserStatus())
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        progressView = CustomProgressView(context)
        binding.btnSaveChanges.setOnClickListener {
            onSaveChanges()
        }
        binding.backButton.setOnClickListener{
            onBackButton()
        }
        binding.genderSwitch.setOnToggledListener(this)
        binding.genderSwitch.isOn = viewModel.idolProfile.value!!.gender

        if (!viewModel.idolProfile.value!!.image.isNullOrBlank()) {
            // Displayed and saved to cache image, as needs for post detail.
            Glide.with(requireActivity())
                .load(viewModel.idolProfile.value!!.image)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.imgProPic)
        }

        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.progressProPic.observe(viewLifecycleOwner, { uploaded ->
            binding.progressPro.toggle(uploaded)
        })

        viewModel.profileUpdateState.observe(viewLifecycleOwner, {
            when (it) {
                is LoadState.OnSuccess -> {
                    if (findNavController().isValidDestination(R.id.myidolprofile_fragment)) {
                        progressView?.dismiss()
                        onBackButton()
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


    fun onSaveChanges() {

        val newName = viewModel.idolProfile.value!!.name
        val company = viewModel.idolProfile.value!!.company

        when {
            viewModel.isUploading.value!! -> context.toast("Profile picture is uploading!")
            newName.isNullOrBlank() -> context.toast("Idol name can't be empty!")
            company.isNullOrBlank() -> context.toast("company can't be empty!")
            else -> {
                context.window.decorView.clearFocus()
                viewModel.storeProfileData()
            }
        }
    }

    fun onBackButton() {
        NavigationHelper.navigateUp(requireActivity())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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

    override fun onSwitched(toggleableView: ToggleableView?, isOn: Boolean) {
        viewModel.idolProfile.value!!.gender = isOn
    }
}