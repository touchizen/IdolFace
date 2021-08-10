package com.touchizen.idolface.api

import com.touchizen.idolface.AppConstants

object ApiFactory {
    val placeholderApi : PlaceholderApi = RetrofitFactory
        .retrofit(AppConstants.BASE_URL)
        .create(PlaceholderApi::class.java)
}