package com.example.arsketch.common.base_component

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock.elapsedRealtime
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.doOnAttach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.dream.libads.admob.AppOpenResumeAdManager
import com.dream.libads.admob.InterstitialSingleReqAdManager
import com.dream.libads.admob.RewardAdsManager
import com.dream.libads.utils.AdsConfigUtils
import com.dream.libads.utils.AdsConfigUtils.Companion.ADMOB
import com.dream.libads.utils.DialogLoadingAds
import com.example.arsketch.BuildConfig
import com.example.arsketch.MainApplication
import com.example.arsketch.R
import com.example.arsketch.common.AgeSignalsHelper
import com.example.arsketch.common.AppSharePreference
import com.example.arsketch.common.ads.AdsCore
import com.example.arsketch.common.ads.InterAdsEnum
import com.example.arsketch.common.ads.RewardTypeEnum
import com.example.arsketch.common.config
import com.example.arsketch.common.createContext
import com.example.arsketch.common.isInternetAvailable
import com.example.arsketch.common.toPx
import com.example.arsketch.ui.dialog.DialogFragmentLoadingInterAds
import com.example.arsketch.ui.dialog.DialogShowNativeFull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Locale

abstract class BaseActivity<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater) -> VB, private val shouldPadding: () -> Boolean
) : AppCompatActivity() {
    lateinit var binding: VB
    private var dialogFullScreenAdmob: DialogShowNativeFull? = null
    private val dialogFragmentLoadingOpenAds by lazy {
        DialogLoadingAds(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        binding = bindingInflater.invoke(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        hideNavigationBar()
        initView()
        callbackSharePreference()?.let { callback ->
            AppSharePreference.INSTANCE.registerOnSharedPreferenceChangeListener(callback)
        }
        binding.root.doOnAttach {
            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
                val statusBarResId =
                    resources.getIdentifier("status_bar_height", "dimen", "android")
//                val statusBarHeight = if (shouldPadding.invoke()) {
//                    if (statusBarResId > 0) resources.getDimensionPixelSize(statusBarResId) else toPx(
//                        24
//                    ).toInt()
//                } else {
//                    0
//                }

                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                val imeHeight = if (insets.isVisible(ime())) insets.getInsets(ime()).bottom else 0
                v.setPadding(
                    systemBars.left, systemBars.top, systemBars.right, systemBars.bottom
                )
                insets
            }

        }
        MainApplication.app.liveDataShowOpenAds.observe(this) {
            if (it) {
                if (MainApplication.app.canShowOpenAds) {
                    try {
                        MainApplication.app.liveDataShowOpenAds.postValue(false)
                        launchWhenResumed {
                            showOpenAdsResume()
                        }
                    } catch (e: Exception) {

                    }
                }

            }
        }
    }

    open fun callbackSharePreference(): SharedPreferences.OnSharedPreferenceChangeListener? {
        return null
    }

    protected fun changeBackPressCallBack(action: () -> Unit) {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                action.invoke()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
            val uiMode = overrideConfiguration.uiMode
            overrideConfiguration.setTo(baseContext.resources.configuration)
            overrideConfiguration.uiMode = uiMode
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    override fun attachBaseContext(newBase: Context) = super.attachBaseContext(
        newBase.createContext(
            Locale(config.savedLanguage)
        )
    )

    protected fun changeStatusBarColor(color: Int) {
        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, color)
    }

    protected fun showInterAds(
        action: () -> Unit,
        actionFailed: () -> Unit,
        type: InterAdsEnum,
        mustWait: Boolean = true,
        showLoading: Boolean = true,
        showNative: Boolean = true
    ) {
        // Brazil Digital ECA: skip ads for minors
        if (AgeSignalsHelper.isMinor(this)) {
            action.invoke()
            return
        }
        if (!MainApplication.app.checkCanShowAds() && mustWait) {
            actionFailed.invoke()
            return
        }
        val statusAds = when (type) {
            InterAdsEnum.SPLASH -> AdsConfigUtils(this).getStatusConfig(AdsConfigUtils.inter_splash)
            InterAdsEnum.BACK -> AdsConfigUtils(this).getStatusConfig(AdsConfigUtils.inter_back)
            InterAdsEnum.DRAW -> AdsConfigUtils(this).getStatusConfig(AdsConfigUtils.inter_draw)
            InterAdsEnum.OB3 -> AdsConfigUtils(this).getStatusConfig(AdsConfigUtils.inter_ob3)
            InterAdsEnum.HOME -> AdsConfigUtils(this).getStatusConfig(AdsConfigUtils.inter_home)


        }
        when (statusAds) {
            AdsConfigUtils.OFF -> {
                Log.d("TAG", "showInterAds: OFF")
                action.invoke()
            }

            ADMOB -> {
                Log.d("TAG", "showInterAds: ADMOB")
                showAdmobInterAds(action, actionFailed, type, showLoading, showNative)
            }
        }
    }


    private fun showAdmobInterAds(
        action: () -> Unit,
        actionFailed: () -> Unit,
        type: InterAdsEnum,
        showLoading: Boolean = true,
        shouldShowNative: Boolean = true
    ) {
        launchWhenResumed {
            if (!isInternetAvailable(this@BaseActivity)) {
                actionFailed.invoke()
                return@launchWhenResumed
            }

            val keyId = if (BuildConfig.FLAVOR == "staging" || !config.isRealAds) {
                BuildConfig.inter_test
            } else {
                when (type) {
                    InterAdsEnum.SPLASH -> BuildConfig.inter_splash
                    InterAdsEnum.BACK -> BuildConfig.inter_back
                    InterAdsEnum.DRAW -> BuildConfig.inter_draw
                    InterAdsEnum.HOME -> BuildConfig.inter_home
                    InterAdsEnum.OB3 -> BuildConfig.inter_ob3
                }
            }

            val interstitialSingleReqAdManager = InterstitialSingleReqAdManager(
                this@BaseActivity,
                keyId,
            )
            InterstitialSingleReqAdManager.isShowingAds = true
            val dialogLoadingInterAds =
                DialogFragmentLoadingInterAds().onCreateDialog(this@BaseActivity)
            if (showLoading) {
                dialogLoadingInterAds.show()
            }
            val canShowNative = AdsConfigUtils(this@BaseActivity).getStatusConfig(
                AdsConfigUtils.native_full_status
            ) == ADMOB

            if (canShowNative) {
                val keyNativeId = if (BuildConfig.FLAVOR == "staging" || !config.isRealAds) {
                    BuildConfig.native_test
                } else {
                    ""
                }
                dialogFullScreenAdmob = DialogShowNativeFull(
                    this@BaseActivity, keyNativeId, action = {

                        action.invoke()
                        InterstitialSingleReqAdManager.isShowingAds = false
                        MainApplication.app.action?.invoke()
                        MainApplication.app.action = {}

                    })
                dialogFullScreenAdmob?.loadAdNative()
            }

            interstitialSingleReqAdManager.requestAds(onLoadAdSuccess = {
                if (showLoading && !isFinishing && !isDestroyed) {
                    dialogLoadingInterAds.dismiss()
                }
                launchWhenResumed {
                    interstitialSingleReqAdManager.show(this@BaseActivity, onShowAdsFinish = {
//                        AppLog.d("TAG", "onShowAdsFinish: ")
                        MainApplication.app.mLastShowAds = elapsedRealtime()
                        if (!canShowNative) {
                            InterstitialSingleReqAdManager.isShowingAds = false
                            action()
                            MainApplication.app.action?.invoke()
                            MainApplication.app.action = {}
                        } else {
                            if (dialogFullScreenAdmob?.statusNative == DialogShowNativeFull.FAIL) {
                                InterstitialSingleReqAdManager.isShowingAds = false
                                action.invoke()
                                MainApplication.app.action?.invoke()
                                MainApplication.app.action = {}
                            } else {
                                dialogFullScreenAdmob?.interPing(DialogShowNativeFull.INTER_CLOSE)
                            }
                        }
                    }, onAdsShowing = {
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (canShowNative) {
                                dialogFullScreenAdmob?.interPing(DialogShowNativeFull.INTER_SHOW_FULL)
                            }
                        }, 1000)

                    })
                }
            }, onAdLoadFail = {
                launchWhenResumed {
                    if (!canShowNative) {
                        actionFailed()
                        InterstitialSingleReqAdManager.isShowingAds = false
                    } else {
                        if (dialogFullScreenAdmob?.statusNative == DialogShowNativeFull.FAIL) {
                            InterstitialSingleReqAdManager.isShowingAds = false
                            action.invoke()
                            MainApplication.app.action?.invoke()
                            MainApplication.app.action = {}
                        } else {
                            dialogFullScreenAdmob?.interPing(DialogShowNativeFull.INTER_FAIL)
                        }
                    }
                    if (showLoading && !isFinishing && !isDestroyed) {
                        dialogLoadingInterAds.dismiss()
                    }
                }

            })
        }

    }

    protected fun toast(res: Int) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.layout_toast, null)
        val toast = Toast.makeText(
            this@BaseActivity, res, Toast.LENGTH_SHORT
        )
        toast.view = layout
        val textview: TextView = layout.findViewById(R.id.tvToast)
        textview.text = getString(res)
        toast.setGravity(Gravity.CENTER or Gravity.CENTER_HORIZONTAL, 0, 0)
        toast.show()
        val handler = Handler()
        handler.postDelayed({ toast.cancel() }, 1400)
    }

