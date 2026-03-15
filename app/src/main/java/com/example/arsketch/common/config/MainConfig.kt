package com.example.arsketch.common.config

import android.content.Context
import com.example.arsketch.common.AppSharePreference
import com.example.arsketch.common.Constant
import java.util.Locale

class MainConfig(private val context: Context) {

    companion object {
        lateinit var INSTANCE: MainConfig
        fun newInstance(context: Context): MainConfig {
            if (!Companion::INSTANCE.isInitialized) {
                INSTANCE = MainConfig(context)
            }
            return INSTANCE
        }
    }

    var customConfig: Boolean
        get() = AppSharePreference.getInstance(context).getBoolean("customConfig", false)
        set(customConfig) = AppSharePreference.getInstance(context)
            .saveBoolean("customConfig", customConfig)
    var canSplashLoadConfig: Boolean
        get() = AppSharePreference.getInstance(context).getBoolean("Constant.KEY_SPLASH_LOAD", true)
        set(canSplashLoadConfig) = AppSharePreference.getInstance(context)
            .saveBoolean("Constant.KEY_SPLASH_LOAD", canSplashLoadConfig)
    var isAdsConfigDone: Boolean
        get() = AppSharePreference.getInstance(context)
            .getBoolean("Constant.KEY_ADS_CONFIG_DONE", false)
        set(isAdsConfigDone) = AppSharePreference.getInstance(context)
            .saveBoolean("Constant.KEY_ADS_CONFIG_DONE", isAdsConfigDone)
    var isRealAds: Boolean
        get() = AppSharePreference.getInstance(context).getBoolean("Constant.KEY_REAL_ADS", true)
        set(isRealAds) = AppSharePreference.getInstance(context)
            .saveBoolean("Constant.KEY_REAL_ADS", isRealAds)
    var savedLanguage: String
        get() = AppSharePreference.getInstance(context)
            .getString(Constant.KEY_LANGUAGE, Locale.getDefault().language)
        set(savedLanguage) = AppSharePreference.getInstance(context)
            .saveString(Constant.KEY_LANGUAGE, savedLanguage)

    var rateTime: Long
        get() = AppSharePreference.getInstance(context)
            .getLong(Constant.KEY_RATE_TIME, System.currentTimeMillis())
        set(rateTime) = AppSharePreference.getInstance(context)
            .saveLong(Constant.KEY_RATE_TIME, rateTime)

    var isTorchEnable: Boolean
        get() = AppSharePreference.getInstance(context)
            .getBoolean(Constant.KEY_TORCH, false)
        set(isTorchEnable) = AppSharePreference.getInstance(context)
            .saveBoolean(Constant.KEY_TORCH, isTorchEnable)

    var downloadedMap: MutableMap<Int, List<Pair<Int,String>>>
        get() = AppSharePreference.getInstance(context).getMap(
            Constant.KEY_DOWNLOADED_MAP,
            mutableMapOf()
        )
        set(downloadedMap) = AppSharePreference.getInstance(context)
            .saveMap(Constant.KEY_DOWNLOADED_MAP, downloadedMap)

    var isPassOnboard: Boolean
        get() = AppSharePreference.getInstance(context)
            .getBoolean(Constant.KEY_PASS_ONBOARD, false)
        set(isPassOnboard) = AppSharePreference.getInstance(context)
            .saveBoolean(Constant.KEY_PASS_ONBOARD, isPassOnboard)

    var isPassLang: Boolean
        get() = AppSharePreference.getInstance(context)
            .getBoolean(Constant.KEY_PASS_LANGUAGE, false)
        set(isPassLang) = AppSharePreference.getInstance(context)
            .saveBoolean(Constant.KEY_PASS_LANGUAGE, isPassLang)

//    var timeLogin: Int
//        get() = AppSharePreference.getInstance(context).getInt(Constant.KEY_TIME_LOGIN, 0)
//        set(timeLogin) = AppSharePreference.getInstance(context)
//            .saveInt(Constant.KEY_TIME_LOGIN, timeLogin)
//    var isUserNotifyGranted: Boolean
//        get() = AppSharePreference.getInstance(context)
//            .getBoolean(Constant.KEY_NOTIFY_GRANTED, false)
//        set(isUserNotifyGranted) = AppSharePreference.getInstance(context)
//            .saveBoolean(Constant.KEY_NOTIFY_GRANTED, isUserNotifyGranted)
    var isUserRated: Boolean
        get() = AppSharePreference.getInstance(context).getBoolean(Constant.KEY_RATE, false)
        set(isUserRated) = AppSharePreference.getInstance(context)
            .saveBoolean(Constant.KEY_RATE, isUserRated)
//
//    var timeLogin: Int
//        get() = AppSharePreference.getInstance(context).getInt(Constant.KEY_TIME_LOGIN, 0)
//        set(timeLogin) = AppSharePreference.getInstance(context)
//            .saveInt(Constant.KEY_TIME_LOGIN, timeLogin)
//


}