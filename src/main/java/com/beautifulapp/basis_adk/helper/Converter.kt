package com.beautifulapp.basis_adk.helper

import android.util.Log
import androidx.databinding.InverseMethod
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Converter {

    @JvmStatic
    fun secondToString(second: Number?, pattern: String?): String {
        second ?: kotlin.run { return "" }
        return Calendar.getInstance(Locale.getDefault()).apply {
            timeInMillis = second.toLong() * 1000 - timeZone.rawOffset
        }.let {
            if (pattern.isNullOrBlank()) {
                SimpleDateFormat().format(it.time)
            } else SimpleDateFormat(pattern).format(it.time)
        } ?: ""

    }

    @JvmStatic
    fun dateToString(date: Date?, pattern: String?): String {
        return date?.let {
            if (pattern.isNullOrBlank()) {
                SimpleDateFormat().format(date)
            } else SimpleDateFormat(pattern).format(date)
        } ?: ""
    }


    @JvmStatic
    fun stringToDate(date: String?, format: String = "EEE MMM dd HH:mm:ss zzz yyyy"): Date =
        date?.let {
            try {
                SimpleDateFormat(format, Locale.US).parse(date) ?: Date()
            } catch (e: ParseException) {
                Date()
            }
        } ?: Date()

    @JvmStatic
    fun stringToDate(date: String?): Date =
        date?.let {
            try {
                SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).parse(date) ?: Date()
            } catch (e: ParseException) {
                Date()
            }
        } ?: Date()


    @JvmStatic
    fun meterToKm(d: Double?): String {
        return d?.let {
            "${String.format("%.2f", it / 1000)} Km"
        } ?: "_ _ Km"
    }

    @JvmStatic
    fun milliSecToStringDate(milliSeconds: Long, format: String = "HH:mm:ss.SS"): String =
        SimpleDateFormat(format).apply { timeZone = TimeZone.getTimeZone("GMT") }.format(Calendar.getInstance().apply { timeInMillis = milliSeconds }.time)


    @InverseMethod("strToB")
    @JvmStatic
    fun bToStr(tag: String?, oldvalue: String?, value: Boolean): String? {
        Log.e("strToB", "bToStr  tag: $tag   value: $value   oldvalue: $oldvalue")
        return if (value) tag else oldvalue ?: tag
    }

    @JvmStatic
    fun strToB(tag: String?, oldvalue: String?, value: String?): Boolean {
        Log.e("strToB", "strToB  tag: $tag   value: $value   oldvalue: $oldvalue")
        return (value == tag)
    }


    @InverseMethod("toInt")
    @JvmStatic
    fun toString(value: Number?) = value?.toInt()?.toString()

    @JvmStatic
    fun toInt(value: String?) = value?.toIntOrNull() as? Number


}