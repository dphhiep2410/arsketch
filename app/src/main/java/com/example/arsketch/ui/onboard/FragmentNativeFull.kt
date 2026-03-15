package com.example.arsketch.ui.onboard

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.arsketch.common.ads.NativeTypeEnum
import com.example.arsketch.common.base_component.BaseFragment
import com.example.arsketch.common.hide
import com.example.arsketch.common.show
import com.example.arsketch.databinding.FragmentNativeFullBinding

class FragmentNativeFull : BaseFragment<FragmentNativeFullBinding>() {

    private val handler = Handler(Looper.getMainLooper())
    private var delayRunnable: Runnable? = null

    companion object {
        private const val KEY_NATIVE_TYPE = "key_native_type"
        private const val BUTTON_DELAY_MS = 3000L

        fun newInstance(nativeType: NativeTypeEnum) = FragmentNativeFull().apply {
            arguments = Bundle().apply {
                putString(KEY_NATIVE_TYPE, nativeType.name)
            }
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentNativeFullBinding {
        return FragmentNativeFullBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        val typeName = arguments?.getString(KEY_NATIVE_TYPE) ?: NativeTypeEnum.FULL_OB1.name
        val nativeType = NativeTypeEnum.valueOf(typeName)

        // Hide X button - use fragment's Next button instead
        binding.nativeFullScreen.mViewBinding.btnHideNativeCollapse.visibility = View.GONE

        setupButton()
        loadNativeAd(nativeType)
    }

    private fun setupButton() {
        delayRunnable = Runnable {
            if (!isAdded) return@Runnable
            binding.nativeFullScreen.initCollapseEvent {
                (activity as? OnboardActivity)?.goToNextPage()
            }
        }
        handler.postDelayed(delayRunnable!!, BUTTON_DELAY_MS)
    }

    private fun loadNativeAd(nativeType: NativeTypeEnum) {
        binding.nativeFullScreen.showShimmer(true)
        (activity as? OnboardActivity)?.getNativeAd(nativeType) { nativeAd ->
            if (nativeAd != null && isAdded) {
                binding.nativeFullScreen.setNativeAd(nativeAd)
                binding.nativeFullScreen.visibility = View.VISIBLE
                binding.nativeFullScreen.showShimmer(false)
            } else if (isAdded) {
                binding.nativeFullScreen.visibility = View.GONE
                binding.nativeFullScreen.errorShimmer()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        delayRunnable?.let { handler.removeCallbacks(it) }
    }
}
