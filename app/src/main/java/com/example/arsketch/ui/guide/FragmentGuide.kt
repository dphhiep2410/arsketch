package com.example.arsketch.ui.guide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.arsketch.R
import com.example.arsketch.common.base_component.BaseFragment
import com.example.arsketch.data.model.GuideModel
import com.example.arsketch.databinding.FragmentGuideBinding


class FragmentGuide : BaseFragment<FragmentGuideBinding>() {

    companion object {
        private const val POSITION_GUIDE = "position_guide"
        fun newInstance(position: Int) = FragmentGuide().apply {
            arguments = Bundle().apply {
                putInt(POSITION_GUIDE, position)
            }
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentGuideBinding {
        return FragmentGuideBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        val listData = mutableListOf(
            GuideModel(
                getString(R.string.how_to_use_sketch),
                getString(R.string.sketch_guide_1),
                R.drawable.ic_how_to_use_sketch_1,
                getString(R.string.sketch_guide_2),
                R.drawable.ic_how_to_use_sketch_2
            ),
            GuideModel(
                getString(R.string.how_to_use_trace),
                getString(R.string.trace_guide_1),
                R.drawable.ic_how_to_use_trace_1,
                getString(R.string.trace_guide_2),
                R.drawable.ic_how_to_use_trace_2
            ),
        )
        val pos = arguments?.getInt(POSITION_GUIDE)
        if (pos != null) {
            with(binding) {
                tvTitle.text = listData[pos].title
                tvGuide1.text = listData[pos].tv1
                tvGuide2.text = listData[pos].tv2
                imvHowToUse1.setImageResource(listData[pos].image1)
                imvHowToUse2.setImageResource(listData[pos].image2)
            }
        } else {

        }
    }
}