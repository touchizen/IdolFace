package com.touchizen.idolface.model

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@IgnoreExtraProperties
@Parcelize
data class IdolProfile(
    var id: String="",
    var userId: String="",
    var createdAt: Long=0,
    var updatedAt: Long=0,
    var image: String="",
    var name: String="",
    var oldName: String="",
    var groupName: String="",
    var company: String="",
    var birthDate: String="",
    var pictureCount: Long=0,
    var bodyProfile: String="",
    var gender:Boolean=true,
    var jobClass:String="",
    var about:String="",
    var isApproved:Boolean=false
    ) : Parcelable