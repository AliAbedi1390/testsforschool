package com.example.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PersianNumberHelper {
    private val persianDigits = arrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(input: Any?): String {
        if (input == null) return ""
        val str = input.toString()
        val builder = StringBuilder()
        for (char in str) {
            if (char in '0'..'9') {
                builder.append(persianDigits[char - '0'])
            } else {
                builder.append(char)
            }
        }
        return builder.toString()
    }

    fun toPersianPercentage(percentage: Float): String {
        val rounded = Math.round(percentage)
        return "${toPersianDigits(rounded)}٪"
    }

    fun toPersianPercentage(percentage: Double): String {
        val rounded = Math.round(percentage)
        return "${toPersianDigits(rounded)}٪"
    }

    fun formatDateTime(timestamp: Long): String {
        if (timestamp <= 0L) return "-"
        val date = Date(timestamp)
        val format = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault())
        return toPersianDigits(format.format(date))
    }
}
