package com.example.arsketch.ui.guide

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.arsketch.ui.onboard.FragmentOnboard

class GuideViewPagerAdapter(
    fm: FragmentManager, lifecycle: Lifecycle
) :
    FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return FragmentGuide.newInstance(position)
    }
}