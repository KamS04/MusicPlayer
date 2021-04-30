package com.kam.musicplayer.view.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.ActivityMainBinding
import com.kam.musicplayer.services.MusicPlayerService
import com.kam.musicplayer.utils.musicApplication
import com.kam.musicplayer.view.customview.BottomSheetView
import com.kam.musicplayer.view.fragments.TabsFragment
import com.kam.musicplayer.viewmodel.MainActivityViewModel
import com.kam.musicplayer.viewmodel.factories.MainActivityViewModelFactory

class MainActivity : BaseActivity() {

    private lateinit var mBinding: ActivityMainBinding

    private val mViewModel: MainActivityViewModel by viewModels {
        MainActivityViewModelFactory(musicApplication)
    }

    private lateinit var mTabsFragment: TabsFragment
    private var isTabLayoutBeingShown = false

    private var isMusicScreenShowing = false
    private var isQueueScreenShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setSupportActionBar(mBinding.toolbarMt)

        mTabsFragment = TabsFragment()

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

        loadTabLayout()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return true
    }

    private fun loadTabLayout() {
        loadContentFragment(mTabsFragment, true)
    }

    private fun loadContentFragment(fragment: Fragment, isTabLayout: Boolean = false) {
        supportFragmentManager.beginTransaction().apply {
            replace(mBinding.mainScreenFl.id, fragment)
            addToBackStack(null)
        }.commit()

        isTabLayoutBeingShown = isTabLayout
    }


}