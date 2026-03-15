package com.dream.libads.admob

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.libads.R


abstract class MLBBaseNativeAdView(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    var mNativeAd: NativeAd? = null

    open fun setNativeAd(nativeAd: NativeAd) {

        this.mNativeAd = nativeAd

        val icon = nativeAd.icon
        val starRating = nativeAd.starRating
        val title = nativeAd.headline
        val callAction = nativeAd.callToAction
        val price = nativeAd.price
        val subTitle = nativeAd.body
        val store = nativeAd.store
        val advertiser = nativeAd.advertiser


        if (icon != null) {
            getIconView()?.setImageDrawable(icon.drawable)
            getIconView()?.visibility = VISIBLE
        } else {
            getIconView()?.setBackgroundResource(R.drawable.bg_8)
            getIconView()?.visibility = VISIBLE
        }
        if (advertiser != null) {
            getAdvertiser()?.text = advertiser
            getAdvertiser()?.visibility = VISIBLE
            getAdvertiser()?.isSelected = true
        }else{
            getAdvertiser()?.visibility = INVISIBLE
        }

        if(store != null){
            getStore()?.text = store
            getStore()?.visibility = VISIBLE
            getStore()?.isSelected = true
        }else{
            getStore()?.visibility = INVISIBLE
        }

        if(starRating != null && starRating > 0){
            getRatingView()?.rating = starRating.toFloat()
            getRatingView()?.visibility = VISIBLE
            getRatingView()?.isSelected = true
            getTvStar()?.text = starRating.toFloat().toString()
        }else{
            getRatingView()?.visibility = GONE
            getTvStar()?.visibility = GONE
        }


        if (subTitle != null) {
            getSubTitleView()?.text = subTitle
            getSubTitleView()?.visibility = View.VISIBLE
            getSubTitleView()?.isSelected = true
        } else {
            getSubTitleView()?.visibility = View.INVISIBLE
        }



        if (title != null) {
            getTitleView().text = title
            getTitleView().visibility = View.VISIBLE
            getTitleView().isSelected = true

        } else {
            getTitleView().visibility = View.INVISIBLE
        }

        if (callAction != null) {
            getCallActionButtonView().text = callAction
            getCallActionButtonView().visibility = View.VISIBLE
        } else {
            getCallActionButtonView().visibility = View.INVISIBLE
        }


        if (price != null) {
            getPriceView()?.text = price
            getPriceView()?.visibility = View.VISIBLE
        } else {
            getPriceView()?.visibility = View.INVISIBLE
        }

        if (getMediaView() != null) {
            getAdView().mediaView = getMediaView()
        }

        getAdView().callToActionView = getCallActionButtonView()
        getAdView().headlineView = getTitleView()
        getAdView().bodyView = getSubTitleView()
        getAdView().setNativeAd(nativeAd)
        getRootAds()?.setOnClickListener {
            getCallActionButtonView().performClick()
        }

        getLabelAds()?.setOnClickListener {
            getCallActionButtonView().performClick()
        }
        getRootAds().setOnTouchListener { _, event ->
            val screenHeight = resources.displayMetrics.heightPixels
            val touchY = event.rawY

            if (event.action == MotionEvent.ACTION_DOWN) {
                if (screenHeight - touchY < 50) {
                    return@setOnTouchListener true
                }
            }

            false
        }
    }

    fun showShimmer(isShow: Boolean) {
        if (isShow) {
            getShimmerView()?.startShimmer()
            getShimmerView()?.isVisible = true
            getRootAds().isVisible = true
        } else {
            getShimmerView()?.stopShimmer()
            getShimmerView()?.isVisible = false
            getRootAds().isVisible = true
        }
    }

    fun hideAdsAndShimmer() {
        getShimmerView()?.stopShimmer()
        getShimmerView()?.isVisible = false
        getRootAds().isVisible = false
    }

    fun errorShimmer() {
        getShimmerView()?.stopShimmer()
    }


    abstract fun getTitleView(): AppCompatTextView
    abstract fun getSubTitleView(): AppCompatTextView?
    abstract fun getRatingView(): AppCompatRatingBar?
    abstract fun getIconView(): ImageView?
    abstract fun getPriceView(): AppCompatTextView?
    abstract fun getCallActionButtonView(): AppCompatTextView
    abstract fun getAdView(): NativeAdView
    abstract fun getShimmerView(): ShimmerFrameLayout ?
    abstract fun getRootAds(): ViewGroup
    abstract fun getStore(): AppCompatTextView?
    abstract fun getAdvertiser(): AppCompatTextView?
    abstract fun getViewContainerRate_Price(): View?
    abstract fun getLabelAds(): LinearLayout?
    open fun getMediaView(): MediaView? = null
    open fun getTvStar(): AppCompatTextView? = null
}