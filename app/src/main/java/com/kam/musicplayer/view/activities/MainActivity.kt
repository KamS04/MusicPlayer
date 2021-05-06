package com.kam.musicplayer.view.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.ActivityMainBinding
import com.kam.musicplayer.services.MusicPlayerService
import com.kam.musicplayer.utils.musicApplication
import com.kam.musicplayer.view.customview.BottomSheetView
import com.kam.musicplayer.view.fragments.PlaylistViewer
import com.kam.musicplayer.view.fragments.StaticDataViewer
import com.kam.musicplayer.view.fragments.TabsFragment
import com.kam.musicplayer.viewmodel.MainActivityViewModel
import com.kam.musicplayer.viewmodel.factories.MainActivityViewModelFactory

class MainActivity : BaseActivity(), MainActivityViewModel.DataShower {

    private lateinit var mBinding: ActivityMainBinding

    private val mViewModel: MainActivityViewModel by viewModels {
        MainActivityViewModelFactory(musicApplication)
    }

    private lateinit var mTabsFragment: TabsFragment
    private var isTabLayoutBeingShown = false

    private lateinit var mStaticData: StaticDataViewer
    private lateinit var mPlaylistViewer: PlaylistViewer

    private var isMusicScreenShowing = false
    private var isQueueScreenShowing = false

    private val canSwitch: Boolean
        get() = !isMusicScreenShowing && !isQueueScreenShowing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setSupportActionBar(mBinding.toolbarMt)

        mTabsFragment = TabsFragment()
        mStaticData = StaticDataViewer()
        mPlaylistViewer = PlaylistViewer()

        loadContentFragment(mTabsFragment, true)

        mViewModel.setDataShower(this)

        mViewModel.title.observe(this) {
            supportActionBar?.title = it
        }

        mBinding.musicScreenBsv.setOnActionListener(object: BottomSheetView.OnActionListener {
            override fun onSheetClosed() {
                isMusicScreenShowing = false
            }

            override fun onSheetOpened() {
                isMusicScreenShowing = true
            }

            override fun onSetPeekHeight(peekHeight: Int) {
                with(mBinding.mainScreenFl) {
                    val lp = layoutParams
                    lp.height = mBinding.windowCl.height - peekHeight
                }
            }
        })

        mBinding.queueScreenBsv.setOnActionListener(object: BottomSheetView.OnActionListener {
            override fun onSheetClosed() {
                isQueueScreenShowing = false
            }

            override fun onSheetOpened() {
                isMusicScreenShowing = true
            }

            override fun onSetPeekHeight(peekHeight: Int) {
                with(mBinding.mainScreenFl) {
                    val lp = layoutParams
                    lp.height = mBinding.musicScreenBsv.height - peekHeight
                    layoutParams = lp
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
            R.id.kill_service -> {
                stopService(Intent(this, MusicPlayerService::class.java))
            }
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (!isTabLayoutBeingShown) {
            mViewModel.returnHome()
        }
    }

    private fun loadContentFragment(fragment: Fragment, isTabLayout: Boolean = false) {
        supportFragmentManager.beginTransaction().apply {
            replace(mBinding.mainScreenFl.id, fragment)
            addToBackStack(null)
        }.commit()

        isTabLayoutBeingShown = isTabLayout
        supportActionBar?.setDisplayHomeAsUpEnabled(!isTabLayout)
    }

    override fun receiveControl() {
        loadContentFragment(mTabsFragment, true)
    }

    override fun showStaticData(): Boolean {
        if (canSwitch)
            loadContentFragment(mStaticData)
        return canSwitch
    }

    override fun showPlaylist(): Boolean {
        if (canSwitch)
            loadContentFragment(mPlaylistViewer)
        return canSwitch
    }


}