package com.example.arsketch.ui.dialog

import android.content.Context
import com.example.arsketch.R
import com.example.arsketch.common.base_component.BaseDialog
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.databinding.DialogRequestAudioBinding

class DialogRequestRecordAudio(
    private val mContext: Context,
    private val onPressAccept: () -> Unit,
    private val isNotification: Boolean
) :
    BaseDialog<DialogRequestAudioBinding>(mContext) {
    override fun getViewBinding(): DialogRequestAudioBinding {
        return DialogRequestAudioBinding.inflate(layoutInflater)
    }

    override fun setBackGroundDrawable() {
        window?.setBackgroundDrawableResource(R.drawable.bg_n6_12)
    }

    override fun initView() {
        if (isNotification) {
            binding.tv2.text = mContext.getString(R.string.access_3)
        } else {
            binding.tv2.text = mContext.getString(R.string.access_2)
        }

        binding.btnAccept.clickWithDebounce {
            onPressAccept.invoke()
            dismiss()
        }

        binding.btnCancel.clickWithDebounce {
            dismiss()
        }
    }
}