package com.touchizen.idolface.ui.idolimageprofile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.*
import com.google.firebase.storage.UploadTask
import com.touchizen.idolface.model.IdolImage
import com.touchizen.idolface.model.IdolProfile
import com.touchizen.idolface.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


@HiltViewModel
class IdolImageProfileViewModel @Inject
constructor(
    @ApplicationContext private val context: Context,
    private val preference: MPreference
) : ViewModel()
{

    val imageUpdateState = MutableLiveData<LoadState>()
    val isUploading = MutableLiveData(false)
    val checkIdolImageState = MutableLiveData<LoadState>()

//    var imageId = MutableLiveData(IdolUtils.getGalleryUniqueId())
//    var imageUrl= MutableLiveData("")
//    val userName = MutableLiveData("")
//    var uId = MutableLiveData("")
//    var yesCount = MutableLiveData<Long>(0)
//    var noCount = MutableLiveData<Long>(0)
//    var realName= MutableLiveData("")
//    var copyright= MutableLiveData(false)

    var idolImage = MutableLiveData<IdolImage>()
    var idolProfile = MutableLiveData<IdolProfile>()

//    private var storageRef = IdolUtils.getGalleryStorageRef(context, imageId.value!!)
//    private var docuRef = IdolUtils.getGalleryDocumentRef(context, imageId.value!!)

    private var createdAt: Long = System.currentTimeMillis()

    private lateinit var uploadTask: UploadTask


    init {
        Log.d("idolimge","IdolImageProfileViewModel")
//        val idolProfile = preference.getUserProfile()
//        idolProfile?.let {
//            name.value = idolProfile.userName
//            idolProfilePicUrl.value = userProfile.image
//            about = userProfile.about
//            createdAt = userProfile.createdAt ?: System.currentTimeMillis()
//        }
    }

    fun initVars(idol:IdolImage) {
//        this.imageId.value = idol.id
//        this.imageUrl.value = idol.imageUrl
//        this.storageRef = IdolUtils.getGalleryStorageRef(context, idol.id)
//        this.docuRef = IdolUtils.getGalleryDocumentRef(context, idol.id)
//        this.uId.value = idol.uId
//        this.userName.value = idol.userName
//        this.createdAt =  idol.createdAt
//
//        this.yesCount.value = idol.yesCount
//        this.noCount.value = idol.noCount
//        this.realName.value = idol.realName
//        this.copyright.value = idol.copyright
    }

    fun uploadIdolImage(imagePath: Uri) {
//        try {
//            isUploading.value = true
//            val child = storageRef.child("gallery_${System.currentTimeMillis()}.jpg")
//            if (this::uploadTask.isInitialized && uploadTask.isInProgress)
//                uploadTask.cancel()
//            uploadTask = child.putFile(imagePath)
//            uploadTask.addOnSuccessListener {
//                child.downloadUrl.addOnCompleteListener { taskResult ->
//                    isUploading.value = false
//                    imageUrl.value = taskResult.result.toString()
//                }.addOnFailureListener {
//                    OnFailureListener { e ->
//                        isUploading.value = false
//                        context.toast(e.message.toString())
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    fun saveChanges(
        imageUrl: String,
        userId: String,
        userName: String,
        createdAt: Long,
        yesCount: Long,
        noCount: Long,
        realName: String,
        copyright: Boolean
    ) {
//        this.imageUrl.value = imageUrl
//        this.uId.value = userId
//        this.userName.value = userName
//        this.createdAt =  createdAt
//
//        this.yesCount.value = yesCount
//        this.noCount.value = noCount
//        this.realName.value = realName
//        this.copyright.value = copyright

        storeProfileData()
    }

    fun storeProfileData() {
        try {
            imageUpdateState.value = LoadState.OnLoading
//            val idolImage = IdolImage(
//                "",
//                imageUrl.value!!,
//                preference.getUid()!!,
//                userName.value!!,
//                createdAt,
//                yesCount.value!!,
//                noCount.value!!,
//                realName.value!!,
//                copyright.value!!
//            )
//
//            docuRef.set(idolImage, SetOptions.merge()).addOnSuccessListener {
//                context.toast("Saved the idol profile successfully")
//                imageUpdateState.value = LoadState.OnSuccess()
//            }.addOnFailureListener { e ->
//                context.toast(e.message.toString())
//                imageUpdateState.value = LoadState.OnFailure(e)
//            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        Log.d("","IdolProfileViewModel Cleared")
        super.onCleared()
    }
}

