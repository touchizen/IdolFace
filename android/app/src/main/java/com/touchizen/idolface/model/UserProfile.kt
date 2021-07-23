package com.touchizen.idolface.model

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.touchizen.idolface.model.ModelDeviceDetails
import com.touchizen.idolface.model.ModelMobile
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@IgnoreExtraProperties
@Parcelize
data class UserProfile(
    var uId: String?=null,
    var createdAt: Long?=null,
    var updatedAt: Long?=null,
    var image: String="",
    var userName: String="",
    var about: String="",
    var token :String="",
    var mobile: ModelMobile?=null,
    var isPublic: Boolean= false,
    @get:PropertyName("device_details")
    @set:PropertyName("device_details")
    var deviceDetails: ModelDeviceDetails?=null) : Parcelable