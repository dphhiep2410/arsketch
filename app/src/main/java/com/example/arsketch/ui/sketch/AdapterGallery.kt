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
import com.example.arsketch.data.model.ImageModel
import com.example.arsketch.databinding.LayoutItemPictureBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdapterGallery(private val onClickItem: (item: ImageModel) -> Unit) :
    RecyclerView.Adapter<AdapterGallery.GalleryViewHolder>() {

    private var listImage = mutableListOf<ImageModel>()

    fun setData(listData: List<ImageModel>) {
        listImage.clear()
        listImage.addAll(listData)
        notifyDataSetChanged()
    }

    inner class GalleryViewHolder(val binding: LayoutItemPictureBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val binding =
            LayoutItemPictureBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GalleryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listImage.size
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
                                listImage[adapterPosition].imageUri
                            ).placeholder(R.drawable.ic_error_image).centerCrop()
                                .apply(requestOptions).into(binding.imvIcon)
                        } catch (_: IllegalArgumentException) {
                            // Bỏ qua lỗi IllegalArgumentException
                        }
                    }
                }

                binding.root.clickWithDebounce {
                    onClickItem.invoke(listImage[adapterPosition])
                }
            } catch (_: Exception) {
            }
        }
    }
}