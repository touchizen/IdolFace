package com.touchizen.idolface.model

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@IgnoreExtraProperties
@Parcelize
data class IdolImage(
    var id: String="",
    var imageUrl: String="",
    var userId: String="",
    var idolId: String="",
    var userName: String="",
    var createdAt: Long=0,
    var yesCount: Long=0,
    var noCount: Long=0,
    var realName: String="",
    var copyright: Boolean=false
    ) : Parcelable