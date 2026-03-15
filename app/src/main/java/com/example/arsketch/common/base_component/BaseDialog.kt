package com.example.arsketch.common.base_component

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.viewbinding.ViewBinding

abstract class BaseDialog<VB : ViewBinding>(context: Context) : Dialog(context) {
    private var _binding: VB? = null
    protected val binding get() = _binding!!
    abstract fun getViewBinding(): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
//        window?.setBackgroundDrawableResource(R.drawable.bg_dialog)
        setBackGroundDrawable()
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        _binding = getViewBinding()
        setContentView(binding.root)
        val width = (context.resources.displayMetrics.widthPixels * 0.9).toInt()
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        window?.setLayout(width, height)
        window!!.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        initView()
    }


    abstract fun setBackGroundDrawable()
    abstract fun initView()

}