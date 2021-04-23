package com.kam.musicplayer.models.database.songs

import androidx.room.*
import com.kam.musicplayer.models.entities.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdateSong(song: Song)

    @Query("SELECT * FROM SONGS_TABLE")
    fun getAllSongs(): Flow<List<Song>>

    @Delete
    suspend fun deleteSong(song: Song)

}