package com.touchizen.idolface.ui.login

//import com.gowtham.letschat.R
//import com.gowtham.letschat.databinding.FLoginBinding
//import com.gowtham.letschat.models.Country
//import com.gowtham.letschat.ui.activities.SharedViewModel
//import com.gowtham.letschat.utils.*
//import com.gowtham.letschat.views.CustomProgressView
import android.content.Context
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.touchizen.idolface.MainActivity
import com.touchizen.idolface.R
import com.touchizen.idolface.about.LicenseFragmentHelper
import com.touchizen.idolface.about.StandardLicenses
import com.touchizen.idolface.databinding.FragmentLoginBinding
import com.touchizen.idolface.model.Country
import com.touchizen.idolface.model.SharedViewModel
import com.touchizen.idolface.ui.CustomProgressView
import com.touchizen.idolface.utils.*
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.CompositeDisposable

@AndroidEntryPoint
class FragmentLogin : Fragment() {

    private var country: Country? = null

    private lateinit var binding: FragmentLoginBinding

    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private var progressView: CustomProgressView?=null
    private val compositeDisposable = CompositeDisposable()

    private val viewModel by activityViewModels<LogInViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressView = CustomProgressView(requireContext())
        setDataInView()
        subscribeObservers()
    }

    private fun setDataInView() {
        binding.viewmodel = viewModel
        setDefaultCountry()
        binding.txtCountryCode.setOnClickListener {
            Keyboard.close(requireActivity())
            findNavController().navigate(R.id.action_FLogIn_to_FCountries)
        }
        binding.btnGetOtp.setOnClickListener {
            validate()
        }

        binding.memberLicense.setOnClickListener {
            showLicenseAgreement()
        }
    }

    private fun showLicenseAgreement() {
        //activeLicense = StandardLicenses.GPL3
        compositeDisposable.add(
            LicenseFragmentHelper.showLicense(
                requireActivity(),
                StandardLicenses.MEMBER
            )
        )
    }

    private fun validate() {
        try {
            Keyboard.close(requireActivity())
            val mobileNo = viewModel.mobile.value?.trim()
            val country = viewModel.country.value
            when {
                Validator.isMobileNumberEmpty(mobileNo) -> snack(requireActivity(), "Enter valid mobile number")
                country == null -> snack(requireActivity(), "Select a country")
                !Validator.isValidNo(country.code, mobileNo!!) -> snack(
                    requireActivity(),
                    "Enter valid mobile number"
                )
                NetUtils.isNoInternet(requireContext()) -> snackNet(requireActivity())
                else -> {
                    viewModel.setMobile()
                    viewModel.setProgress(true)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setDefaultCountry() {
        try {
            country = EtcUtils.getDefaultCountry()
            val manager =
                requireActivity().getSystemService(Context.TELEPHONY_SERVICE) as (TelephonyManager)?
            manager?.let {
                val countryCode = EtcUtils.clearNull(manager.networkCountryIso)
                if (countryCode.isEmpty())
                    return
                val countries = Countries.getCountries()
                for (i in countries) {
                    if (i.code.equals(countryCode, true))
                        country = i
                }
                viewModel.setCountry(country!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun subscribeObservers() {
        try {
            sharedViewModel.country.observe(viewLifecycleOwner, {
                viewModel.setCountry(it)
            })

            viewModel.getProgress().observe(viewLifecycleOwner, {
                progressView?.toggle(it)
            })

            viewModel.getVerificationId().observe(viewLifecycleOwner, { vCode ->
                vCode?.let {
                    viewModel.setProgress(false)
                    viewModel.resetTimer()
                    viewModel.setVCodeNull()
                    viewModel.setEmptyText()
                    if (findNavController().isValidDestination(R.id.fragment_login))
                        findNavController().navigate(R.id.action_FLogIn_to_FVerify)
                }
            })

            viewModel.getFailed().observe(viewLifecycleOwner, {
                progressView?.dismiss()
            })

            viewModel.getTaskResult().observe(viewLifecycleOwner, { taskId ->
                if (taskId!=null && viewModel.getCredential().value?.smsCode.isNullOrEmpty())
                    viewModel.fetchUser(taskId)
            })

            viewModel.userProfileGot.observe(viewLifecycleOwner, { success ->
                if (success && viewModel.getCredential().value?.smsCode.isNullOrEmpty()
                               && findNavController().isValidDestination(R.id.fragment_login)) {
                    requireActivity().toastLong("Authenticated successfully using Instant verification")
                    findNavController().navigate(R.id.action_FLogIn_to_FProfile)
                    (requireActivity() as MainActivity).onLoginFinished()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    /*       val action = FMobileDirections.actionFMobileToFVerify(
             Country(
                 code = "sd",
                 name = "sda",
                 noCode = "+83",
                 money = "mon"
             )
         )
         findNavController().navigate(action)*/

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
            compositeDisposable.dispose()
            progressView?.dismissIfShowing()
            super.onDestroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}