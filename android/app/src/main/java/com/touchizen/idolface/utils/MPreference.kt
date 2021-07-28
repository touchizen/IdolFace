package com.touchizen.idolface.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.camera.core.CameraSelector
import com.google.gson.Gson
import com.touchizen.idolface.model.IdolImage
import com.touchizen.idolface.model.IdolProfile
import com.touchizen.idolface.model.ModelMobile
import com.touchizen.idolface.model.UserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MPreference @Inject constructor(@ApplicationContext private val context: Context) {

    private val UID = "userId"

    private val LOGIN="login"

    private val USER="user"

    private val USERNAME="userName"

    private val MOBILE="mobile"

    private val TOKEN="token"

    private val GENDER="gender"

    private val CAMERA_FACING="camera_facing"

    private val IDOL_PROFILE="idol_profile"

    private val IDOL_IMAGE="idol_image"

    private val ONLINE_USER="online_user"

    private val ONLINE_GROUP="online_group"

    private val LOGIN_TIME="login_time"

    private val FRAGMENT_AFTER_LOGIN="fragment_after_login"

    private val LAST_LOGGED_DEVICE_SAME="last_logged_device_same"

    private val PREFS_FILENAME = "com.touchizen.idolface.utils.prefs"

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_FILENAME, 0)

    private val editor = sharedPreferences.edit()

    private fun storeString(key: String, value: String) {
        editor.run {
            putString(key, value)
            apply()
        }
    }

    private fun storeLong(key: String, value: Long) {
        editor.run {
            putLong(key, value)
            apply()
        }
    }

    private fun storeInt(key: String, value: Int) {
        editor.run {
            putInt(key, value)
            apply()
        }
    }

    private fun storeBoolean(key: String, value: Boolean) =
        editor.run {
            putBoolean(key, value)
            apply()
        }

    private fun getString(key: String) =
        sharedPreferences.getString(key, "")

    private fun isBoolean(key: String) =
        sharedPreferences.getBoolean(key, false)

    fun clearAll() =
        editor?.run {
            clear()
            apply()
        }

    fun setLogin(){
        storeBoolean(LOGIN, true)
    }

    fun setLastDevice(same: Boolean){
        storeBoolean(LAST_LOGGED_DEVICE_SAME, same)
    }

    fun setLogInTime(){
        storeLong(LOGIN_TIME,System.currentTimeMillis())
    }

    fun getLogInTime()=
        sharedPreferences.getLong(LOGIN_TIME, 0)


    fun setCameraFacing(lensFacing: Int) {
        storeInt(CAMERA_FACING, lensFacing)
    }

    fun getCameraFacing() =
        sharedPreferences.getInt(CAMERA_FACING, CameraSelector.LENS_FACING_BACK)

    fun setGender(selected: Int) {
        storeInt(GENDER, selected)
    }

    fun getGender() =
        sharedPreferences.getInt(GENDER, 0)

    fun setFragmentAfterLogin(fragment: String) {
        storeString(FRAGMENT_AFTER_LOGIN, fragment)
    }

    fun getFragmentAfterLogin() =
        sharedPreferences.getString(FRAGMENT_AFTER_LOGIN, "")

    fun setCurrentUser(id: String){
        storeString(ONLINE_USER, id)
    }

    fun getUsername() = getUserProfile()!!.userName

    fun clearCurrentUser() {
        setCurrentUser("")
    }

    fun getOnlineUser(): String {
        return getString(ONLINE_USER) ?: ""
    }

    fun setCurrentGroup(id: String){
        storeString(ONLINE_GROUP, id)
    }

    fun clearCurrentGroup() {
        setCurrentGroup("")
    }

    fun getOnlineGroup(): String {
        return getString(ONLINE_GROUP) ?: ""
    }


    fun isSameDevice()=
        sharedPreferences.getBoolean(LAST_LOGGED_DEVICE_SAME, true)

    fun isLoggedIn()= sharedPreferences.getBoolean(LOGIN, false)

    fun isNotLoggedIn()= !isLoggedIn()

    fun setUid(uid: String) =  storeString(UID, uid)

    fun getUid()= getString(UID)

    fun saveProfile(profile: UserProfile){
        storeString(USER, Gson().toJson(profile))
    }

    fun saveMobile(mobile: ModelMobile){
        storeString(MOBILE, Gson().toJson(mobile))
    }

    fun saveIdolProfile(idolProfile: IdolProfile){
        storeString(IDOL_PROFILE, Gson().toJson(idolProfile))
    }

    fun getIdolProfile(): IdolProfile?{
        val str=getString(IDOL_PROFILE)
        if (str.isNullOrBlank())
            return null
        return Gson().fromJson(str, IdolProfile::class.java)
    }

    fun saveIdolImage(idolImage: IdolImage){
        storeString(IDOL_IMAGE, Gson().toJson(idolImage))
    }

    fun getIdolImage(): IdolImage?{
        val str=getString(IDOL_IMAGE)
        if (str.isNullOrBlank())
            return null
        return Gson().fromJson(str, IdolImage::class.java)
    }

    fun updatePushToken(token: String){
        storeString(TOKEN, token)
    }

    fun getPushToken() = getString(TOKEN)

    fun getUserProfile(): UserProfile?  {
        val str=getString(USER)
        if (str.isNullOrBlank())
            return null
        return Gson().fromJson(str, UserProfile::class.java)
    }

    fun getMobile(): ModelMobile?  {
        val str=getString(MOBILE)
        if (str.isNullOrBlank())
            return null
        return Gson().fromJson(str, ModelMobile::class.java)
    }

}