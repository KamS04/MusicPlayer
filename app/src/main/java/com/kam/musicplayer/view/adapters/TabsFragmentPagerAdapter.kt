package com.kam.musicplayer.view.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Adapter for [androidx.viewpager2.widget.ViewPager2]
 * Handles creating tabs from fragments
 */
class TabsFragmentPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    val getTabs: () -> MutableList<TabInfo>
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val mData: MutableList<TabInfo>
        get() = getTabs()

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun createFragment(position: Int): Fragment {
        return mData.first { it.position == position }.fragment
    }


    data class TabInfo(
        val position: Int,
        val name: String,
        val fragment: Fragment
    )

}