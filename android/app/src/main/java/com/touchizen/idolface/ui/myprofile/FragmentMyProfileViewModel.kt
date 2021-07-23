package com.touchizen.idolface.ui.myprofile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.UploadTask
import com.touchizen.idolface.utils.LoadState
import com.touchizen.idolface.utils.MPreference
import com.touchizen.idolface.utils.UserUtils
import com.touchizen.idolface.utils.toast
//import com.gowtham.letschat.utils.LoadState
//import com.gowtham.letschat.utils.MPreference
//import com.gowtham.letschat.utils.UserUtils
//import com.gowtham.letschat.utils.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
//import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class FragmentMyProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preference: MPreference
) : ViewModel() {

    private var userProfile = preference.getUserProfile()

    val userName = MutableLiveData(userProfile?.userName)

    val imageUrl = MutableLiveData(userProfile?.image)

    val about = MutableLiveData(userProfile?.about)

    val isUploading = MutableLiveData(false)

    private val mobileData = userProfile?.mobile

    private val storageRef = UserUtils.getStorageRef(context)

    private val docuRef = UserUtils.getDocumentRef(context)

    val mobile = MutableLiveData("${mobileData?.country} ${mobileData?.number}")

    val profileUpdateState = MutableLiveData<LoadState>()

    private lateinit var uploadTask: UploadTask

    init {
        Log.d("MyProfile","FragmentMyProfileViewModel init")
    }

    fun uploadProfileImage(imagePath: Uri) {
        try {
            isUploading.value = true
            val child = storageRef.child("profile_picture_${System.currentTimeMillis()}.jpg")
            if (this::uploadTask.isInitialized && uploadTask.isInProgress)
                uploadTask.cancel()
            uploadTask = child.putFile(imagePath)
            uploadTask.addOnSuccessListener {
                child.downloadUrl.addOnCompleteListener { taskResult ->
                    isUploading.value = false
                    imageUrl.value = taskResult.result.toString()
                }.addOnFailureListener {
                    OnFailureListener { e ->
                        isUploading.value = false
                        context.toast(e.message.toString())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveChanges(name: String, strAbout: String, image: String) {
        name.toLowerCase(Locale.getDefault())
        updateProfileData(name, strAbout, image)
    }

    private fun updateProfileData(name: String, strAbout: String, image: String) {
        try {
            profileUpdateState.value = LoadState.OnLoading
            val profile = userProfile!!
            profile.userName = name
            profile.about = strAbout
            profile.image = image
            profile.updatedAt = System.currentTimeMillis()
            docuRef.set(profile, SetOptions.merge()).addOnSuccessListener {
                context.toast("Profile updated!")
                userProfile = profile
                preference.saveProfile(profile)
                profileUpdateState.value = LoadState.OnSuccess()
            }.addOnFailureListener { e ->
                context.toast(e.message.toString())
                profileUpdateState.value = LoadState.OnFailure(e)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (this::uploadTask.isInitialized && uploadTask.isInProgress)
            uploadTask.cancel()
    }

}
