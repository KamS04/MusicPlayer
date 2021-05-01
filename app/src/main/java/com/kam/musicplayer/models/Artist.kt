package com.kam.musicplayer.models

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import com.kam.musicplayer.models.entities.Song

/**
 * Data class that holds information about a specific artist
 */
data class Artist(
    val name: String,
    val songs: List<Song> = listOf()
) {
    val songsCount: Int
        get() = songs.size

    val coverArt: Uri?
        get() = songs.firstOrNull { it.albumArt != null}?.albumArt

    operator fun get(position: Int) = songs[position]
}

val ARTIST_DIFF_CALLBACK = object: DiffUtil.ItemCallback<Artist>() {
    override fun areItemsTheSame(oldItem: Artist, newItem: Artist): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Artist, newItem: Artist): Boolean {
        return oldItem.songsCount == newItem.songsCount &&
                oldItem.coverArt == newItem.coverArt
    }

}