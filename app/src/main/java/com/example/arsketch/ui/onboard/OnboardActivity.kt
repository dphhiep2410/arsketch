package com.example.arsketch.ui.onboard

import android.content.Intent
import android.content.SharedPreferences
import com.example.arsketch.MainApplication
import com.example.arsketch.common.Constant
import com.example.arsketch.common.ads.AdsCore
import com.example.arsketch.common.ads.InterAdsEnum
import com.example.arsketch.common.ads.NativeTypeEnum
import com.example.arsketch.common.base_component.BaseActivity
import com.example.arsketch.common.config
import com.example.arsketch.common.isInternetAvailable
import com.example.arsketch.databinding.ActivityOnboardBinding
import com.example.arsketch.ui.dialog.DialogFragmentLoadingConsent
import com.example.arsketch.ui.main.MainActivity
import com.example.arsketch.ui.onboard.adapter.ViewPagerAdapter
import com.google.android.gms.ads.nativead.NativeAd

class OnboardActivity :
    BaseActivity<ActivityOnboardBinding>(ActivityOnboardBinding::inflate, { true }) {
    private lateinit var viewpagerAdapter: ViewPagerAdapter

    private val cachedNativeAds = mutableMapOf<NativeTypeEnum, NativeAd>()
    private val nativeAdCallbacks = mutableMapOf<NativeTypeEnum, MutableList<(NativeAd) -> Unit>>()

    override fun callbackSharePreference(): SharedPreferences.OnSharedPreferenceChangeListener? {
        return null
    }

    override fun initView() {
        preloadAds()
        initViewPager()
        changeBackPressCallBack {
            if (intent.getBooleanExtra(Constant.KEY_FROM_MAIN, false)) {
                finish()
            }
        }
    }

    private fun preloadAds() {
        val types = listOf(
            NativeTypeEnum.OB1,
            NativeTypeEnum.OB2,
            NativeTypeEnum.OB3,
            NativeTypeEnum.FULL_OB1,
            NativeTypeEnum.FULL_OB2
        )
        for (type in types) {
            AdsCore.loadNativeAds(
                context = this, action = { nativeAd ->
                cachedNativeAds[type] = nativeAd
                nativeAdCallbacks[type]?.forEach { it.invoke(nativeAd) }
                nativeAdCallbacks.remove(type)
            }, actionFail = {
                nativeAdCallbacks.remove(type)
            }, actionWhenOpen = null, type = type
            )
        }
    }

    fun getNativeAd(type: NativeTypeEnum, onResult: (NativeAd?) -> Unit) {
        val cached = cachedNativeAds[type]
        if (cached != null) {
            onResult(cached)
        } else {
            nativeAdCallbacks.getOrPut(type) { mutableListOf() }.add { nativeAd ->
                onResult(nativeAd)
            }
        }
    }

    fun goToNextPage() {
        binding.vpOnboard.currentItem = binding.vpOnboard.currentItem + 1
    }

    fun getViewPager() = binding.vpOnboard

    fun onStartClicked() {
        config.isPassOnboard = true
        showInterAds(
            action = { navigateToMain() },
            actionFailed = { navigateToMain() },
            type = InterAdsEnum.OB3
        )
    }

    private fun navigateToMain() {
//        if (CMPUtils(this@OnboardActivity).canShowAds()) {
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
//        } else {
//        if (isInternetAvailable(this)) {
//            val dialog = DialogFragmentLoadingConsent().onCreateDialog(this)
//            dialog.show()
//            MainApplication.app.adsInit?.showConsentForm(this, onInitSuccess = {
//                startActivity(Intent(this, MainActivity::class.java))
//                finish()
//            }, onInitFailed = {
//                dialog.dismiss()
//                startActivity(Intent(this, MainActivity::class.java))
//                finish()
//            })
//            } else {
//            config.isPassOnboard = true
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
//        }
//        }
        config.isPassOnboard = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun initViewPager() {
        viewpagerAdapter = ViewPagerAdapter(
            supportFragmentManager, lifecycle
        )
        binding.vpOnboard.adapter = viewpagerAdapter
        binding.vpOnboard.isUserInputEnabled = false
    }
}
