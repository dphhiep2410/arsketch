package com.example.arsketch.common.ads

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import com.dream.libads.admob.InterstitialSingleReqAdManager
import com.dream.libads.admob.MLBBaseNativeAdView
import com.dream.libads.admob.NativeAdsManager
import com.dream.libads.utils.AdsConfigUtils
import com.example.arsketch.BuildConfig
import com.example.arsketch.common.config
import com.example.arsketch.common.hide
import com.example.arsketch.common.isInternetAvailable
import com.example.arsketch.ui.dialog.DialogFragmentLoadingInterAds
import com.example.arsketch.ui.dialog.DialogShowNativeFull
import com.google.android.gms.ads.nativead.NativeAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig


enum class NativeTypeEnum {
    SPLASH, FULL_SPLASH, LANG, OB1, FULL_OB1, OB2, FULL_OB2, OB3, FULL_OB3, COLLAPSE, PERMISSION, SETTING, FULL_SETTING, MEDIUM_CATEGORY, CATEGORY_2, DRAW
}

enum class MrecTypeEnum {
    SPLASH
}

enum class InterAdsEnum {
    SPLASH, OB3, HOME, BACK, DRAW
}

enum class BannerTypeEnum {
    SPLASH
}

enum class RewardTypeEnum {}

object AdsCore {
    //    fun loadBannerAds(
//        context: Activity,
//        action: ((adView: AdManagerAdView) -> Unit)?,
//        actionFail: (() -> Unit)?,
//        actionWhenOpen: (() -> Unit)?,
//        type: BannerTypeEnum
//    ) {
//        if (AdsConfigUtils(context).getPremium()) {
//            actionFail?.invoke()
//            return
//        }
//        val adsStatus = when (type) {
//
//        }
//        when (adsStatus) {
//            AdsConfigUtils.OFF -> {
//                actionFail?.invoke()
//                return
//            }
//
//            else -> {
//                val keyId = if (BuildConfig.FLAVOR == "staging" || !context.config.isRealAds) {
//                    BuildConfig.banner_test
//                } else {
//                    when (type) {
//                        BannerTypeEnum.SPLASH -> ""
//                    }
//
//                }
//                val adaptiveBannerManager = AdaptiveBannerManager(
//                    context, keyId, isCollapsible = false
//                )
//
//                adaptiveBannerManager.loadBanner(null, onAdLoader = {
//                    action?.invoke(it)
//                }, onAdLoadFail = {
//                    actionFail?.invoke()
//                }, onOpened = {
//                    actionWhenOpen?.invoke()
//                })
//            }
//        }
//
//
//    }
    fun showNativeFull(context: Activity,action: () -> Unit,type: NativeTypeEnum) {
        if (isInternetAvailable(context)) {
            val status = getStatusNative(type,context)
            val key = getKeyNative(type,context)
            if (status == 1) {

                val keyNativeId = if (BuildConfig.FLAVOR == "staging" || !context.config.isRealAds) {
                    BuildConfig.native_test
                } else {
                    key
                }
                InterstitialSingleReqAdManager.isShowingAds = true

                val dialogLoadingInterAds =
                    DialogFragmentLoadingInterAds().onCreateDialog(context)
                dialogLoadingInterAds.show()
                loadNativeAds(context, action = {
                    dialogLoadingInterAds.hide()
                    val dialogFullScreenAdmob = DialogShowNativeFull(
                        context, keyNativeId, isShowAlone = true, action = {
                            InterstitialSingleReqAdManager.isShowingAds = false
                            action.invoke()
                        }, nativeFull = it
                    )
//                    dialogFullScreenAdmob.setNativePreLoad(it)
                    dialogFullScreenAdmob.show()
                }, actionFail = {
                    dialogLoadingInterAds.hide()
                    InterstitialSingleReqAdManager.isShowingAds = false
                    action.invoke()
                }, actionWhenOpen = {}, type = type)
            } else {
                InterstitialSingleReqAdManager.isShowingAds = false
                action.invoke()
            }
        } else {
            InterstitialSingleReqAdManager.isShowingAds = false
            action.invoke()

        }
    }

