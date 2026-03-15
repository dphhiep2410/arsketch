package com.dream.libads.admob

import android.app.Activity
import android.os.Handler
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.dream.libads.utils.Constants
import com.dream.libads.utils.Utils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.admanager.AdManagerAdView

class MrecBannerManager(
    private val context: Activity,
    private val mIdBanner01: String,
) {
    var adView: AdManagerAdView? = null
    var isBannerLoaded = false

    val adSize: AdSize = AdSize.MEDIUM_RECTANGLE


    fun loadBanner(
        parent: ViewGroup?,
        onAdLoader: ((adView: AdManagerAdView) -> Unit)? = null,
        onAdLoadFail: ((Boolean) -> Unit)? = null
    ) {
        if (!Utils.isOnline(context)) {
            onAdLoadFail?.invoke(isBannerLoaded)
            return
        }

        var handler: Handler? = Handler()
        val runnable = Runnable {
            if (handler != null) {
                onAdLoadFail?.invoke(isBannerLoaded)
                handler = null
            }

        }
        handler?.postDelayed(runnable, Constants.TIME_OUT_DELAY)

        requestBannerAdsPrepare(mIdBanner01, parent, {
            if (handler == null) {
                return@requestBannerAdsPrepare
            }
            handler?.removeCallbacks(runnable)
            handler = null
            onAdLoader?.invoke(it)
        }, onAdLoadFail = {
            if (handler == null) {
                return@requestBannerAdsPrepare
            }
            handler?.removeCallbacks(runnable)
            handler = null
            onAdLoadFail?.invoke(isBannerLoaded)
        })

    }


    private fun requestBannerAdsPrepare(
        idBanner: String,
        parent: ViewGroup?,
        onAdLoader: ((adView: AdManagerAdView) -> Unit)? = null,
        onAdLoadFail: ((Boolean) -> Unit)? = null
    ) {
        adView = AdManagerAdView(context)
//        parent?.removeAllViews()
        adView?.adUnitId = idBanner
        adView?.setAdSizes(adSize)
        adView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                isBannerLoaded = true
                parent?.removeAllViews()
                parent?.addView(adView)
//                parent?.isVisible = true
                onAdLoader?.invoke(adView!!)
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
//                Log.d("TAG", "onAdFailedToLoad: " + p0.domain)
//                Log.d("TAG", "onAdFailedToLoad: " + p0.message)
//                Log.d("TAG", "onAdFailedToLoad: " + p0.cause)
//                Log.d("TAG", "onAdFailedToLoad: " + p0.code)
                super.onAdFailedToLoad(p0)
                onAdLoadFail?.invoke(isBannerLoaded)
            }
        }
        adView?.setOnPaidEventListener {
            Utils.postRevenueAdjust(it, adView?.adUnitId)
        }
        val requestConfiguration =
            RequestConfiguration.Builder().setTestDeviceIds(Constants.testDevices()).build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        val adRequest = AdRequest.Builder().build()
        adView?.loadAd(adRequest)


        fun loadAdViewToParent(parent: ViewGroup?) {
            if (adView?.parent != null) {
                (adView?.parent as ViewGroup).removeAllViews()
            }
            parent?.removeAllViews()
            parent?.addView(adView)
            parent?.isVisible = true

        }

        fun stopView() {
            adView?.pause()
        }

        fun resumeView() {
            adView?.resume()
        }
    }

    fun destroy() {
        try {
            adView?.destroy()

        } catch (e: Exception) {
        }
    }
}