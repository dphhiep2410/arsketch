package com.example.arsketch.ui.sketch

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.arsketch.R
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.common.hide
import com.example.arsketch.common.loadImageUrl
import com.example.arsketch.common.show
import com.example.arsketch.data.model.SketchModel
import com.example.arsketch.databinding.ItemSketchBinding
import java.io.File

class AdapterSketchList(
    private val onClickItem: (SketchModel) -> Unit,
    private val onClickVipItem: (SketchModel,Int) -> Unit,
) : RecyclerView.Adapter<AdapterSketchList.SkeListViewHolder>() {
    private val listData: MutableList<SketchModel> = mutableListOf()

    inner class SkeListViewHolder(val binding: ItemSketchBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    fun setData(mListData:List<SketchModel>){
        listData.clear()
        listData.addAll(mListData)
        notifyDataSetChanged()
    }
    fun notifyItem(position: Int){
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkeListViewHolder {
        val binding = ItemSketchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SkeListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(holder: SkeListViewHolder, position: Int) {
        with(holder) {
            if (listData[position].isFree) {
                binding.imvVip.hide()
                binding.imvDownload.hide()
                Glide.with(itemView.context).load(listData[position].freeIdRawRes).into(binding.imvIcon)
                binding.root.clickWithDebounce {
                    onClickItem.invoke(listData[position])
                }
            } else if (listData[position].isUnlocked || File(listData[position].localUrl).exists()) {
                binding.imvVip.hide()
                binding.imvDownload.hide()
                binding.loading.show()
                binding.imvIcon.loadImageUrl(listData[position].localUrl,onSuccess = {
                    Log.d("TAG", "onSuccess: ")
                    binding.imvIcon.setImageDrawable(it)

                    binding.loading.hide()
                }, onFail = {
                    Log.d("TAG", "onFail: ")
                })
                binding.root.clickWithDebounce {
                    onClickItem.invoke(listData[position])
                }
            } else{
                binding.imvVip.hide()
                binding.imvDownload.show()
                binding.loading.show()
                binding.imvIcon.loadImageUrl(listData[position].remoteUrl,onSuccess = {
                    Log.d("TAG", "lmao: ")
                    binding.imvIcon.setImageDrawable(it)

                    binding.loading.hide()
                },onFail = {
                    Log.d("TAG", "onFail: ")

                })
                binding.root.clickWithDebounce {
                    onClickVipItem.invoke(listData[position],position)
                }
            }

        }
    }


}