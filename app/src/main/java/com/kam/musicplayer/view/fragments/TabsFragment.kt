package com.kam.musicplayer.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.kam.musicplayer.databinding.FragmentTabsBinding
import com.kam.musicplayer.view.adapters.TabsFragmentPagerAdapter

/**
 * This handles displaying the main data, sort of
 * It is a placeholder that displays the
 * [AllSongsFragment], [PlaylistsFragment], [ArtistsFragment], [AlbumsFragment]
 */
class TabsFragment : Fragment() {

    private var _binding: FragmentTabsBinding? = null

    private val mBinding: FragmentTabsBinding
        get() = _binding?: throw Exception("Binding must not be accessed before creation or after destruction")


    private lateinit var mTabsFragmentPagerAdapter: TabsFragmentPagerAdapter

    lateinit var mTabs: MutableList<TabsFragmentPagerAdapter.TabInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mTabs = createTabs()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabsBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mTabsFragmentPagerAdapter = TabsFragmentPagerAdapter(childFragmentManager, lifecycle) {
            mTabs
        }

        mBinding.contentVp2.adapter = mTabsFragmentPagerAdapter

        TabLayoutMediator(mBinding.tabsTl, mBinding.contentVp2) { tab, position ->
            tab.text = mTabs.first { it.position == position }.name
        }.attach()
    }

    private fun createTabs(): MutableList<TabsFragmentPagerAdapter.TabInfo> {
        val tabs: MutableList<TabsFragmentPagerAdapter.TabInfo> = mutableListOf()

        tabs.add(TabsFragmentPagerAdapter.TabInfo(0, "Music",
            AllSongsFragment()
        ))

        tabs.add(TabsFragmentPagerAdapter.TabInfo(1, "Albums",
            AlbumsFragment()
        ))

        tabs.add(TabsFragmentPagerAdapter.TabInfo(2, "Artists",
            ArtistsFragment()
        ))

        tabs.add(TabsFragmentPagerAdapter.TabInfo(3, "Playlists",
            PlaylistsFragment()
        ))

        return tabs
    }

}