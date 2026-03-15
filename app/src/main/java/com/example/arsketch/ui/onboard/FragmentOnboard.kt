package com.example.arsketch.ui.onboard

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import com.example.arsketch.R
import com.example.arsketch.common.ads.NativeTypeEnum
import com.example.arsketch.common.base_component.BaseFragment
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.common.hide
import com.example.arsketch.common.show
import com.example.arsketch.data.model.OnboardModel
import com.example.arsketch.databinding.FragmentOnboardBinding


class FragmentOnboard : BaseFragment<FragmentOnboardBinding>() {

    private val handler = Handler(Looper.getMainLooper())
    private var delayRunnable: Runnable? = null

    companion object {
        private const val POSITION_ONBOARD = "position_onboard"
        private const val BUTTON_DELAY_MS = 3000L

        fun newInstance(position: Int) = FragmentOnboard().apply {
            arguments = Bundle().apply {
                putInt(POSITION_ONBOARD, position)
            }
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentOnboardBinding {
        return FragmentOnboardBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        val listData = mutableListOf(
            OnboardModel(getString(R.string.title_onboard_1), getString(R.string.content_onboard_1), R.drawable.ic_onboard_1),
            OnboardModel(getString(R.string.title_onboard_2), getString(R.string.content_onboard_2), R.drawable.ic_onboard_2),
            OnboardModel(getString(R.string.title_onboard_3), getString(R.string.content_onboard_3), R.drawable.ic_onboard_3),
        )
        val pos = arguments?.getInt(POSITION_ONBOARD) ?: 0
        binding.imvIcon.setImageResource(listData[pos].image)
        binding.tvTitle.text = listData[pos].title
        binding.tvContent.text = listData[pos].content ?: ""

        setupButtons(pos)
        setupDotsIndicator()
        loadNativeAd(pos)
    }

    private fun setupButtons(position: Int) {
        binding.btnNext.hide()
        binding.btnStart.hide()
        binding.loadingAds.hide()

        val isLastPage = position == 2

        binding.btnNext.setOnClickListener {
            (activity as? OnboardActivity)?.goToNextPage()
        }

        binding.btnStart.clickWithDebounce {
            (activity as? OnboardActivity)?.onStartClicked()
        }

        delayRunnable = Runnable {
            if (!isAdded) return@Runnable
            if (isLastPage) {
                binding.btnStart.show()
            } else {
                binding.btnNext.show()
            }
        }
        handler.postDelayed(delayRunnable!!, BUTTON_DELAY_MS)
    }

    private fun setupDotsIndicator() {
        val viewPager = (activity as? OnboardActivity)?.getViewPager() ?: return
        binding.dotsIndicator.attachTo(viewPager)
    }

    private fun loadNativeAd(position: Int) {
        val nativeType = when (position) {
            0 -> NativeTypeEnum.OB1
            1 -> NativeTypeEnum.OB2
            2 -> NativeTypeEnum.OB3
            else -> return
        }

        binding.nativeAdView.showShimmer(true)
        (activity as? OnboardActivity)?.getNativeAd(nativeType) { nativeAd ->
            if (nativeAd != null && isAdded) {
                binding.nativeAdView.setNativeAd(nativeAd)
                binding.nativeAdView.visibility = View.VISIBLE
                binding.nativeAdView.showShimmer(false)
                binding.scrollView.post {
                    binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                } } else if (isAdded) {
                binding.nativeAdView.visibility = View.GONE
                binding.nativeAdView.errorShimmer()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        delayRunnable?.let { handler.removeCallbacks(it) }
    }
}
