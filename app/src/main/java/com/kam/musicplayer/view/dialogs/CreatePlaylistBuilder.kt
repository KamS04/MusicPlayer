package com.kam.musicplayer.view.dialogs

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.DialogGetTextBinding
import com.kam.musicplayer.utils.layoutInflater

class CreatePlaylistBuilder(private val context: Context) {

    private var mOnOk: ((name: String) -> Unit)? = null
    private var mOnCancel: (() -> Unit)? = null

    fun setOnOk(action: (name: String) -> Unit): CreatePlaylistBuilder {
        mOnOk = action
        return this
    }

    fun setOnCancel(action: () -> Unit): CreatePlaylistBuilder {
        mOnCancel = action
        return this
    }

    fun createDialog(): AlertDialog {
        val binding = DialogGetTextBinding.inflate(context.layoutInflater)

        val builder = AlertDialog.Builder(context).apply {
            setTitle("Create Playlist")
            setView(binding.root)
            setPositiveButton(R.string.create_playlist) { dialog, _ ->
                dialog.dismiss()
                mOnOk?.let {
                    it(binding.playlistNameEt.text.toString())
                }
            }
            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                mOnCancel?.let { it() }
            }
        }

        return builder.create()
    }

}