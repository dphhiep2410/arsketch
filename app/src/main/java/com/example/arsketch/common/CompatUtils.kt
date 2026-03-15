package com.example.arsketch.common

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.PowerManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.example.arsketch.common.config.MainConfig
import java.util.Locale

val Context.config: MainConfig get() = MainConfig.newInstance(this)

fun Context.createContext(newLocale: Locale): Context = if (buildMinVersionN()) {
    createContextAndroidN(newLocale)
} else {
    createContextLegacy(newLocale)
}

private fun Context.createContextAndroidN(newLocale: Locale): Context {
    val resources: Resources = resources
    val configuration: Configuration = resources.configuration
    configuration.setLocale(newLocale)
    return createConfigurationContext(configuration)
}

fun Context.openLink(strUri: String?) {
    try {
        val uri = Uri.parse(strUri)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun Context.toDp(sizeInDp: Int): Int {
    val scale: Float = this.resources.displayMetrics.density
    return (sizeInDp * scale + 0.5f).toInt()
}


private fun Context.createContextLegacy(newLocale: Locale): Context {
    val resources: Resources = resources
    val configuration: Configuration = resources.configuration
    configuration.locale = newLocale
    resources.updateConfiguration(configuration, resources.displayMetrics)
    return this
}

fun Context.isScreenOn() = (getSystemService(Context.POWER_SERVICE) as PowerManager).isScreenOn


@ColorInt
fun Context.compatColor(@ColorRes id: Int): Int = if (buildMinVersionM()) {
    getColor(id)
} else {
    ContextCompat.getColor(this, id)
}

fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}

fun Context.queryCursor(
    uri: Uri,
    projection: Array<String>,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null,
    showErrors: Boolean = false,
    callback: (cursor: Cursor) -> Unit
) {
    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder,)
        cursor?.use {
            if (cursor.moveToFirst()) {
                do {
                    callback(cursor)
                } while (cursor.moveToNext())
            }
        }
    } catch (e: Exception) {
        if (showErrors) {
//            showErrorToast(e)
        }
    }
}
fun isInternetAvailable(context: Context): Boolean {
    if (buildMinVersionM()) {
        var result = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        cm?.run {
            this.getNetworkCapabilities(this.activeNetwork)?.run {
                result = when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            }
        }
        return result
    } else {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        if (netInfo != null) {
            val networkInfo = cm.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
        return false
    }
}