    private fun getStatusNative(
        type: NativeTypeEnum, context: Context
    ): Int {
        return when (type) {
            NativeTypeEnum.SPLASH -> AdsConfigUtils(context).getStatusConfig(AdsConfigUtils.native_splash)
            NativeTypeEnum.SETTING -> AdsConfigUtils(context).getStatusConfig(AdsConfigUtils.native_setting)
            NativeTypeEnum.FULL_SETTING -> AdsConfigUtils(context).getStatusConfig(AdsConfigUtils.native_full_setting)
            NativeTypeEnum.MEDIUM_CATEGORY -> AdsConfigUtils(context).getStatusConfig(AdsConfigUtils.native_medium_category)
            NativeTypeEnum.OB3 -> AdsConfigUtils(context).getStatusConfig(AdsConfigUtils.native_ob3)
            NativeTypeEnum.CATEGORY_2 -> AdsConfigUtils(context).getStatusConfig(AdsConfigUtils.native_category_2)
            NativeTypeEnum.COLLAPSE -> AdsConfigUtils(context).getStatusConfig(AdsConfigUtils.native_collapse)
            NativeTypeEnum.DRAW -> AdsConfigUtils(context).getStatusConfig(AdsConfigUtils.native_draw)
            NativeTypeEnum.FULL_OB1 -> AdsConfigUtils(context).getStatusConfig(AdsConfigUtils.native_full_ob1)
            NativeTypeEnum.FULL_OB2 -> AdsConfigUtils(context).getStatusConfig(AdsConfigUtils.native_full_ob2)
            NativeTypeEnum.PERMISSION -> AdsConfigUtils(context).getStatusConfig(AdsConfigUtils.native_permission)
            NativeTypeEnum.OB1 -> AdsConfigUtils(context).getStatusConfig(AdsConfigUtils.native_ob1)
            NativeTypeEnum.OB2 -> AdsConfigUtils(context).getStatusConfig(AdsConfigUtils.native_ob2)
            NativeTypeEnum.LANG -> AdsConfigUtils(context).getStatusConfig(AdsConfigUtils.native_lang)
            NativeTypeEnum.FULL_SPLASH -> AdsConfigUtils(context).getStatusConfig(AdsConfigUtils.native_full_splash)
            NativeTypeEnum.FULL_OB3 -> AdsConfigUtils(context).getStatusConfig(AdsConfigUtils.native_full_ob3)
        }
    }

    fun getKeyNative(type: NativeTypeEnum, context: Context): String {
        return if (BuildConfig.FLAVOR == "staging" && !context.config.isRealAds) {
            BuildConfig.native_test
        } else {
            when (type) {
                NativeTypeEnum.SPLASH -> BuildConfig.native_splash
                NativeTypeEnum.SETTING -> BuildConfig.native_setting
                NativeTypeEnum.FULL_SETTING -> BuildConfig.native_full_setting
                NativeTypeEnum.MEDIUM_CATEGORY -> BuildConfig.native_medium_category
                NativeTypeEnum.OB3 -> BuildConfig.native_ob3
                NativeTypeEnum.CATEGORY_2 -> BuildConfig.native_category_2
                NativeTypeEnum.COLLAPSE -> BuildConfig.native_collapse
                NativeTypeEnum.DRAW -> BuildConfig.native_draw
                NativeTypeEnum.FULL_OB1 -> BuildConfig.native_full_ob1
                NativeTypeEnum.FULL_OB2 -> BuildConfig.native_full_ob2
                NativeTypeEnum.PERMISSION -> BuildConfig.native_permission
                NativeTypeEnum.OB1 -> BuildConfig.native_ob1
                NativeTypeEnum.OB2 -> BuildConfig.native_ob2
                NativeTypeEnum.LANG -> BuildConfig.native_lang
                NativeTypeEnum.FULL_SPLASH -> BuildConfig.native_full_splash
                NativeTypeEnum.FULL_OB3 -> BuildConfig.native_full_ob3

            }

        }
    }

    fun loadNativeAds(
        context: Context,
        action: ((nativeAd: NativeAd) -> Unit)?,
        actionFail: (() -> Unit)?,
        actionWhenOpen: (() -> Unit)?,
        type: NativeTypeEnum,
    ) {
        if (AdsConfigUtils(context).getPremium()) {
            actionFail?.invoke()
            return
        }
        val adsStatus = getStatusNative(type, context)
        when (adsStatus) {
            AdsConfigUtils.OFF -> {
                actionFail?.invoke()
                return
            }

            else -> {
                val keyId = getKeyNative(type, context)
                val mNativeAdManager = NativeAdsManager(
                    context,
                    keyId,
                )

                mNativeAdManager.loadAds(onLoadSuccess = { nativeAd ->
                    action?.invoke(nativeAd)
                }, onLoadFail = { ss, nativeAd ->
                    actionFail?.invoke()
                }, actionWhenOpen)
            }
        }


    }

