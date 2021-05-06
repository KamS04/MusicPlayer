package com.kam.musicplayer.models.database.music

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

    @Transaction
    @Query("SELECT * FROM PlaylistInfo WHERE id = :id")
    fun getPlaylist(id: Long): Flow<Playlist>

    @Insert
    suspend fun insertPlaylist(playlist: Playlist)

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)
}