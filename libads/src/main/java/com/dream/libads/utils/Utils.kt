package com.dream.libads.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.google.android.gms.ads.AdValue


object Utils {
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) && capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            ) -> true

            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) && capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            ) -> true

            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) && capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            ) -> true

            else -> false
        }
    }
    fun postRevenueAdjust(ad: AdValue, adUnit: String?) {
        val adjustAdRevenue = AdjustAdRevenue("admob_sdk")
        val rev = ad.valueMicros.toDouble() / 1000000
        adjustAdRevenue.setRevenue(rev, ad.currencyCode)
        adjustAdRevenue.setAdRevenueUnit(adUnit)
        Adjust.trackAdRevenue(adjustAdRevenue)
    }

}