package com.adafruit.pyleap.utils

import android.content.Context
import java.io.IOException

fun getDataFromAssetAsString(context: Context, fileName: String): String? {
    val result: String
    try {
        result = context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        return null
    }
    return result
}


