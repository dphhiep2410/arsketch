package com.example.arsketch.ui.dialog

import android.content.Context
import com.example.arsketch.R
import com.example.arsketch.common.base_component.BaseDialog
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.databinding.DialogWatchAdsBinding

class DialogWatchAds(
    context: Context,
    private val onClickWatch: () -> Unit,
    var isRecord:Boolean = false
) :
    BaseDialog<DialogWatchAdsBinding>(context) {
    override fun getViewBinding(): DialogWatchAdsBinding {
        return DialogWatchAdsBinding.inflate(layoutInflater)
    }


    override fun setBackGroundDrawable() {
        window?.setBackgroundDrawableResource(R.drawable.bg_n6_12)
    }

    override fun initView() {
        binding.btnWatch.clickWithDebounce {
            onClickWatch.invoke()
            dismiss()
        }
        if(isRecord){
            binding.imvIcon.setImageResource(R.drawable.ic_record)
            binding.tvUnlockItem.text = context.getText(R.string.record)
        }else{
            binding.imvIcon.setImageResource(R.drawable.ic_unlock_ads)
            binding.tvUnlockItem.text = context.getText(R.string.unlock_item)
        }
    }
}