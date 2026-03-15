package com.example.arsketch.ui.setting

import android.content.Intent
import com.example.arsketch.R
import com.example.arsketch.common.Constant
import com.example.arsketch.common.ads.AdsCore
import com.example.arsketch.common.ads.InterAdsEnum
import com.example.arsketch.common.ads.NativeTypeEnum
import com.example.arsketch.common.base_component.BaseActivity
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.common.openLink
import com.example.arsketch.databinding.ActivitySettingBinding
import com.example.arsketch.ui.dialog.DialogRateUs
import com.example.arsketch.ui.language.LanguageActivity

class SettingActivity :
    BaseActivity<ActivitySettingBinding>(ActivitySettingBinding::inflate, { true }) {

    override fun initView() {
        changeStatusBarColor(R.color.white)

        binding.btnBack.clickWithDebounce {
            showInterAds({
                finish()
            }, { finish() }, InterAdsEnum.BACK)
        }

        binding.containerLanguage.clickWithDebounce {
            AdsCore.showNativeFull(this, action = {
                val mIntent = Intent(this,LanguageActivity::class.java)
                mIntent.putExtra(Constant.KEY_FROM_MAIN,true)
                startActivity(mIntent)
            }, type = NativeTypeEnum.FULL_SETTING)

        }


        binding.containerShare.clickWithDebounce {
            AdsCore.showNativeFull(this, action = {
                shareApp()
            }, type = NativeTypeEnum.FULL_SETTING)

        }
        binding.containerRate.clickWithDebounce {
            AdsCore.showNativeFull(this, action = {
                rateApp()
            }, type = NativeTypeEnum.FULL_SETTING)
        }


        binding.containerPrivacy.clickWithDebounce {
            AdsCore.showNativeFull(this, action = {
                openLink(Constant.PRIVACY)
            }, type = NativeTypeEnum.FULL_SETTING)

        }
        loadAds()
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(
                Intent.EXTRA_TEXT, Constant.URL_APP
            )
            startActivity(Intent.createChooser(shareIntent, "Choose one"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun rateApp() {
        val dialogRate = DialogRateUs(this, {}, {}).onCreateDialog()
        dialogRate.show()
    }

    private fun loadAds() {
        AdsCore.showNativeAds(this, binding.nativeAdmob, {}, {}, {}, NativeTypeEnum.SETTING)

    }
}