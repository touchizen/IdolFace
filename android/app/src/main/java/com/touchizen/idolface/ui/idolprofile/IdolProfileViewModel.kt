package com.touchizen.idolface.ui.idolprofile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.*
import com.google.firebase.storage.UploadTask
import com.touchizen.idolface.R
import com.touchizen.idolface.model.IdolProfile
import com.touchizen.idolface.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject


@HiltViewModel
class IdolProfileViewModel @Inject
constructor(
    @ApplicationContext private val context: Context,
    private val preference: MPreference
) : ViewModel()
{

    val progressProPic = MutableLiveData(false)

    val profileUpdateState = MutableLiveData<LoadState>()
    val isUploading = MutableLiveData(false)
    val checkUserNameState = MutableLiveData<LoadState>()

    var idolId = MutableLiveData(IdolUtils.getUniqueId())
    val name = MutableLiveData("")
    val oldname = MutableLiveData("")
    val birthDate = MutableLiveData("")
    val gender = MutableLiveData(true)
    val groupName = MutableLiveData("")
    val bodyProfile = MutableLiveData("")
    val company = MutableLiveData("")
    val picCount = MutableLiveData<Long>(0)
    val jobClass = MutableLiveData("")
    val about = MutableLiveData("")

    var idolProfile : IdolProfile? = null

    private var storageRef = IdolUtils.getStorageRef(context, idolId.value!!)
    private var docuRef = IdolUtils.getDocumentRef(context, idolId.value!!)

    val idolImgUrl = MutableLiveData("")

    private var createdAt: Long = System.currentTimeMillis()

    private lateinit var uploadTask: UploadTask


    init {
        Log.d("idolpro","IdolProfileViewModel")
    }

    fun initVars(idol:IdolProfile) {
        this.idolProfile = idol
        this.idolId.value = idol.id
        this.storageRef = IdolUtils.getStorageRef(context, idol.id)
        this.docuRef = IdolUtils.getDocumentRef(context, idol.id)
        this.name.value = idol.name
        this.idolImgUrl.value = idol.image
        this.birthDate.value = idol.birthDate
        this.gender.value = idol.gender
        this.groupName.value = idol.groupName
        this.bodyProfile.value = idol.bodyProfile
        this.company.value = idol.company
        this.picCount.value = idol.pictureCount
        this.jobClass.value = idol.jobClass
        this.about.value = idol.about
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
                    idolImgUrl.value = taskResult.result.toString()
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

    fun saveChanges(
        name: String,
        image: String,
        gender: Boolean,
        birthDate: String,
        groupName: String,
        bodyProfile: String,
        company: String,
        jobClass: String,
        about: String,
        picCount: Long
    ) {
        //name.toLowerCase(Locale.getDefault())
        //updateProfileData(name, strAbout, image)
        this.name.value = name
        this.idolImgUrl.value = image
        this.birthDate.value = birthDate
        this.gender.value = gender
        this.groupName.value = groupName
        this.bodyProfile.value = bodyProfile
        this.company.value = company
        this.jobClass.value = jobClass
        this.about.value = about
        this.picCount.value = picCount

        storeProfileData()
    }

    fun storeProfileData() {
        try {
            profileUpdateState.value = LoadState.OnLoading
            val profile = IdolProfile(
                "",
                preference.getUid()!!,
                createdAt,
                System.currentTimeMillis(),
                idolImgUrl.value!!,
                name.value!!,
                oldname.value!!,
                groupName.value!!,
                company.value!!,
                birthDate.value!!,
                picCount.value!!,
                bodyProfile.value!!,
                gender.value!!,
                jobClass.value!!,
                about.value!!
            )

            docuRef.set(profile, SetOptions.merge()).addOnSuccessListener {
                context.toast("Saved the idol profile successfully")
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
        Log.d("","IdolProfileViewModel Cleared")
        super.onCleared()
    }
}

