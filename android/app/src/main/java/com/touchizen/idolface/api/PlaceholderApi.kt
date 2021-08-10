package com.touchizen.idolface.api

import com.touchizen.idolface.model.IdolProfile
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET

interface PlaceholderApi {

    @GET("/idolface/idols.json")
    fun getIdols() : Deferred<Response<List<IdolProfile>>>

}