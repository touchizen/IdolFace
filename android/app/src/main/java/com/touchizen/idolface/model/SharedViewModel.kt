package com.touchizen.idolface.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.touchizen.idolface.model.Country
import com.touchizen.idolface.utils.ScreenState
import com.touchizen.idolface.utils.printMeD
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {

    val country = MutableLiveData<Country>()

    val openMainAct = MutableLiveData<Boolean>()

    private val _state = MutableLiveData<ScreenState>(ScreenState.IdleState)

    val lastQuery = MutableLiveData<String>()

    val listOfQuery = arrayListOf("")

    private var timer: TimerTask? = null

    init {
        "Init SharedViewModel".printMeD()
    }

    fun getLastQuery(): LiveData<String> {
        return lastQuery
    }

    fun setLastQuery(query: String) {
        Log.d("Shared","Last Query $query")
        listOfQuery.add(query)
        lastQuery.value = query
    }

    fun setState(state: ScreenState) {
        Log.d("Shared","State $state")
        _state.value = state
    }

    fun getState(): LiveData<ScreenState> {
        return _state
    }

    fun setCountry(country: Country) {
        this.country.value = country
    }


    fun onFromSplash() {
        if (timer == null) {
            timer = Timer().schedule(2000) {
                openMainAct.postValue(true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        "onCleared SharedViewModel".printMeD()
    }

}