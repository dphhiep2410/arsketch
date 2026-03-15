package com.example.arsketch.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.example.arsketch.R
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.common.hide
import com.example.arsketch.common.show
import com.example.arsketch.databinding.DialogLoadVideoBinding


class DialogLoadingProgress(context: Context, private val onEnd: () -> Unit) : Dialog(context) {
    private lateinit var binding: DialogLoadVideoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawable(ColorDrawable(context.getColor(R.color.transparent)))
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        window!!.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        binding = DialogLoadVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val width = (context.resources.displayMetrics.widthPixels * 0.9).toInt()
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        window?.setLayout(width, height)
        initView()
    }

    fun setValue(value: Float) {
        binding.progressBar.progress = (value * 100).toInt()
    }

    fun complete() {
        binding.imageArrow.hide()
        binding.imageDone.show()
        binding.imageArrow.clearAnimation()
        val drawable = binding.imageDone.drawable
        if (drawable is AnimatedVectorDrawableCompat) {
            drawable.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    dismiss()
                    onEnd.invoke()
                }
            })
            drawable.start()
        } else if (drawable is AnimatedVectorDrawable) {
            drawable.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    dismiss()
                    onEnd.invoke()
                }
            })
            drawable.start()
        }
    }

    fun reset() {
        binding.progressBar.progress = 0
        binding.imageArrow.show()
        binding.imageDone.hide()
    }

    private fun initView() {
        loopAnimation(binding.imageArrow)
        binding.root.clickWithDebounce { }
    }

    private fun loopAnimation(view: View) {
        view.animate().translationY(60f).alpha(1f).setDuration(1000).withEndAction {
                view.translationY = -60f
                view.alpha = 0f
                loopAnimation(view)
            }.start()
    }


}