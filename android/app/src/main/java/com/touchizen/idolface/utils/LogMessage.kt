package com.touchizen.idolface.utils

import android.util.Log
import com.touchizen.idolface.BuildConfig.DEBUG

object LogMessage {

    private val logVisible = DEBUG

    internal fun v(msg: String) {
        if (logVisible) Log.v("LetsChat",msg)
    }

    internal fun e(msg: String) {
        if (logVisible) Log.e("LetsChat",msg)
    }

}