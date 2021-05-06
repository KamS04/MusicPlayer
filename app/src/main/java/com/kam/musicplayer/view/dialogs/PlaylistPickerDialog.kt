package com.kam.musicplayer.view.dialogs

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.kam.musicplayer.R
import com.kam.musicplayer.models.entities.Playlist

class PickPlaylistBuilder(private val context: Context) {

    private var playlistsList: List<Playlist> = listOf()
    private var mOnSelected: ((playlist: Playlist) -> Unit)? = null
    private var mRequestCreate: (() -> Unit)? = null
    private var mOnCancel: (() -> Unit)? = null

    fun setOnSelected(action: (playlist: Playlist) -> Unit) : PickPlaylistBuilder {
        mOnSelected = action
        return this
    }

    fun setPlaylists(playlists: List<Playlist>) : PickPlaylistBuilder {
        playlistsList = playlists
        return this
    }

    fun setRequestCreate(action: () -> Unit) : PickPlaylistBuilder {
        mRequestCreate = action
        return this
    }

    fun setOnCancel(action: () -> Unit) : PickPlaylistBuilder {
        mOnCancel = action
        return this
    }

    fun createDialog(): AlertDialog {
        return AlertDialog.Builder(context).apply {
            setTitle(R.string.choose_playlist)

            setItems( playlistsList.map { it.info.name }.toTypedArray() ) { dialog, which ->
                dialog.dismiss()
                mOnSelected?.let {
                    it(playlistsList[which])
                }
            }

            setNeutralButton(R.string.create_playlist) { dialog, _ ->
                dialog.dismiss()
                mRequestCreate?.let { it() }
            }

            setNeutralButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                mOnCancel?.let { it() }
            }
        }.create()
    }

}