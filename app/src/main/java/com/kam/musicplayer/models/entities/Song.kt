package com.kam.musicplayer.models.entities

import android.net.Uri
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