package com.touchizen.idolface.api

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.touchizen.idolface.model.IdolProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

object Apis {

    /**
     * Get Idol list from server.
     */
    fun getIdolList(activity:AppCompatActivity) {

        val service = ApiFactory.placeholderApi

        GlobalScope.launch(Dispatchers.Main) {
            val idolRequest = service.getIdols()
            try {
                val response = idolRequest.await()
                if(response.isSuccessful){
                    val idols = response.body()
                }else{
                    Log.d("MainActivity ",response.errorBody().toString())
                }

            }catch (e: Exception){

            }
        }
    }
}