//    fun showRewardAds(
//        action: () -> Unit, actionFailed: () -> Unit, type: RewardTypeEnum
//    ) {
//        launchWhenResumed {
//            if (!isInternetAvailable(this@BaseActivity)) {
//                actionFailed.invoke()
//                return@launchWhenResumed
//            }
//            if (type == RewardTypeEnum.RECORD){
//                if(AdsConfigUtils(this@BaseActivity).getRewardRecord() == AdsConfigUtils.OFF){
//                    action.invoke()
//                    return@launchWhenResumed
//                }
//            }
//
//            if (type == RewardTypeEnum.DOWNLOAD){
//                if(AdsConfigUtils(this@BaseActivity).getRewardDownload() == AdsConfigUtils.OFF){
//                    action.invoke()
//                    return@launchWhenResumed
//                }
//            }
//
//            val rewardAdsManager: RewardAdsManager = when (type) {
//                RewardTypeEnum.RECORD -> {
//                    RewardAdsManager(
//                        this@BaseActivity,
//                        BuildConfig.reward_record,
//                    )
//                }
//
//                RewardTypeEnum.DOWNLOAD -> {
//                    RewardAdsManager(
//                        this@BaseActivity,
//                        BuildConfig.reward_download,
//                    )
//                }
//            }
//            RewardAdsManager.isShowing = true
//            val dialogLoadingInterAds =
//                DialogFragmentLoadingInterAds().onCreateDialog(this@BaseActivity)
//            dialogLoadingInterAds.show()
//            rewardAdsManager.loadAds(onAdLoader = {
//                dialogLoadingInterAds.dismiss()
//                launchWhenResumed {
//                    rewardAdsManager.showAds(
//                        this@BaseActivity,
//                        onAdClose = {
//                            InterstitialSingleReqAdManager.isShowingAds = false
//                            action()
//                        },
//                        onActionDoneWhenAdsNotComplete = {}, onAdLoadFail = {},
//                    )
//                }
//            }, onAdLoadFail = {
//                RewardAdsManager.isShowing = false
//                actionFailed()
//                dialogLoadingInterAds.dismiss()
//            })
//        }
//    }
//    private fun showOpenAds() {
//        if (InterstitialSingleReqAdManager.isShowingAds) {
//            return
//        }
//        if (AdsConfigUtils(this).getStatusConfig(AdsConfigUtils.inter_resume_status) == ADMOB) {
//            dialogFragmentLoadingOpenAds.showDialogLoading()
//            InterstitialSingleReqAdManager.isShowingAds = true
//            val canShowNative = AdsConfigUtils(this@BaseActivity).getStatusConfig(
//                AdsConfigUtils.native_full_status) == ADMOB
//            if (AdsConfigUtils(this).getStatusConfig(AdsConfigUtils.native_full_status) == ADMOB) {
//
//                val keyNativeFull = if (BuildConfig.FLAVOR == "adsTest") {
//                    AdsCore.native_test_id
//                }else{
//                    BuildConfig.native_full_id
//                }
//
//
//                dialogFullScreenAdmob = DialogShowNativeFull(
//                    this,keyNativeFull
//                ) {
//                    InterstitialSingleReqAdManager.isShowingAds = false
//                    MainApplication.app.liveDataShowOpenAds.postValue(false)
//                    MainApplication.app.action?.invoke()
//                }
//                dialogFullScreenAdmob?.loadAdNative()
//
//            }
//            val keyId = if (BuildConfig.FLAVOR == "adsTest") {
//                BuildConfig.inter_test_id
//            } else {
//                BuildConfig.inter_resume_id
//            }
//            val interResume = InterstitialSingleReqAdManager(this, keyId)
//            interResume.requestAds(onLoadAdSuccess = {
//                dialogFragmentLoadingOpenAds.dismissDialog()
//                lifecycleScope.launchWhenResumed {
//                    interResume.show(this@BaseActivity, onShowAdsFinish = {
//                        if(canShowNative){
//                            if (dialogFullScreenAdmob?.statusNative == DialogShowNativeFull.FAIL) {
//                                InterstitialSingleReqAdManager.isShowingAds = false
//                            } else {
//                                dialogFullScreenAdmob?.interPing(DialogShowNativeFull.INTER_CLOSE)
//                            }
//                        }else{
//                            InterstitialSingleReqAdManager.isShowingAds = false
//                        }
//
//                    }, onAdsShowing = {
//                        dialogFullScreenAdmob?.interPing(DialogShowNativeFull.INTER_SHOW_FULL)
//                    })
//                }
//
//
//            }, onAdLoadFail = {
//                dialogFragmentLoadingOpenAds?.dismissDialog()
//                if(dialogFullScreenAdmob?.statusNative == DialogShowNativeFull.FAIL){
//                    InterstitialSingleReqAdManager.isShowingAds = false
//                }else{
//                    dialogFullScreenAdmob?.interPing(DialogShowNativeFull.INTER_FAIL)
//                }
//            })
//
//        }else{
//            InterstitialSingleReqAdManager.isShowingAds = false
//            MainApplication.app.liveDataShowOpenAds.postValue(false)
//            MainApplication.app.action?.invoke()
//        }
//    }

    private fun hideNavigationBar() {
        val decorView: View = window.decorView

        val uiOptions: Int =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//
//            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        }
        decorView.systemUiVisibility = uiOptions
    }
    protected fun showOpenAdsResume(action: (() -> Unit)? = null) {
        // Brazil Digital ECA: skip ads for minors
        if (AgeSignalsHelper.isMinor(this)) {
            action?.invoke()
            return
        }
        if (AppOpenResumeAdManager.isShowingAd) {
            return
        }

        if (AdsConfigUtils(this).getStatusConfig(AdsConfigUtils.open_resume) == ADMOB) {

            val dialogLoadingInterAds1 =
                DialogFragmentLoadingInterAds().onCreateDialog(this@BaseActivity)
            dialogLoadingInterAds1.show()

            val keyId = if (BuildConfig.FLAVOR == "staging" || !config.isRealAds) {
                BuildConfig.open_test
            } else {
                BuildConfig.open_resume
            }
            val interResume = AppOpenResumeAdManager(
                this@BaseActivity, keyId
            )

//            val canShowNative = AdsConfigUtils(this@BaseActivity).getStatusConfig(
//                AdsConfigUtils.native_full_status
//            ) == ADMOB
            val canShowNative = false

            if (canShowNative) {
                val keyNativeId = if (BuildConfig.FLAVOR == "staging" || !config.isRealAds) {
                    BuildConfig.native_test
                } else {
                    BuildConfig.native_full_splash
                }

                dialogFullScreenAdmob = DialogShowNativeFull(
                    this@BaseActivity, keyNativeId, action = {
                        action?.invoke()
                        AppOpenResumeAdManager.isShowingAd = false
                        MainApplication.app.action?.invoke()
                        MainApplication.app.action = {}
                    })
                dialogFullScreenAdmob?.loadAdNative()
            }

            interResume.loadAd(onAdLoader = {
                if(dialogLoadingInterAds1.isShowing && !isFinishing && !isDestroyed){
                    dialogLoadingInterAds1.dismiss()
                }
                interResume.showAdIfAvailable(this, action = {
                    AppOpenResumeAdManager.isShowingAd = false
                    if (!canShowNative) {
                        InterstitialSingleReqAdManager.isShowingAds = false
                        action?.invoke()
                        MainApplication.app.action?.invoke()
                        MainApplication.app.action = {}
                    } else {
                        if (dialogFullScreenAdmob?.statusNative == DialogShowNativeFull.FAIL) {
                            AppOpenResumeAdManager.isShowingAd = false
                            action?.invoke()
                            MainApplication.app.action?.invoke()
                            MainApplication.app.action = {}
                        } else {
                            dialogFullScreenAdmob?.interPing(DialogShowNativeFull.INTER_CLOSE)
                        }
                    }
                }, actionWhenShow = {
                    if (canShowNative) {
                        dialogFullScreenAdmob?.interPing(DialogShowNativeFull.INTER_SHOW_FULL)
                    }
                })
            }, onAdLoadFail = {
                if(dialogLoadingInterAds1.isShowing && !isFinishing && !isDestroyed){
                    dialogLoadingInterAds1.dismiss()
                }
                launchWhenResumed {
                    if (!canShowNative) {
                        AppOpenResumeAdManager.isShowingAd = false
                        MainApplication.app.action?.invoke()
                        action?.invoke()
                    } else {
                        if (dialogFullScreenAdmob?.statusNative == DialogShowNativeFull.FAIL) {
                            AppOpenResumeAdManager.isShowingAd = false
                            action?.invoke()
                            MainApplication.app.action?.invoke()
                            MainApplication.app.action = {}
                        } else {
                            dialogFullScreenAdmob?.interPing(DialogShowNativeFull.INTER_FAIL)
                        }
                    }

                }
            }, 1)

        }else{
            action?.invoke()
        }
    }

    abstract fun initView()

    fun LifecycleOwner.launchWhenResumed(block: suspend CoroutineScope.() -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                block()
                this@launch.cancel()
            }
        }

    }
}