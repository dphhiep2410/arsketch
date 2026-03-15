package com.dream.libads.admob

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAdView
import com.libads.databinding.CustomNativeAdsStyle3Binding

class NativeAdStyle3(context: Context, attrs: AttributeSet?) :
    MLBBaseNativeAdView(context, attrs) {

    var mViewBinding: CustomNativeAdsStyle3Binding =
        CustomNativeAdsStyle3Binding.inflate(LayoutInflater.from(context), this, true)

    override fun getTitleView(): AppCompatTextView = mViewBinding.adHeadline
    override fun getSubTitleView(): AppCompatTextView = mViewBinding.adBody
    override fun getRatingView(): AppCompatRatingBar? = null

    override fun getIconView(): AppCompatImageView ?= mViewBinding.adAppIcon

    override fun getPriceView(): AppCompatTextView? = null
    override fun getCallActionButtonView(): AppCompatTextView = mViewBinding.adCallToAction
    override fun getMediaView(): MediaView = mViewBinding.adMedia

    override fun getAdView(): NativeAdView = mViewBinding.adView
    override fun getShimmerView(): ShimmerFrameLayout = mViewBinding.shimmer.shimmerViewGift

    override fun getViewContainerRate_Price(): View? = null
    override fun getLabelAds(): LinearLayout? {
        return null
    }

    override fun getRootAds(): ConstraintLayout = mViewBinding.rootNativeAd
    override fun getStore(): AppCompatTextView? {
        return null

    }

    override fun getAdvertiser(): AppCompatTextView?= null
}