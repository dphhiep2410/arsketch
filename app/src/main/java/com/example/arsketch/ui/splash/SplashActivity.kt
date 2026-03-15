package com.example.arsketch.ui.splash

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.dream.libads.admob.InterstitialSingleReqAdManager
import com.dream.libads.utils.AdsConfigUtils
import com.dream.libads.utils.AdsConfigUtils.Companion.ADMOB
import com.dream.libads.utils.Constants
import com.example.arsketch.BuildConfig
import com.example.arsketch.MainApplication
import com.example.arsketch.R
import com.example.arsketch.common.ads.AdsCore
import com.example.arsketch.common.ads.NativeTypeEnum
import com.example.arsketch.common.base_component.BaseActivity
import com.example.arsketch.common.config
import com.example.arsketch.common.isInternetAvailable
import com.example.arsketch.databinding.ActivitySplashBinding
import com.example.arsketch.ui.dialog.DialogShowNativeFull
import com.example.arsketch.ui.language.LanguageActivity
import com.example.arsketch.ui.main.MainActivity
import com.example.arsketch.ui.onboard.OnboardActivity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity :
    BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate, { true }) {
    private var handler: Handler? = Handler()
    private var runnable: Runnable? = null
    private var splashNativeFullDialog: DialogShowNativeFull? = null

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }
    private val updateLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            // Update finished or cancelled, proceed
            proceedAfterUpdateCheck()
        }

    override fun initView() {
        changeBackPressCallBack {}
        checkForUpdate()
    }

    private fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                    AppUpdateType.IMMEDIATE
                )
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            } else {
                proceedAfterUpdateCheck()
            }
        }.addOnFailureListener {
            Log.d("TAG", "checkForUpdate failed: ${it.message}")
            proceedAfterUpdateCheck()
        }
    }

    private fun proceedAfterUpdateCheck() {
        if (isInternetAvailable(this)) {
            MainApplication.app.adsInit?.showConsentForm(this@SplashActivity, onInitSuccess = {
                fetchRemoteConfig()
            }, onInitFailed = {
                fetchRemoteConfig()
            })
        } else {
            lifecycleScope.launch {
                delay(3000)
                navigate()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            }
        }
    }

    private fun navigate() {
        lifecycleScope.launchWhenResumed {
            if (config.isPassOnboard) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                if (!config.isPassLang) {
                    startActivity(Intent(this@SplashActivity, LanguageActivity::class.java))
                } else {
                    startActivity(Intent(this@SplashActivity, OnboardActivity::class.java))
                }
            }

            finish()
        }
    }

    private fun fetchRemoteConfig() {
        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings =
            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(3600).build()
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        handler = Handler()
        runnable = Runnable {
            assignValueToAdsConfig()
            handler = null
        }
        handler?.postDelayed(runnable!!, Constants.TIME_OUT_DELAY)

        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener {
            if (handler == null) return@addOnCompleteListener
            handler?.removeCallbacks(runnable!!)
            handler = null
            assignValueToAdsConfig()
        }
    }

    private fun assignValueToAdsConfig() {
        AdsCore.assignValueFromFireBase(this)
        doWhenConfigLoaded()
    }

    private fun doWhenConfigLoaded() {
        val statusAds = AdsConfigUtils(this).getStatusConfig(AdsConfigUtils.inter_splash)
        if (statusAds != ADMOB || !isInternetAvailable(this)) {
            // Không có inter ads → show native, navigate khi native resolve
            AdsCore.showNativeAds(
                context = this,
                viewNative = binding.nativeView,
                action = { navigate() },
                actionFail = { navigate() },
                actionWhenOpen = {},
                type = NativeTypeEnum.SPLASH
            )
            return
        }

        val keyId = if (BuildConfig.FLAVOR == "staging" || !config.isRealAds) {
            BuildConfig.inter_test
        } else {
            BuildConfig.inter_splash
        }
        val manager = InterstitialSingleReqAdManager(this, keyId)
        InterstitialSingleReqAdManager.isShowingAds = true

        val canShowNative =
            AdsConfigUtils(this).getStatusConfig(AdsConfigUtils.native_full_splash) == ADMOB
        if (canShowNative) {
            val keyNativeId = if (BuildConfig.FLAVOR == "staging" || !config.isRealAds) {
                BuildConfig.native_test
            } else {
                BuildConfig.native_full_splash
            }
            splashNativeFullDialog = DialogShowNativeFull(this, keyNativeId, action = {
                navigate()
                InterstitialSingleReqAdManager.isShowingAds = false
                MainApplication.app.action?.invoke()
                MainApplication.app.action = {}
            })
            splashNativeFullDialog?.loadAdNative()
        }

        // 0 = đang load, 1 = load thành công, 2 = load thất bại
        var interState = 0
        var triggerReady = false

        // Load native ads và inter ads cùng lúc.
        // Callback của native ads quyết định khi nào show inter:
        //   - native thành công → đợi 3s rồi show inter
        //   - native thất bại   → show inter ngay lập tức
        AdsCore.showNativeAds(
            context = this, viewNative = binding.nativeView, action = {
            // Native load thành công → bắt đầu đếm 3s
            lifecycleScope.launch {
                delay(3000)
                triggerReady = true
                with(this@SplashActivity) {
                    when (interState) {
                        1 -> launchWhenResumed { showLoadedInter(manager, canShowNative) }
                        2 -> handleInterFail(canShowNative)
                        // 0: inter vẫn đang load → chờ callback của inter
                    }
                }
            }
        }, actionFail = {
            // Native thất bại → trigger inter ngay, không đợi 3s
            triggerReady = true
            when (interState) {
                1 -> launchWhenResumed { showLoadedInter(manager, canShowNative) }
                2 -> handleInterFail(canShowNative)
                // 0: inter vẫn đang load → chờ callback của inter
            }
        }, actionWhenOpen = {}, type = NativeTypeEnum.SPLASH
        )

        // Load inter ads ngay lập tức, song song với native
        manager.requestAds(onLoadAdSuccess = {
            interState = 1
            // Chỉ show khi trigger đã sẵn sàng (native đã resolve)
            if (triggerReady) {
                launchWhenResumed { showLoadedInter(manager, canShowNative) }
            }
        }, onAdLoadFail = {
            interState = 2
            if (triggerReady) {
                launchWhenResumed { handleInterFail(canShowNative) }
            }
        })
    }

    private fun showLoadedInter(manager: InterstitialSingleReqAdManager, canShowNative: Boolean) {
        manager.show(this@SplashActivity, onShowAdsFinish = {
            MainApplication.app.mLastShowAds = SystemClock.elapsedRealtime()
            if (!canShowNative) {
                InterstitialSingleReqAdManager.isShowingAds = false
                navigate()
                MainApplication.app.action?.invoke()
                MainApplication.app.action = {}
            } else {
                if (splashNativeFullDialog?.statusNative == DialogShowNativeFull.FAIL) {
                    InterstitialSingleReqAdManager.isShowingAds = false
                    navigate()
                    MainApplication.app.action?.invoke()
                    MainApplication.app.action = {}
                } else {
                    splashNativeFullDialog?.startCountDown()
                    splashNativeFullDialog?.interPing(DialogShowNativeFull.INTER_CLOSE)
                }
            }
        }, onAdsShowing = {
            Handler(Looper.getMainLooper()).postDelayed({
                if (canShowNative) {
                    splashNativeFullDialog?.interPing(DialogShowNativeFull.INTER_SHOW_FULL)
                }
            }, 1000)
        })
    }

    private fun handleInterFail(canShowNative: Boolean) {
        if (!canShowNative) {
            InterstitialSingleReqAdManager.isShowingAds = false
            navigate()
            MainApplication.app.action?.invoke()
            MainApplication.app.action = {}
        } else {
            if (splashNativeFullDialog?.statusNative == DialogShowNativeFull.FAIL) {
                InterstitialSingleReqAdManager.isShowingAds = false
                navigate()
                MainApplication.app.action?.invoke()
                MainApplication.app.action = {}
            } else {
                splashNativeFullDialog?.interPing(DialogShowNativeFull.INTER_FAIL)
            }
        }
    }
}
