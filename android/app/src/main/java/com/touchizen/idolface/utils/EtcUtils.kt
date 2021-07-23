package com.touchizen.idolface.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.touchizen.idolface.model.Country

object EtcUtils {

    private const val PERMISSION_REQ_CODE = 114

    fun getDefaultCountry() = Country("KR", "Korea", "+82", "KRW")

    fun clearNull(str: String?) = str?.trim() ?: ""

    fun startNewActivity(activity: Activity, className: Class<*>?) {
        val intent = Intent(activity, className)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(intent)
        activity.finish()
    }

    fun checkPermission(context: Fragment,
                        vararg permissions: String, reqCode: Int= PERMISSION_REQ_CODE): Boolean {
        var allPermitted = false
        for (permission in permissions) {
            allPermitted = (ContextCompat.checkSelfPermission(context.requireContext(), permission)
                    == PackageManager.PERMISSION_GRANTED)
            if (!allPermitted) break
        }
        if (allPermitted) return true
        context.requestPermissions(
            permissions,
            reqCode
        )
        return false
    }

    fun isPermissionOk(vararg results: Int): Boolean {
        var isAllGranted = true
        for (result in results) {
            if (PackageManager.PERMISSION_GRANTED != result) {
                isAllGranted = false
                break
            }
        }
        return isAllGranted
    }


}