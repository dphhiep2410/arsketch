package com.example.arsketch.ui.language

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.arsketch.R
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.databinding.LayoutItemLanguageBinding

import java.util.Locale

class AdapterLanguage(
    private val context: Context, private val onCLickItem: (Triple<Int, Int, Locale>) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mRecyclerView: RecyclerView? = null
    private var listDataLang = mutableListOf<Triple<Int, Int, Locale>>()
    private var currentPos = -1

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    fun setData(mListLang: MutableList<Triple<Int, Int, Locale>>) {
        listDataLang.clear()
        listDataLang.addAll(mListLang)
        notifyDataSetChanged()
    }

    fun removeSelectedId(){
        notifyItemChanged(currentPos)
        currentPos = -1
        notifyItemChanged(currentPos)

    }


    override fun getItemCount(): Int {
        return listDataLang.size
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> {
                with(holder as LanguageViewHolder) {
                    if (absoluteAdapterPosition == currentPos) {
                        binding.tvCountry.setTextColor(
                            ContextCompat.getColor(
                                context, R.color.black
                            )
                        )
                        binding.container.background = ContextCompat.getDrawable(
                            context, R.drawable.bg_lang_selected
                        )
                        binding.tick.setImageResource(R.drawable.ic_tick_active)
                    } else {
                        binding.tvCountry.setTextColor(
                            ContextCompat.getColor(
                                context, R.color.black
                            )
                        )
                        binding.container.background = ContextCompat.getDrawable(
                            context, R.drawable.bg_lang_unselected
                        )
                        binding.tick.setImageResource(R.drawable.ic_tick_off)

                    }
                    val item = listDataLang[absoluteAdapterPosition]
                    binding.tvCountry.text = context.getString(item.first)
                    binding.imvFlag.setImageDrawable(
                        ContextCompat.getDrawable(
                            context, item.second
                        )
                    )
                    binding.root.clickWithDebounce {
                        onCLickItem(item)
                        notifyItemChanged(currentPos)
                        currentPos = adapterPosition
                        notifyItemChanged(currentPos)
                    }
                }

            }

            1 -> {
//                with(holder as LanguageNativeAdsViewHolder) {
//                    if (insideNativeAd != null) {
//                        binding.nativeAdMediumView.showShimmer(false)
//                        binding.nativeAdMediumView.setNativeAd(insideNativeAd!!)
//                        binding.nativeAdMediumView.visibility = View.VISIBLE
//                        binding.root.show()
//                    } else {
//                        binding.root.hide()
//                        with(binding.nativeAdMediumView) {
//                            visibility = View.GONE
//                            showShimmer(true)
//                        }
//                    }
//                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val binding = LayoutItemLanguageBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                LanguageViewHolder(binding)
            }

            else -> {
                val binding = LayoutItemLanguageBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                LanguageViewHolder(binding)
            }

        }
    }
//
//    fun insertAds(ads: NativeAd) {
//        insideNativeAd = ads
//        notifyDataSetChanged()
//    }

    inner class LanguageViewHolder(val binding: LayoutItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root)

//    inner class LanguageNativeAdsViewHolder(val binding: LayouItm) :
//        RecyclerView.ViewHolder(binding.root)
}