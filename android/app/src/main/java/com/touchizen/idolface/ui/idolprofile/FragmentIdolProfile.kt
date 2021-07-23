package com.touchizen.idolface.ui.idolprofile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.angads25.toggle.interfaces.OnToggledListener
import com.github.angads25.toggle.model.ToggleableView
import com.google.firebase.firestore.CollectionReference
import com.theartofdev.edmodo.cropper.CropImage
import com.touchizen.idolface.MainActivity
import com.touchizen.idolface.R
import com.touchizen.idolface.databinding.FragmentIdolProfileBinding
import com.touchizen.idolface.model.IdolProfile
import com.touchizen.idolface.model.UserStatus
import com.touchizen.idolface.ui.CustomProgressView
import com.touchizen.idolface.ui.datepicker.DatePickerFragment
import com.touchizen.idolface.utils.*
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@AndroidEntryPoint
class FragmentIdolProfile : Fragment(), OnToggledListener {

    private lateinit var binding: FragmentIdolProfileBinding

    private lateinit var context: Activity

    @Inject
    lateinit var preference: MPreference

    @Inject
    lateinit var userCollection: CollectionReference

    private var progressView: CustomProgressView? = null

    private val viewModel: IdolProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        arguments?.getParcelable<IdolProfile>(NavigationHelper.IDOLPROFILE)?.let {
            viewModel.initVars(it)
        }

        binding = FragmentIdolProfileBinding.inflate(layoutInflater, container, false)
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
        binding.imgProPic.setOnClickListener { ImageUtils.askPermission(this) }
        binding.btnDatePicker.setOnClickListener { showTimePickerDialog() }
        binding.btnSaveChanges.setOnClickListener { onSaveChanges() }
        binding.backButton.setOnClickListener { onBackButton()        }
        //binding.fab.setOnClickListener { validate() }
        binding.genderSwitch.setOnToggledListener(this)
        binding.genderSwitch.isOn = viewModel.gender.value!!

        if (!viewModel.birthDate.value.isNullOrBlank()) {
            binding.btnDatePicker.text = viewModel.birthDate.value
        }

        if (!viewModel.idolImgUrl.value.isNullOrBlank()) {
            // Displayed and saved to cache image, as needs for post detail.
            Glide.with(requireActivity())
                .load(viewModel.idolImgUrl.value)
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
                    if (findNavController().isValidDestination(R.id.FIdolProfile)) {
                        progressView?.dismiss()
                        findNavController().navigate(R.id.action_to_idol_fragment)

                        SnackyUtils.showSnackWithOneButton(
                            requireActivity(),
                            getString(R.string.new_idol_displayed),
                            getString(R.string.txt_ok),
                            null
                        )
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

    fun showTimePickerDialog() {
        val dateFragment = DatePickerFragment()
        dateFragment.setBinding(viewModel)
        dateFragment.setPickButton(binding.btnDatePicker)
        dateFragment.show(parentFragmentManager,"dateFragment")
    }

    fun onSaveChanges() {

        val image = viewModel.idolImgUrl.value
        val newName = viewModel.name.value
        val gender = viewModel.gender.value
        val birthDate = viewModel.birthDate.value
        val groupName = viewModel.groupName.value
        val bodyProfile = viewModel.bodyProfile.value
        val company = viewModel.company.value
        val jobClass = viewModel.jobClass.value
        val about = viewModel.about.value
        val picCount = viewModel.picCount.value

        when {
            viewModel.isUploading.value!! -> context.toast("Profile picture is uploading!")
            newName.isNullOrBlank() -> context.toast("Idol name can't be empty!")
            company.isNullOrBlank() -> context.toast("company can't be empty!")
            else -> {
                context.window.decorView.clearFocus()
                viewModel.saveChanges(
                    newName,
                    image ?: "",
                    gender!!,
                    birthDate!!,
                    groupName!!,
                    bodyProfile!!,
                    company!!,
                    jobClass!!,
                    about!!,
                    picCount!!
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ImageUtils.onImagePerResult(this, *grantResults)
    }

    fun onBackButton() {
        NavigationHelper.navigateUp(requireActivity())
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
        viewModel.gender.value = isOn
    }
}