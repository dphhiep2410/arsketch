package com.example.arsketch.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import com.example.arsketch.MainApplication
import com.example.arsketch.R
import com.example.arsketch.common.Constant
import com.example.arsketch.common.ads.AdsCore
import com.example.arsketch.common.ads.InterAdsEnum
import com.example.arsketch.common.ads.NativeTypeEnum
import com.example.arsketch.common.base_component.BaseActivity
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.databinding.ActivityMainBinding
import com.example.arsketch.ui.draw.DrawActivity
import com.example.arsketch.ui.guide.GuideActivity
import com.example.arsketch.ui.setting.SettingActivity
import com.example.arsketch.ui.sketch.RequestPermissionActivity
import com.example.arsketch.ui.sketch.SketchCategoryActivity

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate, { true }) {


    override fun initView() {
        changeStatusBarColor(R.color.n6)
        changeBackPressCallBack { }
        binding.containerSketch.clickWithDebounce {
            showInterAds(action = {

                MainApplication.app.isSketch = true
                MainApplication.app.isChallenge = false

                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    val mIntent = Intent(this, RequestPermissionActivity::class.java)
                    mIntent.putExtra(Constant.KEY_IS_CAMERA, true)
                    startActivity(mIntent)
                } else {
                    startActivity(Intent(this, SketchCategoryActivity::class.java))
                }
            }, actionFailed = {
                MainApplication.app.isSketch = true
                MainApplication.app.isChallenge = false

                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    val mIntent = Intent(this, RequestPermissionActivity::class.java)
                    mIntent.putExtra(Constant.KEY_IS_CAMERA, true)
                    startActivity(mIntent)
                } else {
                    startActivity(Intent(this, SketchCategoryActivity::class.java))
                }
            }, InterAdsEnum.HOME, mustWait = true)


        }
        binding.containerTrace.clickWithDebounce {
            showInterAds(action = {
                MainApplication.app.isSketch = false
                MainApplication.app.isChallenge = false

                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    val mIntent = Intent(this, RequestPermissionActivity::class.java)
                    mIntent.putExtra(Constant.KEY_IS_CAMERA, true)
                    startActivity(mIntent)
                } else {
                    startActivity(Intent(this, SketchCategoryActivity::class.java))
                }
            }, actionFailed = {
                MainApplication.app.isSketch = false
                MainApplication.app.isChallenge = false

                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    val mIntent = Intent(this, RequestPermissionActivity::class.java)
                    mIntent.putExtra(Constant.KEY_IS_CAMERA, true)
                    startActivity(mIntent)
                } else {
                    startActivity(Intent(this, SketchCategoryActivity::class.java))
                }
            }, InterAdsEnum.HOME, mustWait = true)


        }

        binding.btnSetting.clickWithDebounce {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        binding.containerHowToUse.clickWithDebounce {
            showInterAds(action = {
                startActivity(Intent(this, GuideActivity::class.java))
            }, actionFailed = {
                startActivity(Intent(this, GuideActivity::class.java))
            }, InterAdsEnum.HOME, mustWait = true)
        }

        binding.containerChallenge.clickWithDebounce {
            showInterAds(action = {
                MainApplication.app.isSketch = false
                MainApplication.app.isChallenge = true

                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    val mIntent = Intent(this, RequestPermissionActivity::class.java)
                    mIntent.putExtra(Constant.KEY_IS_CAMERA, true)
                    startActivity(mIntent)
                } else {
                    startActivity(Intent(this, SketchCategoryActivity::class.java))
                }
            }, actionFailed = {
                MainApplication.app.isSketch = false
                MainApplication.app.isChallenge = true

                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    val mIntent = Intent(this, RequestPermissionActivity::class.java)
                    mIntent.putExtra(Constant.KEY_IS_CAMERA, true)
                    startActivity(mIntent)
                } else {
                    startActivity(Intent(this, SketchCategoryActivity::class.java))
                }
            }, InterAdsEnum.HOME, mustWait = true)

        }

        binding.containerDraw.clickWithDebounce {
            showInterAds(action = {
                startActivity(Intent(this, DrawActivity::class.java))
            }, actionFailed = {
                startActivity(Intent(this, DrawActivity::class.java))
            }, InterAdsEnum.HOME)
        }

        loadAds()
    }

    private fun loadAds() {
        AdsCore.showNativeAds(this, binding.nativeAdmob, {
            binding.nativeAdmob.initCollapseEvent()
        }, {}, {}, NativeTypeEnum.COLLAPSE)
    }

}