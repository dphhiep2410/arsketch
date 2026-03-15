package com.example.arsketch.ui.draw

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import androidx.databinding.ViewDataBinding
import com.example.arsketch.R
import com.example.arsketch.common.base_component.BaseRecyclerView
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.data.model.ColorModel
import com.example.arsketch.databinding.LayoutItemColorBinding

class AdapterColor(private val onClickItem: (color: String) -> Unit) :
    BaseRecyclerView<ColorModel>() {
    private var lastSelectedView = 0

    override fun getItemLayout(): Int {
        return R.layout.layout_item_color
    }

    @SuppressLint("NotifyDataSetChanged", "UseCompatLoadingForDrawables")

    override fun setData(binding: ViewDataBinding, item: ColorModel, layoutPosition: Int) {
        if (binding is LayoutItemColorBinding) {
            context?.let { ctx ->
                binding.imvColor.setBackgroundColor(Color.parseColor(item.color))
                if (lastSelectedView == layoutPosition) {
                    binding.container.setBackgroundResource(R.drawable.bg_overlay_item_select)
                } else {
                    binding.container.setBackgroundResource(R.drawable.bg_overlay_item)

                }
            }
        }
    }

    override fun submitData(newData: List<ColorModel>) {
        list.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onClickViews(binding: ViewDataBinding, obj: ColorModel, layoutPosition: Int) {
        super.onClickViews(binding, obj, layoutPosition)
        binding.root.clickWithDebounce {
            val temp = lastSelectedView
            lastSelectedView = layoutPosition
            notifyItemChanged(lastSelectedView)
            notifyItemChanged(temp)
            Log.d("TAG", "onClickViews: "+lastSelectedView)
            onClickItem.invoke(obj.color)
        }
    }
}