    fun showNativeAds(
        context: Context,
        viewNative: MLBBaseNativeAdView?,
        action: (() -> Unit)? = null,
        actionFail: (() -> Unit)? = null,
        actionWhenOpen: (() -> Unit)? = null,
        type: NativeTypeEnum,
        shouldHide: Boolean = true,
        shouldShowShimmerWhenReload: Boolean = false

    ): NativeAdsManager? {
        val isVip = AdsConfigUtils(context).getPremium()
        if (isVip) {
            viewNative?.hide()
            action?.invoke()
            return null
        }
        val adsStatus = getStatusNative(type, context)
        when (adsStatus) {
            AdsConfigUtils.ADMOB -> {
                return showNativeAdmobAds(
                    context,
                    viewNative,
                    action,
                    actionFail,
                    actionWhenOpen,
                    type,
                    shouldHide,
                    shouldShowShimmerWhenReload
                )
            }

            AdsConfigUtils.OFF -> {
                action?.invoke()
                viewNative?.hide()
                return null
            }
        }
        return null
    }


    private fun showNativeAdmobAds(
        context: Context,
        view: MLBBaseNativeAdView?,
        action: (() -> Unit)? = null,
        actionFail: (() -> Unit)? = null,
        actionWhenOpen: (() -> Unit)? = null,
        type: NativeTypeEnum,
        shouldHide: Boolean = true,
        shouldShowShimmerWhenReload: Boolean = false

    ): NativeAdsManager {
        val keyId = getKeyNative(type, context)

        val mNativeAdManager = NativeAdsManager(
            context,
            keyId,
        )

        view?.let {
            if (shouldShowShimmerWhenReload) {
                if (it.mNativeAd == null) {
                    it.showShimmer(true)
                } else {
                    it.showShimmer(false)
                }
            } else {
                it.showShimmer(true)
            }
            mNativeAdManager.loadAds(onLoadSuccess = { nativeAd ->
                it.setNativeAd(nativeAd)
                it.visibility = View.VISIBLE
                action?.invoke()
                it.showShimmer(false)
                it.setNativeAd(nativeAd)
                it.isVisible = true
            }, onLoadFail = { ss, nativeAd ->
                actionFail?.invoke()
                it.errorShimmer()
                if (shouldHide) {
                    if (nativeAd == null) {
                        it.visibility = View.GONE
                    } else {
                        it.visibility = View.VISIBLE
                    }
                } else {
                    it.visibility = View.INVISIBLE
                }
            }, actionWhenOpen)
        }
        return mNativeAdManager
    }

//    fun showBannerAds(
//        activity: Activity,
//        view: ViewGroup,
//        action: (() -> Unit)? = null,
//        actionFail: (() -> Unit)? = null,
//        actionWhenOpen: (() -> Unit)? = null,
//        isCollapsible: Boolean,
//        typeMain: BannerTypeEnum,
//        isHide: Boolean = true
//    ): AdaptiveBannerManager? {
//        val isVip = AdsConfigUtils(activity).getPremium()
//        if (isVip) {
//            view.hide()
//            action?.invoke()
//            return null
//        }
//        val statusAds = when (typeMain) {
//
//        }
//
//        when (statusAds) {
//            AdsConfigUtils.ADMOB -> {
//                return showBannerAdmobAds(
//                    activity,
//                    view,
//                    action,
//                    actionFail,
//                    actionWhenOpen,
//                    isCollapsible,
//                    typeMain,
//                    isHide
//                )
//            }
//
//            AdsConfigUtils.OFF -> {
//                view.hide()
//                actionFail?.invoke()
//                return null
//            }
//
//            else -> {
//                return null
//            }
//        }
//    }

//    private fun showBannerAdmobAds(
//        activity: Activity,
//        view: ViewGroup,
//        action: (() -> Unit)? = null,
//        actionFail: (() -> Unit)? = null,
//        actionWhenOpen: (() -> Unit)? = null,
//        isCollapsible: Boolean,
//        typeMain: BannerTypeEnum,
//        isHide: Boolean = true,
//        full: Boolean = true
//    ): AdaptiveBannerManager {
//
//        val keyId = if (BuildConfig.FLAVOR == "staging" || !activity.config.isRealAds) {
//            BuildConfig.banner_test
//        } else {
//            when (typeMain) {
//                BannerTypeEnum.SPLASH -> ""
//
//            }
//        }
//
//        val adaptiveBannerManager = AdaptiveBannerManager(activity, keyId, isCollapsible)
//        adaptiveBannerManager.loadBanner(view, onAdLoadFail = {
//            if (isHide) {
//                view.visibility = View.GONE
//            }
//            actionFail?.invoke()
//        }, onAdLoader = {
//            view.visibility = View.VISIBLE
//            action?.invoke()
//        }, onOpened = { actionWhenOpen?.invoke() })
//
//        return adaptiveBannerManager
//
//    }


//    fun showMrecAds(
//        activity: Activity,
//        view: ViewGroup,
//        action: (() -> Unit)? = null,
//        actionFail: (() -> Unit)? = null,
//        typeMain: MrecTypeEnum,
//    ): MrecBannerManager? {
//        val isVip = AdsConfigUtils(activity).getPremium()
//        if (isVip) {
//            view.hide()
//            action?.invoke()
//            return null
//        }
//        val statusAds = when (typeMain) {
//            MrecTypeEnum.SPLASH -> AdsConfigUtils(activity).getStatusConfig(AdsConfigUtils.banner_splash_mrec_status)
//        }
//
//        when (statusAds) {
//            AdsConfigUtils.ADMOB -> {
//                return showMrecAdmobAds(
//                    activity, view, action, actionFail, typeMain
//                )
//            }
//
//
//            AdsConfigUtils.OFF -> {
//                view.hide()
//                actionFail?.invoke()
//                return null
//            }
//
//            else -> {
//                return null
//            }
//        }
//    }

//    private fun showMrecAdmobAds(
//        activity: Activity,
//        view: ViewGroup,
//        action: (() -> Unit)? = null,
//        actionFail: (() -> Unit)? = null,
//        typeMain: MrecTypeEnum,
//    ): MrecBannerManager {
//        val keyId = if (BuildConfig.FLAVOR == "staging" || !activity.config.isRealAds) {
//            BuildConfig.banner_test
//        } else {
//            when (typeMain) {
//                MrecTypeEnum.SPLASH -> ""
//            }
//
//        }
//        val adaptiveBannerManager = MrecBannerManager(activity, keyId)
//
//        adaptiveBannerManager.loadBanner(view, onAdLoadFail = { isLoaded ->
//            if (isLoaded) {
//                view.visibility = View.VISIBLE
//            } else {
//                view.visibility = View.GONE
//            }
//            actionFail?.invoke()
//        }, onAdLoader = {
//            view.visibility = View.VISIBLE
//            action?.invoke()
//        })
//
//        return adaptiveBannerManager
//
//    }

