package com.example.arsketch.ui.dialog

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.arsketch.R
import com.example.arsketch.common.Constant
import com.example.arsketch.common.base_component.BaseDialog
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.data.model.SketchModel
import com.example.arsketch.databinding.DialogFinishBinding
import com.example.arsketch.ui.challenge.ChallengeActivity
import com.example.arsketch.ui.finish.FinishActivity

class DialogFinish(val mContext: Context, val imageUri: Uri, val sketchModel: SketchModel,val onFinish:(imageUri:Uri)->Unit) :
    BaseDialog<DialogFinishBinding>(mContext) {
    override fun getViewBinding(): DialogFinishBinding {
        return DialogFinishBinding.inflate(layoutInflater)
    }

    override fun setBackGroundDrawable() {
        window?.setBackgroundDrawableResource(R.drawable.bg_n6_12)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    override fun initView() {
        binding.image.setImageURI(imageUri)
        binding.btnFinish.clickWithDebounce {
            dismiss()
            onFinish.invoke(imageUri)
        }


        binding.btnTakePhoto.clickWithDebounce {
            dismiss()
        }


    }
}