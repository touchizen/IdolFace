package com.touchizen.idolface.ui.idolgallery

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
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
class IdolGalleryViewModel @Inject
constructor(
    @ApplicationContext private val context: Context,
    private val preference: MPreference
) : ViewModel()
{

    val imageUpdateState = MutableLiveData<LoadState>()

    //val isUploading = MutableLiveData(false)
    val checkIdolImageState = MutableLiveData<LoadState>()

    val idolGalleryState = MutableLiveData<LoadState>()

    //var imageId = MutableLiveData(IdolUtils.getGalleryUniqueId())
    var imageId = MutableLiveData(IdolUtils.getGalleryUniqueId())
    var imageUrl= MutableLiveData("")
    val userName = MutableLiveData("")
    var userId = MutableLiveData("")
    var yesCount = MutableLiveData<Long>(0)
    var noCount = MutableLiveData<Long>(0)
    var realName= MutableLiveData("")
    var copyright= MutableLiveData(false)

    var idolProfile=MutableLiveData<IdolProfile>()

    var idolImage : IdolImage? = null
    var createdAt: Long = System.currentTimeMillis()

    private var storageRef = IdolUtils.getGalleryStorageRef(context, imageId.value!!)
    private var docuRef = IdolUtils.getGalleryDocumentRef(context, imageId.value!!)

    private lateinit var docIdolProfileRef : DocumentReference // = IdolUtils.getDocumentRef(context, idolId.value!!)

    private lateinit var uploadTask: UploadTask

    val flow = Pager(PagingConfig(15)) {
        IdolGalleryPagingSource(FirebaseFirestore.getInstance(), idolProfile)
    }.flow.cachedIn(viewModelScope)


    init {
        Log.d("idolgallery","IdolGalleryViewModel")
    }

    fun initVars() {
        this.docIdolProfileRef = IdolUtils.getDocumentRef(context, idolProfile.value?.id!!)
    }

    fun uploadIdolImage(imagePath: Uri) {
        try {
            checkIdolImageState.value = LoadState.OnLoading
            val child = storageRef.child("gallery_${System.currentTimeMillis()}.jpg")
            if (this::uploadTask.isInitialized && uploadTask.isInProgress)
                uploadTask.cancel()
            uploadTask = child.putFile(imagePath)
            uploadTask.addOnSuccessListener {
                child.downloadUrl.addOnCompleteListener { taskResult ->
                    checkIdolImageState.value = LoadState.OnSuccess()
                    imageUrl.value = taskResult.result.toString()

                    saveChanges(
                        imageUrl.value!!,
                        preference?.getUid()!!,
                        preference?.getUsername()!!,
                        createdAt,
                        0,
                        0,
                        "",
                        false
                    )

                }.addOnFailureListener {
                    OnFailureListener { e ->
                        checkIdolImageState.value = LoadState.OnFailure(e)
                        context.toast(e.message.toString())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        this.imageUrl.value = imageUrl
        this.userId.value = userId
        this.userName.value = userName
        this.createdAt =  createdAt

        this.yesCount.value = yesCount
        this.noCount.value = noCount
        this.realName.value = realName
        this.copyright.value = copyright

        storeData()
    }

    fun storeData() {
        try {
            imageUpdateState.value = LoadState.OnLoading
            idolImage = IdolImage(
                "",
                imageUrl.value!!,
                preference.getUid()!!,
                idolProfile.value!!.id,
                userName.value!!,
                createdAt,
                yesCount.value!!,
                noCount.value!!,
                realName.value!!,
                copyright.value!!
            )

            val db = FirebaseFirestore.getInstance()
            db.runTransaction{ transaction ->
                transaction.set(docuRef, idolImage!!)
                transaction.update(
                    docIdolProfileRef,
                    "pictureCount",
                    idolProfile.value!!.pictureCount.inc()
                )
            }.addOnSuccessListener {
                context.toast("Saved the idol image successfully")
                imageUpdateState.value = LoadState.OnSuccess()
            }.addOnFailureListener{ e ->
                context.toast(e.message.toString())
                imageUpdateState.value = LoadState.OnFailure(e)
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

