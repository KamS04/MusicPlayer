package com.kam.musicplayer.models.entities

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import androidx.room.*

@Entity
data class PlaylistInfo(
    @PrimaryKey val id: Long,
    @ColumnInfo val name: String
)

@Entity(primaryKeys = ["playlistId", "songId"])
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long
)

data class Playlist(
    @Embedded val info: PlaylistInfo,
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "songId",
        associateBy = Junction(PlaylistSongCrossRef::class)
    )
    val songs: List<Song>
) {
    val songsCount: Int
        get() = songs.size

    val albumArt: Uri?
        get() = songs.firstOrNull { it.albumArt != null }?.albumArt

}

val PLAYLIST_DIFF_UTIL = object: DiffUtil.ItemCallback<Playlist>() {
    override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem.info.id == newItem.info.id
    }

    override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem.info.name == newItem.info.name &&
                oldItem.songsCount == newItem.songsCount &&
                oldItem.albumArt == newItem.albumArt
    }

}

/**
class PlaylistInfo2(
    val id: Long,
    var name: String,
    private val dataProvider: ISensibleDataProvider,
    private val songsEntries: MutableList<Pair<Int, Long>> = mutableListOf()
)
 */