    fun assignValueFromFireBase(context: Context) {
        AdsConfigUtils(context).listKey.forEach {
            val value = FirebaseRemoteConfig.getInstance().getLong(it)
//            if(value == 1L){
//                AdsConfigUtils(context).putValue(it, 0)
//            }
            AdsConfigUtils(context).putValue(it, value.toInt())
//            AdsConfigUtils(context).putValue(it, 0)
        }
        val value = FirebaseRemoteConfig.getInstance().getLong("time_show_ads")
        AdsConfigUtils(context).putValue("time_show_ads", value.toInt())
        val valueTimeMrec = FirebaseRemoteConfig.getInstance().getLong("time_show_banner")
        AdsConfigUtils(context).putValue("time_show_banner", valueTimeMrec.toInt())

        val valueTimeReload = FirebaseRemoteConfig.getInstance().getLong("time_reload_native")
        AdsConfigUtils(context).putValue("time_reload_native", valueTimeReload.toInt())
//        AdsConfigUtils(context).putValue("time_reload_native", 10000)

//        Constants.TIME_OUT = AdsConfigUtils(context).getDefTimeShowAds().toLong()
//        Constants.TIME_OUT = 0
    }
}

//fun pushEvent(context: Context, key: String) {
//    FirebaseAnalytics.getInstance(context).logEvent(key, null)
//}

fun Context.pushEvent(key: String) {
    FirebaseAnalytics.getInstance(this).logEvent(key, null)
}

