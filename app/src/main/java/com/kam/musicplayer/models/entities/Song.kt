package com.kam.musicplayer.models.entities

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="SONGS_TABLE")
class Song(
    @PrimaryKey val id: Long,
    @ColumnInfo val name: String,
    @ColumnInfo val path: Uri,
    @ColumnInfo val artist: String,
    @ColumnInfo val album: String,
    @ColumnInfo val albumArt: Uri?
)

val SONG_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Song>() {

    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem.id == newItem.id
    }

    /**
     * Should we turn all of the ifs into 1 statement?
     * But like with this its ordered in terms of what is most likely to change
     * So if something did change it will hopefully not have to compare as many members
     */
    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
        if (oldItem.name != newItem.name)
            return false

        if (oldItem.artist != newItem.artist)
            return false

        if (oldItem.albumArt != newItem.albumArt)
            return false

        return true
    }

}

object SongDiff {

    fun areContentsTheSameDeep(oldItem: Song, newItem: Song): Boolean {
        return oldItem.name == newItem.name &&
                oldItem.artist == newItem.artist &&
                oldItem.album == newItem.album &&
                oldItem.albumArt == newItem.albumArt &&
                oldItem.path == newItem.path
    }

}