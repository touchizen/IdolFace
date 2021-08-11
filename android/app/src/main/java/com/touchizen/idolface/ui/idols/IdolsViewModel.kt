package com.touchizen.idolface.ui.idols

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.touchizen.idolface.utils.LoadState

class IdolsViewModel() : ViewModel(){

    val idolsProfileState = MutableLiveData<LoadState>()

}