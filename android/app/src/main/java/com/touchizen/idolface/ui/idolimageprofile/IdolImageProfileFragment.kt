package com.touchizen.idolface.ui.idolimageprofile

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.touchizen.idolface.MainActivity
import com.touchizen.idolface.R
import com.touchizen.idolface.databinding.FragmentIdolImageProfileBinding
import com.touchizen.idolface.model.IdolImage
import com.touchizen.idolface.model.IdolProfile
import com.touchizen.idolface.ui.CustomProgressView
import com.touchizen.idolface.utils.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IdolImageProfileFragment : Fragment() {

    private var instance : IdolImageProfileFragment? = null
    private lateinit var binding: FragmentIdolImageProfileBinding
    private var preference: MPreference? = null

    private lateinit var context: Activity

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
//        Navigation.findNavController(requireActivity(), R.id.fragment_container)
//            .navigate(R.id.action_to_idolgallery_fragment)
    }

    fun onReportButton() {

//        val idolImage = viewModel.idolImage.value
//        val idolProfile = viewModel.idolProfile.value
//        Log.d("idolImage",idolImage!!.imageUrl)
//        Log.d("idolProfile",idolProfile!!.image)
//        viewModel.idolProfile.value!!.image =idolImage.imageUrl

        NavigationHelper.openMyIdolProfile(requireActivity(),
            viewModel.idolProfile.value,
            viewModel.idolImage.value
        )
    }

//    private fun getCurrentItem(): Int {
//        return (recyclerView?.getLayoutManager() as LinearLayoutManager)
//            .findFirstVisibleItemPosition()
//    }
//
//    private fun setCurrentItem(position: Int, smooth: Boolean) {
//        if (smooth) {
//            recyclerView?.smoothScrollToPosition(position)
//        }
//        else {
//            recyclerView?.scrollToPosition(position)
//        }
//    }


}