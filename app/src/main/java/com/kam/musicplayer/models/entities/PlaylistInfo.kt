package com.kam.musicplayer.models.entities

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import androidx.room.*

@Entity
data class PlaylistInfo(
    @ColumnInfo var name: String,
    @PrimaryKey(autoGenerate = true) val playlistId: Long = 0,
)

@Entity(primaryKeys = ["playlistId", "position"])
data class PlaylistSong(
    val playlistId: Long,
    val position: Int,
    @Embedded val song: Song
)


data class Playlist(
    @Embedded val info: PlaylistInfo,
    @Relation(
            parentColumn = "playlistId",
            entityColumn = "playlistId"
    )
    val playlistSongs: List<PlaylistSong>
) {
    @delegate:Ignore
    val songs by lazy {
        playlistSongs.sortedBy { it.position }.map { it.song }
    }

    val songsCount: Int
        get() = songs.size

    val albumArt: Uri?
        get() = songs.firstOrNull { it.albumArt != null }?.albumArt
}

val PLAYLIST_DIFF_UTIL = object: DiffUtil.ItemCallback<Playlist>() {

    override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem.info.playlistId == newItem.info.playlistId
    }

    override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem.info.name == newItem.info.name &&
                oldItem.songsCount == newItem.songsCount &&
                oldItem.albumArt == newItem.albumArt
    }

}