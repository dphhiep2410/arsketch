package com.example.arsketch.ui.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.example.arsketch.R

class DialogFragmentLoadingConsent {

    fun onCreateDialog(context: Context): Dialog {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_load_consent)
        val window = dialog.window
        val wlp = window!!.attributes
        wlp.gravity = Gravity.CENTER
        dialog.window!!.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        window.attributes = wlp
        dialog.window!!.setBackgroundDrawableResource(R.color.transparent)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT
        )
        return dialog
    }
}