package com.kam.musicplayer.view.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.ActivityMainBinding
import com.kam.musicplayer.services.MusicPlayerService
import com.kam.musicplayer.utils.musicApplication
import com.kam.musicplayer.view.customview.BottomSheetView
import com.kam.musicplayer.view.fragments.*
import com.kam.musicplayer.viewmodel.MainActivityViewModel
import com.kam.musicplayer.viewmodel.factories.MainActivityViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : BaseActivity(), MainActivityViewModel.DataShower {

    private lateinit var mBinding: ActivityMainBinding

    private val mViewModel: MainActivityViewModel by viewModels {
        MainActivityViewModelFactory(musicApplication)
    }

    private lateinit var mTabsFragment: TabsFragment
    private var isTabLayoutBeingShown = false

    private lateinit var superStatic: SuperStaticDataViewer
    private lateinit var reorganizer: PlaylistReorganizer

    private var isMusicScreenShowing = false
    private var isQueueScreenShowing = false

    private val canSwitch: Boolean
        get() = !isMusicScreenShowing && !isQueueScreenShowing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // Just to make sure the service is running or will start running
//        MusicPlayerService.scheduleNonLeakingTask(this) {
//            it.notifyApplicationState(true)
//        }

        setSupportActionBar(mBinding.toolbarMt)

        mTabsFragment = TabsFragment()

        superStatic = SuperStaticDataViewer()
        reorganizer = PlaylistReorganizer()

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
                Log.i("MAct", "MusicScreenBSV $peekHeight")
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
                Log.i("MAct", "QueueScreenBSV $peekHeight")
                with(mBinding.mainScreenFl) {
                    val lp = layoutParams
                    lp.height = mBinding.musicScreenBsv.height - peekHeight
                    layoutParams = lp
                }
            }
        })

        mViewModel.setDataShower(this)

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null) {
            if (intent.action == Intent.ACTION_VIEW) {
                intent.data?.let { uri ->
                    lifecycleScope.launch {
                        val song = musicApplication.repository.getSong(uri)
                        if (song != null)
                            MusicPlayerService.scheduleTask(this@MainActivity, this@MainActivity) { service ->
                                service.setQueue(mutableListOf(song), null)
                            }
                        intent.action = ""
                        intent.data = null
                    }
                }
            }
        }
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
            R.id.refresh_songs -> {
                lifecycleScope.launch {
                    musicApplication.repository.refreshSongs()
                }
            }
            R.id.kill_service -> {
                stopService(Intent(this, MusicPlayerService::class.java))
            }
            R.id.theme_option -> {
                when(resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
            }
        }
        return true
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        if (!isTabLayoutBeingShown) {
            mViewModel.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.revokeDataShower(this)
        // I want this to be in the application class, but the application stays running
        // as long as the services are running
        MusicPlayerService.run { it.notifyApplicationState(false) }
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
            loadContentFragment(superStatic)
        return canSwitch
    }

    override fun showPlaylistReorganizer(): Boolean {
        if (canSwitch)
            loadContentFragment(reorganizer)
        return canSwitch
    }

}