package com.example.arsketch.ui.onboard.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.arsketch.common.ads.NativeTypeEnum
import com.example.arsketch.ui.onboard.FragmentNativeFull
import com.example.arsketch.ui.onboard.FragmentOnboard

class ViewPagerAdapter(
    fm: FragmentManager, lifecycle: Lifecycle
) :
    FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentOnboard.newInstance(0)
            1 -> FragmentNativeFull.newInstance(NativeTypeEnum.FULL_OB1)
            2 -> FragmentOnboard.newInstance(1)
            3 -> FragmentNativeFull.newInstance(NativeTypeEnum.FULL_OB2)
            4 -> FragmentOnboard.newInstance(2)
            else -> FragmentOnboard.newInstance(0)
        }
    }
}
