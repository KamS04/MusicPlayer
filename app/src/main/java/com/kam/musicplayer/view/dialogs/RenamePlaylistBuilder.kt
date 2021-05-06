package com.kam.musicplayer.view.dialogs

import android.app.AlertDialog
import android.content.Context
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.DialogGetTextBinding
import com.kam.musicplayer.utils.layoutInflater

class RenamePlaylistBuilder(private val context: Context) {

    private var mOnOk: ((newName: String) -> Unit)? = null
    private var mOnCancel: (() -> Unit)? = null
    private var mOldName: String = ""

    fun setOldName(name: String) : RenamePlaylistBuilder {
        mOldName = name
        return this
    }

    fun addOnOk(action: (newName: String) -> Unit) : RenamePlaylistBuilder {
        mOnOk = action
        return this
    }

    fun addOnCancel(action: () -> Unit) : RenamePlaylistBuilder {
        mOnCancel = action
        return this
    }

    fun createDialog(): AlertDialog {
        val binding =DialogGetTextBinding.inflate(context.layoutInflater)
        binding.playlistNameEt.setText(mOldName)

        return AlertDialog.Builder(context).apply {
            setView(binding.root)
            setTitle("Rename Playlist")
            setPositiveButton(R.string.rename_playlist) { dialog, _ ->
                dialog.dismiss()
                mOnOk?.let {
                    it(binding.playlistNameEt.text.toString())
                }
            }
            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                mOnCancel?.let { it() }
            }
        }.create()
    }

}