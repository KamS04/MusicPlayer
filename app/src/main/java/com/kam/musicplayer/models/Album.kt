package com.kam.musicplayer.models

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import com.kam.musicplayer.models.entities.Song

data class Album(
    val name: String,
    var songs: List<Song> = listOf()
) {
    val songsCount: Int
        get() = songs.size

    val coverArt: Uri?
        get() = songs.firstOrNull { it.albumArt != null }?.albumArt

    operator fun get(position: Int): Song = songs[position]
}

val ALBUM_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Album>() {
    override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
        return oldItem.songsCount == newItem.songsCount &&
                oldItem.coverArt == newItem.coverArt
    }

}