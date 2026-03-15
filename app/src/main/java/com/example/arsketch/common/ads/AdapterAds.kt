package com.example.arsketch.common.ads

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.databinding.ItemConfigAdsBinding


class AdapterAds(val onChange:(key: String,value:Int)->Unit) : RecyclerView.Adapter<AdapterAds.ViewHolder>() {
    var listItemConfig = mutableListOf<ItemAds>()

    fun setListConfig(list: List<ItemAds>) {
        listItemConfig.clear()
        listItemConfig.addAll(list)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val binding = ItemConfigAdsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder, position: Int
    ) {
        val item = listItemConfig[position]
        with(holder) {
            holder.binding.apply {
                binding.check.isChecked = item.value == 1
                binding.key.text = item.key
            }

            binding.root.clickWithDebounce {
                onChange.invoke(item.key,if (binding.check.isChecked) 1 else 0)
            }
        }

    }

    override fun getItemCount(): Int {
        return listItemConfig.size
    }

    inner class ViewHolder(val binding: ItemConfigAdsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // Define your ViewHolder properties and methods here
    }
}