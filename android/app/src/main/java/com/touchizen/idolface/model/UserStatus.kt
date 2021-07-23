package com.touchizen.idolface.model

data class UserStatus (
    var status: String="offline", val last_seen: Long=0,
    val typing_status: String="non_typing", val chatuser: String?=null) {
    constructor(offline: String) : this() {
        status = offline
    }
}