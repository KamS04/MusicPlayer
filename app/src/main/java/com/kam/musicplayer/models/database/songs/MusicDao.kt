package com.kam.musicplayer.models.database.songs

import androidx.room.*
import com.kam.musicplayer.models.entities.Playlist
import com.kam.musicplayer.models.entities.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdateSong(song: Song)

    @Query("SELECT * FROM SONGS_TABLE")
    fun getAllSongs(): Flow<List<Song>>

    @Delete
    suspend fun deleteSong(song: Song)

    @Transaction
    @Query("SELECT * FROM PlaylistInfo")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdatePlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)
}