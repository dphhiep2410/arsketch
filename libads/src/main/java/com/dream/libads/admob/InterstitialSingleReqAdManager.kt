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
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


class InterstitialSingleReqAdManager constructor(
    private val context: Context,
    private val mIdAdsFull01: String,
) {
    companion object {
        var isShowingAds = false
    }

    var timeLoaded: Long = 0

    private var mInterstitialAd: InterstitialAd? = null
    var handler: Handler? = null
    var runable: Runnable? = null

    fun requestAds(
        onLoadAdSuccess: (() -> Unit)? = null,
        onAdLoadFail: (() -> Unit)? = null,
    ) {
        val requestConfiguration = RequestConfiguration.Builder().build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        if (!Utils.isOnline(context)) {
            onAdLoadFail?.invoke()
            return
        }

        handler = Handler(Looper.getMainLooper())
        runable = Runnable {
            if (handler != null) {
                onAdLoadFail?.invoke()
                handler = null
            }
        }
        handler?.postDelayed(runable!!, Constants.TIME_OUT_DELAY)

        loadAds(onLoadAdSuccess, onAdLoadFail)
    }

    private fun loadAds(
        onAdLoader: (() -> Unit)? = null, onAdLoadFail: (() -> Unit)? = null
    ) {

        requestAdsPrepare(
            mIdAdsFull01, onAdLoader, onAdLoadFail
        )

    }

    private fun requestAdsPrepare(
        idAds: String, onLoadAdSuccess: (() -> Unit)? = null, onAdLoadFail: (() -> Unit)? = null
    ) {
        if (handler == null) {
            return
        }
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, idAds, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
                if (handler == null) {
                    return
                }
                runable?.let { handler?.removeCallbacks(it) }
                handler = null
                onAdLoadFail?.invoke()
            }


            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                timeLoaded = System.currentTimeMillis()

                if (handler == null) {
                    return
                }
                mInterstitialAd = interstitialAd
                runable?.let { handler?.removeCallbacks(it) }
                handler = null
                onLoadAdSuccess?.invoke()
            }
        })
    }

    fun show(
        activity: Activity, onShowAdsFinish: (() -> Unit)? = null, onAdsShowing: (() -> Unit)
    ) {
        if (mInterstitialAd != null) {
            isShowingAds = true
            runable?.let { handler?.removeCallbacks(it) }
            handler = null
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    onShowAdsFinish?.invoke()

                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    onShowAdsFinish?.invoke()
                }

                override fun onAdShowedFullScreenContent() {
                    mInterstitialAd = null
                    Handler(Looper.getMainLooper()).postDelayed({
                        onAdsShowing?.invoke()
                    }, 1000)
                }
            }
            mInterstitialAd?.setOnPaidEventListener {
                Utils.postRevenueAdjust(it, mInterstitialAd?.adUnitId)
            }
            mInterstitialAd?.show(activity)
        } else {
            runable?.let { handler?.removeCallbacks(it) }
            handler = null
            onShowAdsFinish?.invoke()
        }
    }


}