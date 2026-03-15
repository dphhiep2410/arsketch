package com.dream.libads.admob

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.dream.libads.utils.Constants
import com.dream.libads.utils.Utils
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.admanager.AdManagerAdView

class AdaptiveBannerManager(
    private val context: Activity,
    private val mIdBanner01: String,
    private val isCollapsible: Boolean = true,
    private var view: ViewGroup? = null
) {
    var adView: AdManagerAdView? = null
    var isBannerLoaded = false

    companion object {}

    private val adSize: AdSize
        get() {
            val display = context.windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = context.resources.displayMetrics.widthPixels.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }
            val adWidth = if (view != null) {
                (view!!.measuredWidth / density).toInt()
            } else {
                (adWidthPixels / density).toInt()
            }
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        }

    fun loadBanner(
        parent: ViewGroup?,
        onAdLoader: ((adView: AdManagerAdView) -> Unit)? = null,
        onAdLoadFail: (() -> Unit)? = null,
        onOpened: (() -> Unit)? = null
    ) {
//        Log.d("TAG", "loadBanner: "+mIdBanner01)
        if (!Utils.isOnline(context)) {
            onAdLoadFail?.invoke()
            return
        }

        var handler: Handler? = Handler()
        val runnable = Runnable {
            if (handler != null) {
                onAdLoadFail?.invoke()
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
            onAdLoadFail?.invoke()
        }, onOpened)

    }

    private fun requestBannerAdsPrepare(
        idBanner: String,
        parent: ViewGroup?,
        onAdLoader: ((adView: AdManagerAdView) -> Unit)? = null,
        onAdLoadFail: (() -> Unit)? = null,
        onOpened: (() -> Unit)? = null
    ) {
        adView = AdManagerAdView(context)
//        parent?.removeAllViews()
        adView?.adUnitId = idBanner
        adView?.setAdSizes(adSize)
        adView?.adListener = object : AdListener() {
            override fun onAdClicked() {
                super.onAdClicked()
                onOpened?.invoke()
            }
            override fun onAdLoaded() {
                super.onAdLoaded()
                isBannerLoaded = true
                parent?.removeAllViews()
                parent?.addView(adView)
//                parent?.isVisible = true
                onAdLoader?.invoke(adView!!)
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                onAdLoadFail?.invoke()
//                Log.d("TAG", "onAdFailedToLoad: "+p0.domain)
//                Log.d("TAG", "onAdFailedToLoad: "+p0.message)
//                Log.d("TAG", "onAdFailedToLoad: "+p0.code)
//                Log.d("TAG", "onAdFailedToLoad: "+p0.cause)
            }
        }
        adView?.setOnPaidEventListener {
            Utils.postRevenueAdjust(it, adView?.adUnitId)
        }
        val requestConfiguration =
            RequestConfiguration.Builder().setTestDeviceIds(Constants.testDevices()).build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        val adRequest = if (isCollapsible) {
            AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply {
                putString("collapsible", "bottom")
            }).build()
        } else {
            AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle()).build()
        }
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