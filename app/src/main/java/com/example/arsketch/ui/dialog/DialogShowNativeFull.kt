package com.example.arsketch.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.core.view.isGone
import com.dream.libads.admob.InterstitialSingleReqAdManager
import com.dream.libads.admob.NativeAdsManager
import com.dream.libads.utils.AdsConfigUtils
import com.example.arsketch.databinding.DialogShowNativeFullBinding
import com.google.android.gms.ads.nativead.NativeAd

class DialogShowNativeFull(
    private val context: Activity,
    private val idAds: String,
    private val action: () -> Unit,
    private var isShowAlone: Boolean = false,
    private var actionWhenLoaded: (() -> Unit)? = null,
    private var nativeFull: NativeAd ?= null
) {
    var TAG = "TAG"
    private var countDownTimer: CountDownTimer? = null

    companion object {
        var isShowing = false
        const val LOADING = 0
        const val SUCCESS = 1
        const val FAIL = 2
        const val SHOWING = 3

        const val INTER_SHOW_FULL = 4
        const val INTER_FAIL = 5
        const val INTER_CLOSE = 6
    }

    private val binding by lazy {
        DialogShowNativeFullBinding.inflate(LayoutInflater.from(context))
    }

    private val dialog: Dialog by lazy {
        Dialog(context)
    }

    var statusNative = -1
    private var statusInter = -1

    init {
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)

        dialog.window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )

        if(nativeFull != null){
            setNativePreLoad(nativeFull!!)
        }

    }

    fun isShowing(): Boolean {
        return dialog.isShowing
    }

    fun hide() {
        countDownTimer?.cancel()
        countDownTimer = null
        dialog.window?.decorView?.post {
            if (dialog.isShowing) {
//                AppLog.d(TAG, "hide: ")
                isShowing = false
                dialog.dismiss()
            }
        }

    }

    fun setNativePreLoad(nativeAd: NativeAd) {
        if (!context.isFinishing) {
            binding.nativeFullScreenV2.showShimmer(false)
            binding.nativeFullScreenV2.setNativeAd(nativeAd)
            onLoadSuccess()
//                AppLog.d(TAG, "setNativePreLoad: 1")
            binding.nativeFullScreenV2.initCollapseEvent(onCloseAdsAction = {
                hide()
                action.invoke()
                InterstitialSingleReqAdManager.isShowingAds = false
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun loadAdNative(configStatus: String? = null) {
        if (configStatus == null) {
            if (AdsConfigUtils(context).getStatusConfig(AdsConfigUtils.native_full_status) != AdsConfigUtils.ADMOB) {
                onLoadFail()
            } else {
                doLoad()
            }
        } else {
            if (AdsConfigUtils(context).getStatusConfig(configStatus) != AdsConfigUtils.ADMOB) {
                onLoadFail()
            } else {
                doLoad()
            }
        }


    }

    fun doLoad() {
        statusNative = LOADING

        val nativeAdsManager = NativeAdsManager(context, idAds, shouldReloadAfterTouch = false)

        binding.nativeFullScreenV2.showShimmer(true)
        nativeAdsManager.loadAds(onLoadSuccess = {
            if (!context.isFinishing) {
                binding.nativeFullScreenV2.showShimmer(false)
                binding.nativeFullScreenV2.setNativeAd(it)
//                    AppLog.d(TAG, "setNativePreLoad: x")

                binding.nativeFullScreenV2.initCollapseEvent(onCloseAdsAction = {
                    hide()
                    action.invoke()
                    InterstitialSingleReqAdManager.isShowingAds = false
                })
                onLoadSuccess()
            }
        }, onLoadFail = { success, _ ->
            if (!context.isFinishing) {
                binding.nativeFullScreenV2.hideAdsAndShimmer()
                binding.nativeFullScreenV2.isGone = true
                onLoadFail()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    fun show() {
        if (!context.isFinishing) {
            dialog.setCancelable(false)
            if (!dialog.isShowing) {
//                AppLog.d(TAG, "show: ")
                isShowing = true
                dialog.show()
            }
        }
    }

    fun interPing(state: Int) {

        statusInter = state

        when (statusInter) {
            INTER_SHOW_FULL -> {
                if (statusNative != FAIL) {
                    show()
                }
            }

            INTER_FAIL -> {
                if (statusNative == FAIL) {
//                    AppLog.d(TAG, "interPing: 666")
                    hide()
                    action.invoke()
                    InterstitialSingleReqAdManager.isShowingAds = false

                } else if (statusNative == SUCCESS) {
                    if (dialog.isShowing) {
//                        AppLog.d(TAG, "interPing: 7777")
                    } else {
//                        AppLog.d(TAG, "interPing: 888")
                        show()
                    }
                } else {
//                    AppLog.d(TAG, "interPing: 999")
                    show()
                }
            }

            INTER_CLOSE -> {
                if (statusNative == LOADING) {
//                    AppLog.d(TAG, "interPing: 111")
                    show()
                } else {
                    if (statusNative == FAIL) {
//                        AppLog.d(TAG, "interPing: 222")
                        hide()
                        action.invoke()
                        InterstitialSingleReqAdManager.isShowingAds = false
                    } else if (statusNative == SUCCESS) {
//                        AppLog.d(TAG, "interPing: 333")

                    }
                }
            }
        }
    }

    private fun onLoadSuccess() {
        statusNative = SUCCESS
        if (statusInter == INTER_CLOSE || statusInter == INTER_FAIL) {

        }
    }
    fun startCountDown() {
        if (countDownTimer != null) return  // tránh start 2 lần
        val btnClose = binding.nativeFullScreenV2.mViewBinding.btnHideNativeCollapse
        btnClose.visibility = View.INVISIBLE
        binding.tvCountDown.visibility = View.VISIBLE
        var count = 3
        binding.tvCountDown.text = count.toString()
        countDownTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                count--
                binding.tvCountDown.text = count.toString()
            }
            override fun onFinish() {
                binding.tvCountDown.visibility = View.GONE
                btnClose.visibility = View.VISIBLE
            }
        }.start()
    }
    private fun onLoadFail() {
        statusNative = FAIL
        hide()
        if (statusInter == INTER_CLOSE || statusInter == INTER_FAIL || isShowAlone) {
//            AppLog.d(TAG, "onLoadFail: action")
            action.invoke()
            InterstitialSingleReqAdManager.isShowingAds = false
        }
    }
}