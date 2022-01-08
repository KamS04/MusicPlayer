package com.kam.musicplayer.viewmodel

import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kam.musicplayer.R
import com.kam.musicplayer.application.MusicApplication
import com.kam.musicplayer.models.Album
import com.kam.musicplayer.models.Artist
import com.kam.musicplayer.models.entities.Playlist
import com.kam.musicplayer.view.fragments.SuperStaticDataViewer

class MainActivityViewModel(private val application: MusicApplication) : ViewModel() {

    private val appName = application.applicationContext.resources.getString(R.string.app_name)
    private val _title = MutableLiveData<String>(appName)
    val title: LiveData<String>
        get() = _title

    private var mController: Controller = Controller.Activity

    private val _selectedAlbum: MutableLiveData<LiveData<Album>?> = MutableLiveData(null)
    val selectedAlbum: LiveData<LiveData<Album>?>
        get() = _selectedAlbum

    private val _selectedArtist: MutableLiveData<LiveData<Artist>?> = MutableLiveData(null)
    val selectedArtist: LiveData<LiveData<Artist>?>
        get() = _selectedArtist

    private val _isShowingAlbum: MutableLiveData<Boolean> = MutableLiveData(true)
    val isShowingAlbum: LiveData<Boolean>
        get() = _isShowingAlbum

    private val _currentDisplaying: MutableLiveData<SuperStaticDataViewer.DataType> = MutableLiveData(SuperStaticDataViewer.DataType.Artist)
    val currentlyDisplaying: LiveData<SuperStaticDataViewer.DataType>
        get() = _currentDisplaying

    private val _selectedPlaylist: MutableLiveData<LiveData<Playlist>?> = MutableLiveData(null)
    val selectedPlaylist: LiveData<LiveData<Playlist>?>
        get() = _selectedPlaylist

    private val _playlistToReorder: MutableLiveData<Playlist?> = MutableLiveData(null)
    val playlistToReorder: LiveData<Playlist?>
        get() = _playlistToReorder

    private var mDataShower: DataShower? = null
    private var lastStaticTitle = ""

    fun clearTitle() {
        _title.value = appName
        mController = Controller.Activity
    }

    fun setTitle(controller: Controller, newTitle: String) {
        if (mController == Controller.Static)
            lastStaticTitle = title.value ?: ""

        if (controller == mController)
            _title.value = newTitle
    }

    fun startReordering(playlist: Playlist) {
        _playlistToReorder.value = playlist
        val switched = mDataShower?.showPlaylistReorganizer()
        if (switched == true)
            mController = Controller.PlaylistReorganizer
    }

    fun showAlbum(album: LiveData<Album>) {
        _currentDisplaying.value = SuperStaticDataViewer.DataType.Album
        _selectedAlbum.value = album
        switchToStatic()
    }

    fun showArtist(artist: LiveData<Artist>) {
        _currentDisplaying.value = SuperStaticDataViewer.DataType.Artist
        _selectedArtist.value = artist
        switchToStatic()
    }

    fun showPlaylist(playlist: LiveData<Playlist>) {
        _currentDisplaying.value = SuperStaticDataViewer.DataType.Playlist
        _selectedPlaylist.value = playlist
        switchToStatic()
    }

    private fun switchToStatic() {
        if (mController != Controller.Static) {
            val switched = mDataShower?.showStaticData()
            if (switched == true)
                mController = Controller.Static
        }
    }

    fun setDataShower(dataShower: DataShower) {
        mDataShower = dataShower
        when(mController) {
            Controller.Activity -> return
            Controller.Static -> {
                val switched = dataShower.showStaticData()
                if (!switched)
                    mController = Controller.Activity
            }
            Controller.PlaylistReorganizer -> {
                val switched = dataShower.showPlaylistReorganizer()
                if (!switched)
                    mController = Controller.Activity
            }
        }
    }

    fun revokeDataShower(shower: DataShower) {
        if (shower == mDataShower) {
            mDataShower = null
            mController = Controller.Activity
        }
    }

    fun onBackPressed() {
        when (mController) {
            Controller.Static -> {
                clearTitle()
                mDataShower?.receiveControl()
            }
            Controller.PlaylistReorganizer -> {
                clearTitle()
                switchToStatic()
                setTitle(Controller.Static, lastStaticTitle)
            }
        }
    }

    enum class Controller {
        Activity,
        Static,
        PlaylistReorganizer
    }

    interface DataShower {
        fun receiveControl()
        fun showStaticData() : Boolean
        fun showPlaylistReorganizer(): Boolean
    }
}