package com.example.arsketch

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import com.dream.libads.admob.AppOpenResumeAdManager
import com.dream.libads.admob.InterstitialSingleReqAdManager
import com.dream.libads.admob.RewardAdsManager
import com.dream.libads.utils.AdsConfigUtils
import com.dream.libads.utils.AdsInit
import com.dream.libads.utils.CMPUtils
import com.dream.libads.utils.Constants
import com.example.arsketch.common.AgeSignalsHelper
import com.example.arsketch.common.AppSharePreference
import com.example.arsketch.common.Constant
import com.example.arsketch.common.DownloadManagerApp
import com.example.arsketch.common.ads.ControlAdsActivity
import com.example.arsketch.common.config.MainConfig
import com.example.arsketch.common.isInternetAvailable
import com.example.arsketch.ui.dialog.DialogLoadOpenAds
import com.example.arsketch.ui.splash.SplashActivity
import com.google.android.gms.ads.AdActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

class MainApplication : Application() {
    var isSketch = true
    var isChallenge = false
    var adsInit: AdsInit? = null
    private var currentActivity: Activity? = null
    var mLastShowAds = 0L
    private var appOpenAdsManager: AppOpenResumeAdManager? = null
    var dataSource = MutableLiveData<Map<String, List<String>>>()
    var canShowOpenAds = true
    var liveDataShowOpenAds = MutableLiveData(false)
    var action :(()->Unit) ?= null

    companion object {
        lateinit var app: MainApplication
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        AppSharePreference.getInstance(applicationContext)
        MainConfig.newInstance(applicationContext)
        DownloadManagerApp.getInstance(applicationContext)

        adsInit = AdsInit(this)
        adsInit?.initialize()
        initOpenAds()
        checkAgeSignals()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val request: Request = Request.Builder()
                    .url(Constant.keyData)
                    .build()
                val responses: Response = client.newCall(request).execute()
//                val apiResponse = URL(Constant.keyData).readText()
//                Log.d("TAG", "getListDataSource: "+apiResponse)
                dataSource.postValue(
                    jsonStringToMap(
                        responses.body?.string().toString()
                    ).toMutableMap()
                )

            } catch (e: Exception) {
                e.printStackTrace()
                dataSource.postValue(mutableMapOf())
            }

        }
        val lifecycleEventObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (currentActivity !is SplashActivity && currentActivity !is ControlAdsActivity && !RewardAdsManager.isShowing && !InterstitialSingleReqAdManager.isShowingAds && !AppOpenResumeAdManager.isShowingAd) {
                        if (canShowOpenAds) {
                            liveDataShowOpenAds.postValue(true)
                        }
                    }

                }

                Lifecycle.Event.ON_RESUME -> {
                    mLastShowAds = System.currentTimeMillis() - 800
                }

                Lifecycle.Event.ON_PAUSE -> {

                }

                Lifecycle.Event.ON_STOP -> {


                }

                Lifecycle.Event.ON_CREATE -> {

                }

                Lifecycle.Event.ON_DESTROY -> {


                }

                else -> {

                }
            }
        }
        registerActivityLifecycleCallbacks(AppLifecycleCallbacks())
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleEventObserver)
    }

    private fun jsonStringToMap(jsonString: String): Map<String, List<String>> {
        try {
            val jsonObject = JSONObject(jsonString)
            val map = mutableMapOf<String, List<String>>()

            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = jsonObject.get(key)

                if (value is JSONArray) {
                    val list = mutableListOf<String>()
                    for (i in 0 until value.length() - 1) {
                        list.add(value.getString(i))
                    }
                    map[key] = list
                }
            }
            return map
        } catch (e: Exception) {
            e.printStackTrace()
            return mutableMapOf()
        }

    }

    private fun checkAgeSignals() {
        AgeSignalsHelper.checkAgeSignals(this) { isMinor ->
            if (isMinor) {
                // Disable personalized ads for minors (Brazil Digital ECA compliance)
                canShowOpenAds = false
            }
        }
    }

    private fun initOpenAds() {
        appOpenAdsManager = AppOpenResumeAdManager(
            this, BuildConfig.open_resume,
        )
    }

    fun checkCanShowAds(): Boolean {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - mLastShowAds

        return if (elapsedTime < Constants.TIME_OUT) {
            false
        } else {
            mLastShowAds = currentTime
            true
        }
    }

    inner class AppLifecycleCallbacks : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {
            currentActivity = activity
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityPaused(activity: Activity) {

        }

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {}
    }

}