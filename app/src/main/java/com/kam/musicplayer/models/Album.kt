package com.kam.musicplayer.models

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import com.kam.musicplayer.models.entities.Song

data class Album(
    val name: String,
    var songs: List<Song> = listOf()
) {
    val songCount: Int
        get() = songs.size

    val coverArt: Uri?
        get() = songs.firstOrNull { it.albumArt != null }?.albumArt

    fun getSong(position: Int): Song = songs[position]
}

val ALBUM_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Album>() {
    override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
        return oldItem.songCount == newItem.songCount &&
                oldItem.coverArt == newItem.coverArt
    }

}