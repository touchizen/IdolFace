package com.touchizen.idolface.ui.login

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.touchizen.idolface.ClassifierActivity
import com.touchizen.idolface.MainActivity
import com.touchizen.idolface.model.Country
import com.touchizen.idolface.utils.LogInFailedState
import com.touchizen.idolface.utils.printMeD
import com.touchizen.idolface.utils.toast
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
//import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LoginRepo @Inject constructor(
    @ActivityRetainedScoped val actContxt: ClassifierActivity,
    @ApplicationContext val context: Context
    ) :
    PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

    private val verificationId: MutableLiveData<String> = MutableLiveData()

    private val credential: MutableLiveData<PhoneAuthCredential> = MutableLiveData()

    private val taskResult: MutableLiveData<Task<AuthResult>> = MutableLiveData()

    private val failedState: MutableLiveData<LogInFailedState> = MutableLiveData()

    private val auth = FirebaseAuth.getInstance()

    init {
        "LoginRepo init".printMeD()
    }

    fun setMobile(country: Country, mobile: String) {
        Log.d("LoginRepo","Mobile $mobile")
        val number = country.noCode + " " + mobile
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(actContxt)
            .setCallbacks(this)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
        Log.d("LoginRepo","onVerificationCompleted:$credential")
        this.credential.value = credential
        Handler(Looper.getMainLooper()).postDelayed({
            signInWithPhoneAuthCredential(credential)
        }, 1000)
    }

    override fun onVerificationFailed(exp: FirebaseException) {
        "onVerficationFailed:: ${exp.message}".printMeD()
        failedState.value = LogInFailedState.Verification
        when (exp) {
            is FirebaseAuthInvalidCredentialsException ->
                context.toast("Invalid Request")
            else -> context.toast(exp.message.toString())
        }
    }

    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
        Log.d("LoginRepo","onCodeSent:$verificationId")
        this.verificationId.value = verificationId
        context.toast("Verification code sent successfully")
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("LoginRepo","signInWithCredential:success")
                    taskResult.value = task
                } else {
                    Log.d("LoginRepo","signInWithCredential:failure ${task.exception}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException)
                        context.toast("Invalid verification code!")
                    failedState.value = LogInFailedState.SignIn
                }
            }
    }

    fun setCredential(credential: PhoneAuthCredential) {
        signInWithPhoneAuthCredential(credential)
    }

    fun getVCode(): MutableLiveData<String> {
        return verificationId
    }

    fun setVCodeNull() {
        verificationId.value = null
    }

    fun clearOldAuth(){
        credential.value=null
        taskResult.value=null
    }

    fun getCredential(): LiveData<PhoneAuthCredential> {
        return credential
    }

    fun getTaskResult(): LiveData<Task<AuthResult>> {
        return taskResult
    }

    fun getFailed(): LiveData<LogInFailedState> {
        return failedState
    }
}