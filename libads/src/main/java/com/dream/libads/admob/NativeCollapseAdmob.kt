package com.dream.libads.admob

//import android.util.AppLog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAdView
import com.libads.databinding.NativeCollapseBinding

class NativeCollapseAdmob(context: Context, attrs: AttributeSet?) :
    MLBBaseNativeAdView(context, attrs) {

    var mViewBinding: NativeCollapseBinding =
        NativeCollapseBinding.inflate(LayoutInflater.from(context), this, true)

    fun initCollapseEvent(action: (() -> Unit)? = null, isNew: Boolean = true) {
        showCollapse(true)
        mViewBinding.adAttribution1.visibility = View.VISIBLE
        mViewBinding.btnHideNativeCollapse.setOnClickListener {
            showCollapse(false)
            action?.invoke()
            mViewBinding.adAttribution1.visibility = VISIBLE
            return@setOnClickListener
        }

    }


    fun showCollapse(boolean: Boolean) {
        mViewBinding.root.post {
            if (boolean) {
                mViewBinding.btnHideNativeCollapse.isVisible = true
                mViewBinding.layoutMedia.isVisible = true
                mViewBinding.adCallToAction.isVisible = true
            } else {
                mViewBinding.layoutMedia.isGone = true
                mViewBinding.btnHideNativeCollapse.isGone = true
                mViewBinding.adCallToAction.isGone = true
            }
        }

    }


    override fun getTitleView(): AppCompatTextView = mViewBinding.adHeadline
    override fun getSubTitleView(): AppCompatTextView = mViewBinding.adBody
    override fun getRatingView(): AppCompatRatingBar? = null
    override fun getIconView(): AppCompatImageView = mViewBinding.adAppIcon
    override fun getPriceView(): AppCompatTextView? = null
    override fun getCallActionButtonView(): AppCompatTextView = mViewBinding.adCallToAction
    override fun getMediaView(): MediaView = mViewBinding.adMedia
    override fun getAdView(): NativeAdView = mViewBinding.adView
    override fun getViewContainerRate_Price(): View? = null
    override fun getShimmerView(): ShimmerFrameLayout = mViewBinding.shimmer.shimmerView
    override fun getRootAds(): ConstraintLayout = mViewBinding.rootNativeAd
    override fun getLabelAds(): LinearLayout? = null
    override fun getStore(): AppCompatTextView? = null
    override fun getAdvertiser(): AppCompatTextView? = null

}