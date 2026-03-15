package com.dream.libads.admob

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.dream.libads.utils.Constants
import com.dream.libads.utils.Utils
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date


class AppOpenStartAdManager constructor(
    private val context: Context,
    private val idOpenAds01: String,
) {
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isAdLoaded = false
    var handler: Handler? = null
    var runable: Runnable? = null

    companion object {
        var isShowingAd = false
    }

    private var loadTime: Long = 0

    fun loadAd(onAdLoader: (() -> Unit)?, onAdLoadFail: (() -> Unit)?) {
        if (isLoadingAd || isAdAvailable()) {
//            AppLog.d("TAG", "loadAd: ")
            return
        }
        handler = Handler(Looper.getMainLooper())
        runable = Runnable {
            if (handler != null) {
                handler = null
                onAdLoadFail?.invoke()
            }
        }
        handler?.postDelayed(runable!!, Constants.TIME_OUT_DELAY)
        isLoadingAd = true
//            AppLog.d("TAG", "loadAd: 1")

        loadAdsPrepare(idOpenAds01, onAdLoader, onAdLoadFail = {
            isAdLoaded = false
            if (handler != null) {
                onAdLoadFail?.invoke()
                runable?.let { handler?.removeCallbacks(it) }
                handler = null
                onAdLoadFail?.invoke()
            }
        })


    }

    private fun loadAdsPrepare(
        idAds: String, onAdLoader: (() -> Unit)? = null, onAdLoadFail: (() -> Unit)? = null
    ) {
        val request = AdRequest.Builder().build()

        AppOpenAd.load(context,
            idAds,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    if (handler == null) {
                        return
                    }
                    appOpenAd = ad
                    isAdLoaded = true
                    isLoadingAd = false
                    runable?.let { handler?.removeCallbacks(it) }
                    handler = null
                    loadTime = Date().time
                    onAdLoader?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                    Log.d("TAG", "onAdFailedToLoad: "+loadAdError.domain)
//                    Log.d("TAG", "onAdFailedToLoad: "+loadAdError.message)
//                    Log.d("TAG", "onAdFailedToLoad: "+loadAdError.responseInfo)
//                    Log.d("TAG", "onAdFailedToLoad: "+loadAdError.code)
                    isLoadingAd = false
                    onAdLoadFail?.invoke()
                }
            })
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }


    fun showAdIfAvailable(
        activity: Activity,
        onShowAdCompleteListener: OnShowAdCompleteListener,
        actionWhenShowAds: (() -> Unit)? = null
    ) {
        if (isShowingAd) {
            return
        }
        if (!isAdAvailable()) {
            onShowAdCompleteListener.onShowAdComplete()
            return
        }
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
//                Log.d("TAG", "onAdDismissedFullScreenContent: ")
                appOpenAd = null
                isShowingAd = false
                onShowAdCompleteListener.onShowAdComplete()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
//                Log.d("TAG", "onAdFailedToShowFullScreenContent: ")
                appOpenAd = null
                isShowingAd = false
                onShowAdCompleteListener.onShowAdComplete()
            }

            override fun onAdShowedFullScreenContent() {
                actionWhenShowAds?.invoke()
                isShowingAd = true
            }
        }
        appOpenAd?.setOnPaidEventListener {
            Utils.postRevenueAdjust(it, "OpenAds")
        }
        appOpenAd?.show(activity)
    }

    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }
}
