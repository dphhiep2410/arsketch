package com.example.arsketch.ui.sketch

import android.provider.ContactsContract.Data
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.data.model.DataCategory
import com.example.arsketch.databinding.ItemCategoryBinding

class AdapterSketchCategory(val listData: MutableList<DataCategory>,private val onClickItem:(DataCategory)->Unit) :
    RecyclerView.Adapter<AdapterSketchCategory.SketchCategoryViewHolder>() {
    inner class SketchCategoryViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SketchCategoryViewHolder {
        val binding =
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SketchCategoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(holder: SketchCategoryViewHolder, position: Int) {
        with(holder){
            binding.imvIcon.setImageResource(listData[position].image)
            binding.name.text = listData[position].name
            binding.root.clickWithDebounce {
                onClickItem.invoke(listData[position])
            }
        }
    }
}