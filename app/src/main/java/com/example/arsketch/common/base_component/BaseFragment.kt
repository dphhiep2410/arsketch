package com.example.arsketch.common.base_component

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock.elapsedRealtime
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withStarted
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.dream.libads.admob.InterstitialSingleReqAdManager
import com.dream.libads.utils.AdsConfigUtils
import com.dream.libads.utils.AdsConfigUtils.Companion.ADMOB
import com.example.arsketch.BuildConfig
import com.example.arsketch.MainApplication
import com.example.arsketch.common.AppSharePreference
import com.example.arsketch.common.ads.InterAdsEnum
import com.example.arsketch.common.config
import com.example.arsketch.common.isInternetAvailable
import com.example.arsketch.ui.dialog.DialogFragmentLoadingInterAds
import com.example.arsketch.ui.dialog.DialogShowNativeFull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    lateinit var binding: VB
    private var dialogFullScreenAdmob: DialogShowNativeFull? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = getViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        callbackSharePreference()?.let { callback ->
            AppSharePreference.INSTANCE.registerOnSharedPreferenceChangeListener(callback)
        }
    }

    abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    abstract fun initView()

    open fun callbackSharePreference(): SharedPreferences.OnSharedPreferenceChangeListener? {
        return null
    }


//    protected fun navigateUsingActivityController(resId: Int, bundle: Bundle? = null) {
//        try {
//            lifecycleScope.launch {
//                withStarted {
//                    if (activity is MainActivity) {
//                        (activity as MainActivity).getNavController()?.navigate(resId, bundle)
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
protected fun showInterAds(
    action: () -> Unit,
    actionFailed: () -> Unit,
    type: InterAdsEnum,
    mustWait: Boolean = true,
    showLoading: Boolean = true,
    showNative: Boolean = true
) {
    val isVip = AdsConfigUtils(requireContext()).getPremium()
    if (isVip) {
        action.invoke()
        return
    }
    if (!MainApplication.app.checkCanShowAds() && mustWait) {
        actionFailed.invoke()
        return
    }
    val statusAds = when (type) {
        InterAdsEnum.SPLASH -> AdsConfigUtils(requireContext()).getStatusConfig(AdsConfigUtils.inter_splash)
        InterAdsEnum.BACK -> AdsConfigUtils(requireContext()).getStatusConfig(AdsConfigUtils.inter_back)
        InterAdsEnum.DRAW -> AdsConfigUtils(requireContext()).getStatusConfig(AdsConfigUtils.inter_draw)
        InterAdsEnum.OB3 -> AdsConfigUtils(requireContext()).getStatusConfig(AdsConfigUtils.inter_ob3)
        InterAdsEnum.HOME -> AdsConfigUtils(requireContext()).getStatusConfig(AdsConfigUtils.inter_home)


    }
    when (statusAds) {
        AdsConfigUtils.OFF -> {
            action.invoke()
        }

        ADMOB -> {
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
            if (!isInternetAvailable(requireContext())) {
                actionFailed.invoke()
                return@launchWhenResumed
            }

            val keyId = if (BuildConfig.FLAVOR == "staging" || !requireContext().config.isRealAds) {
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
                requireContext(),
                keyId,
            )
            InterstitialSingleReqAdManager.isShowingAds = true
            val dialogLoadingInterAds =
                DialogFragmentLoadingInterAds().onCreateDialog(requireContext())
            if (showLoading) {
                dialogLoadingInterAds.show()
            }
            val canShowNative = AdsConfigUtils(requireContext()).getStatusConfig(
                AdsConfigUtils.native_full_status
            ) == ADMOB

            if (canShowNative) {
                val keyNativeId = if (BuildConfig.FLAVOR == "staging" || !requireContext().config.isRealAds) {
                    BuildConfig.native_test
                } else {
                    ""
                }
                dialogFullScreenAdmob = DialogShowNativeFull(
                    requireActivity(), keyNativeId, action = {

                        action.invoke()
                        InterstitialSingleReqAdManager.isShowingAds = false
                        MainApplication.app.action?.invoke()
                        MainApplication.app.action = {}

                    })
                dialogFullScreenAdmob?.loadAdNative()
            }

            interstitialSingleReqAdManager.requestAds(onLoadAdSuccess = {
                if (showLoading) {
                    dialogLoadingInterAds.dismiss()
                }
                launchWhenResumed {
                    interstitialSingleReqAdManager.show(requireActivity(), onShowAdsFinish = {
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
                    if (showLoading) {
                        dialogLoadingInterAds.dismiss()
                    }
                }

            })
        }

    }

    protected fun navigateToFragment(fragmentId: Int, actionId: Int, bundle: Bundle? = null) {
        if (fragmentId == findNavController().currentDestination?.id) {
            lifecycleScope.launch {
                withStarted {
                    findNavController().navigate(actionId, bundle)
                }
            }
        }
    }

    protected fun navigateToFragment(id: Int, action: NavDirections) {
        if (findNavController().currentDestination?.id == id) {
            lifecycleScope.launch {
                withStarted {
                    findNavController().navigate(action)
                }
            }
        }
    }

    protected fun navigateBack(id: Int) {
        if (findNavController().currentDestination?.id == id) {
            lifecycleScope.launch {
                withStarted {
                    findNavController().popBackStack()
                }
            }
        }
    }

//    protected fun navigateBackUsingActivityController() {
//        lifecycleScope.launch {
//            withStarted {
//                if (activity is MainActivity) {
//                    (activity as MainActivity).getNavController()?.popBackStack()
//                }
//            }
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        callbackSharePreference()?.let { AppSharePreference.INSTANCE.unregisterListener(it) }
    }

    protected fun changeBackPressCallBack(action: () -> Unit) {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                action.invoke()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }
    fun LifecycleOwner.launchWhenResumed(block: suspend CoroutineScope.() -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                block()
                this@launch.cancel()
            }
        }

    }
}