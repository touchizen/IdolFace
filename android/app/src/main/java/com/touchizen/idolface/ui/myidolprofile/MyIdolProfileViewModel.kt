package com.touchizen.idolface.ui.myidolprofile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.databinding.BindingAdapter
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
import java.util.*
import javax.inject.Inject


@HiltViewModel
class MyIdolProfileViewModel @Inject
constructor(
    @ApplicationContext private val context: Context,
    private val preference: MPreference
) : ViewModel()
{

    val progressProPic = MutableLiveData(false)

    val profileUpdateState = MutableLiveData<LoadState>()
    val isUploading = MutableLiveData(false)
    val checkUserNameState = MutableLiveData<LoadState>()

    var idolId = MutableLiveData(IdolUtils.getReportUniqueId())

    var idolProfile = MutableLiveData<IdolProfile>()

    private var docuRef = IdolUtils.getReportDocumentRef(context, idolId.value!!)

    private var createdAt: Long = System.currentTimeMillis()

    init {
        Log.d("MyIdolProfile","MyIdolProfileViewModel")
    }

    fun storeProfileData() {
        try {
            profileUpdateState.value = LoadState.OnLoading

            idolProfile.value!!.createdAt = createdAt

            docuRef.set(idolProfile, SetOptions.merge()).addOnSuccessListener {
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

