package com.touchizen.idolface.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Country(
    val code: String, val name: String, val noCode: String,
    val money: String
) : Parcelable
