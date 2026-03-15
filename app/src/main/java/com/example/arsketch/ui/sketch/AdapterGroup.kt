package com.example.arsketch.ui.sketch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.arsketch.R
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.data.model.GroupImageModel
import com.example.arsketch.data.model.ImageModel
import com.example.arsketch.databinding.LayoutItemGroupBinding
import com.example.arsketch.databinding.LayoutItemPictureBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdapterGroup(private val onClickItem: (item: GroupImageModel) -> Unit) :
    RecyclerView.Adapter<AdapterGroup.GalleryViewHolder>() {

    private var listGroup = mutableListOf<GroupImageModel>()

    fun setData(listData: List<GroupImageModel>) {
        listGroup.clear()
        listGroup.addAll(listData)
        notifyDataSetChanged()
    }

    inner class GalleryViewHolder(val binding: LayoutItemGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val binding =
            LayoutItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GalleryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listGroup.size
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        with(holder) {
            try {
                var requestOptions = RequestOptions()
                requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(10))
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        try {
                            Glide.with(itemView.context).load(
                                listGroup[adapterPosition].thumbUrl[0]
                            ).placeholder(R.drawable.ic_error_image).centerCrop()
                                .apply(requestOptions).into(binding.imvIcon)
                        } catch (_: IllegalArgumentException) {
                            // Bỏ qua lỗi IllegalArgumentException
                        }
                    }
                }

                binding.tvName.text = listGroup[adapterPosition].name
                binding.quantity.text = listGroup[adapterPosition].thumbUrl.size.toString()

                binding.root.clickWithDebounce {
                    onClickItem.invoke(listGroup[adapterPosition])
                }
            } catch (_: Exception) {
            }
        }
    }
}