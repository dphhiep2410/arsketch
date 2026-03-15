package com.example.arsketch.ui.guide

import com.example.arsketch.common.base_component.BaseActivity
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.databinding.ActivityGuideBinding

class GuideActivity : BaseActivity<ActivityGuideBinding>(ActivityGuideBinding::inflate,{true}) {
    private lateinit var viewpagerAdapter: GuideViewPagerAdapter


    override fun initView() {
        initButton()
        initViewPager()
    }

    private fun initButton() {
//        AdsCore.showBannerAds(this,binding.bannerAds,null,null,true,BannerTypeEnum.DRAW)
        binding.btnBack.clickWithDebounce {
            finish()
        }
    }

    private fun initViewPager() {
        viewpagerAdapter = GuideViewPagerAdapter(
            supportFragmentManager, lifecycle
        )
        binding.vpGuide.adapter = viewpagerAdapter
        binding.dotsIndicator.attachTo(binding.vpGuide)
    }


}