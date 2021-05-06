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

    private val _selectedPlaylist: MutableLiveData<LiveData<Playlist>?> = MutableLiveData(null)
    val selectedPlaylist: LiveData<LiveData<Playlist>?>
        get() = _selectedPlaylist

    private var mDataShower: DataShower? = null

    fun clearTitle() {
        _title.value = appName
        mController = Controller.Activity
    }

    fun setTitle(controller: Controller, newTitle: String) {
        if (controller == mController)
            _title.value = newTitle
    }

    fun showAlbum(album: LiveData<Album>) {
        _isShowingAlbum.value = true
        _selectedAlbum.value = album
        if (mController == Controller.Activity) {
            val switched = mDataShower?.showStaticData()
            if (switched == true)
                mController = Controller.Static
        }
    }

    fun showArtist(artist: LiveData<Artist>) {
        _isShowingAlbum.value = false
        _selectedArtist.value = artist
        if (mController == Controller.Activity) {
            val switched = mDataShower?.showStaticData()
            if (switched == true)
                mController = Controller.Static
        }
    }

    fun showPlaylist(playlist: LiveData<Playlist>) {
        _selectedPlaylist.value = playlist
        if (mController == Controller.Activity) {
            val switched = mDataShower?.showPlaylist()
            if (switched == true)
                mController = Controller.Playlist
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
            Controller.Playlist -> {
                val switched = mDataShower!!.showPlaylist()
                if (!switched)
                    mController = Controller.Activity
            }
        }
    }

    fun returnHome() {
        clearTitle()
        mDataShower?.receiveControl()
    }

    enum class Controller {
        Activity,
        Static,
        Playlist
    }

    interface DataShower {
        fun receiveControl()
        fun showStaticData() : Boolean
        fun showPlaylist() : Boolean
    }
}