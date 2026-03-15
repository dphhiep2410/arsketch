package com.dream.libads.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.LogLevel
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import com.libads.R
import java.util.concurrent.atomic.AtomicBoolean


class AdsInit(var context: Application) {
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)
    var cmpUtils = CMPUtils(context)
    lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun initialize() {
        initAdjust()
        cmpUtils = CMPUtils(context)
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(context)
    }

    fun showConsentForm(
        activity: Activity, onInitSuccess: (() -> Unit), onInitFailed: (() -> Unit)
    ) {
        googleMobileAdsConsentManager.gatherConsent(activity, onCanShowAds = {
            initializeAdmobAndMax(onInitSuccess)
        }, onDisableAds = {
            onInitFailed.invoke()
        })
    }

    fun showUpdatePrivacyOption(
        activity: Activity, onInitSuccess: (() -> Unit), onInitFailed: (() -> Unit)
    ) {
        googleMobileAdsConsentManager.showPrivacyOptionsForm(
            activity,
            onConsentFormDismissedListener = {
                if (cmpUtils.canShowAds()) {
                    Log.e("CMP", "showConsentForm canShowAds")
                    onInitSuccess.invoke()
                } else {
                    Log.e("CMP", "showConsentForm disableAds")
                    onInitFailed.invoke()
                }
            })
    }

    private fun initializeAdmobAndMax(onInitSuccess: (() -> Unit)) {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            onInitSuccess()
            return
        }
        MobileAds.initialize(context) {
            val configuration =
                RequestConfiguration.Builder().setTestDeviceIds(Constants.testDevices()).build()
            MobileAds.setRequestConfiguration(configuration)
            val statusMap = it.adapterStatusMap
            for (adapterClass in statusMap.keys) {
                val status = statusMap[adapterClass]
                Log.e(
                    "NekoAdsLoader", String.format(
                        " NekoAdsLoader Mediation Adapter name: %s, Description: %s, Latency: %d, initializationState: %s",
                        adapterClass,
                        status!!.description,
                        status.latency,
                        status.initializationState
                    )
                )
            }
            Log.e("NekoAdsLoader", "initialize success")
        }
        initApplovinMediation()
        onInitSuccess.invoke()

    }

    fun showTestingMediation(activity: Activity) {
        MobileAds.openAdInspector(activity) {}
    }

    private fun initApplovinMediation() {
//        val initConfig =
//            AppLovinSdkInitializationConfiguration.builder("lBnu4FPbTxGUi8cqgwNENfRfqINTVk8B9NxC2Japp2DqKPThsIhPCF7zcC6wr9_IN8OLTcG9SR4dT4OJwQPTBf")
//                .setMediationProvider(AppLovinMediationProvider.MAX).build()
//        AppLovinSdk.getInstance(context)
//            .initialize(initConfig) { }
    }

    private fun initAdjust() {
        val config = AdjustConfig(
            context,
            context.getString(R.string.adjust_token),
            AdjustConfig.ENVIRONMENT_PRODUCTION
        )
        config.setLogLevel(LogLevel.WARN)
        Adjust.initSdk(config)
        context.registerActivityLifecycleCallbacks(
            AdjustLifecycleCallbacks()
        )
    }


    inner class AdjustLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
            Adjust.onResume()
        }

        override fun onActivityPaused(activity: Activity) {
            Adjust.onPause()
        }

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {}
    }

}