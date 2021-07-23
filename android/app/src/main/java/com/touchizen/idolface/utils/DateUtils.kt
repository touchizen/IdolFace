package com.touchizen.idolface.utils

import java.text.ParseException
import java.text.SimpleDateFormat

object DateUtils {

    fun isValidDate(inDate: String): Boolean {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd")
        dateFormat.isLenient = false
        try {
            dateFormat.parse(inDate.trim { it <= ' ' })
        } catch (pe: ParseException) {
            return false
        }
        return true
    }

}