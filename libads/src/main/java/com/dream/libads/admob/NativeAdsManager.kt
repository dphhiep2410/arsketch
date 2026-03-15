package com.dream.libads.admob

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.dream.libads.utils.Constants
import com.dream.libads.utils.Utils
//import android.util.AppLog
import com.google.ads.mediation.applovin.AppLovinMediationAdapter
import com.google.ads.mediation.facebook.FacebookMediationAdapter
import com.google.ads.mediation.mintegral.MintegralMediationAdapter
import com.google.ads.mediation.pangle.PangleMediationAdapter
import com.google.ads.mediation.vungle.VungleMediationAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

class NativeAdsManager constructor(
    private val context: Context,
    private val idNativeAds01: String,
    private val shouldReloadAfterTouch: Boolean = true
) {
    private var mAdLoader: AdLoader? = null
    var nativeAd: NativeAd? = null

    var handler: Handler? = null
    var runable: Runnable? = null
    private var isAdClicked = false

    fun loadAds(
        onLoadSuccess: ((NativeAd) -> Unit)? = null,
        onLoadFail: ((failed: Boolean, nativeAd: NativeAd?) -> Unit)? = null,
        actionWhenOpen: (() -> Unit)? = null
    ) {
        if (!Utils.isOnline(context)) {
            onLoadFail?.invoke(true, nativeAd)
            return
        }
//        AppLog.d("TAG", "loadAds: "+idNativeAds01)

        handler = Handler(Looper.getMainLooper())
        runable = Runnable {
            if (handler != null) {
                handler = null
                onLoadFail?.invoke(true, nativeAd)
            }
        }
        handler?.postDelayed(runable!!, Constants.TIME_OUT_DELAY)
        requestAds(idNativeAds01, onLoadSuccess, onLoadFail = {
            runable?.let { handler?.removeCallbacks(it) }
            onLoadFail?.invoke(true, nativeAd)
        },actionWhenOpen)

    }

    fun requestAds(
        idNativeAds: String,
        onLoadSuccess: ((NativeAd) -> Unit)? = null,
        onLoadFail: (() -> Unit)? = null,
        actionWhenOpen: (() -> Unit)? = null
    ) {
//        AppLog.d("TAG", "requestAds: "+idNativeAds)
        mAdLoader = AdLoader.Builder(context, idNativeAds).withNativeAdOptions(
            NativeAdOptions.Builder()
                .setVideoOptions(VideoOptions.Builder().setStartMuted(true).build())
                .setRequestCustomMuteThisAd(true).build()
        ).forNativeAd {
            nativeAd?.destroy()
            nativeAd = it
//            onLoadSuccess?.invoke(it)
            nativeAd?.setOnPaidEventListener {
                Utils.postRevenueAdjust(it, idNativeAds)
            }


        }.withAdListener(object : AdListener() {
            override fun onAdOpened() {
                super.onAdOpened()
                actionWhenOpen?.invoke()
            }
            override fun onAdLoaded() {
                super.onAdLoaded()
                if (handler != null) {
                    runable?.let { handler?.removeCallbacks(it) }
                    handler = null
                    nativeAd?.let { onLoadSuccess?.invoke(it) }
                }
            }

            override fun onAdClosed() {
                super.onAdClosed()
                if (shouldReloadAfterTouch) {
                    val request = AdRequest.Builder().build()
                    mAdLoader?.loadAd(request)
                }
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
//                AppLog.d("TAG", "LoadAdError: " + p0.cause)
//                AppLog.d("TAG", "LoadAdError: " + p0.message)
//                AppLog.d("TAG", "LoadAdError: " + p0.code)
//                AppLog.d("TAG", "LoadAdError: " + p0.domain)
                if (handler != null) {
                    runable?.let { handler?.removeCallbacks(it) }
                    handler = null
                    onLoadFail?.invoke()
                }
            }
        }).build()

        val extras = Bundle()
        val request =
            AdRequest.Builder().addNetworkExtrasBundle(FacebookMediationAdapter::class.java, extras)
                .addNetworkExtrasBundle(PangleMediationAdapter::class.java, extras)
                .addNetworkExtrasBundle(
                    AppLovinMediationAdapter::class.java, extras
                ).addNetworkExtrasBundle(MintegralMediationAdapter::class.java, extras)
                .addNetworkExtrasBundle(VungleMediationAdapter::class.java, extras).build()
        mAdLoader?.loadAd(request)
    }


}