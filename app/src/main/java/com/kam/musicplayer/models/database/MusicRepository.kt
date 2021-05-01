package com.kam.musicplayer.models.database

import androidx.annotation.WorkerThread
import com.kam.musicplayer.models.database.songs.MusicDao
import com.kam.musicplayer.models.entities.Playlist
import com.kam.musicplayer.models.entities.Song
import kotlinx.coroutines.flow.Flow

class MusicRepository(private val database: MusicRoomDatabase) {

    private val musicDao: MusicDao = database.musicDao()

    @WorkerThread
    suspend fun insertUpdateSong(song: Song) {
        musicDao.insertUpdateSong(song)
    }

    val allSongs: Flow<List<Song>> = musicDao.getAllSongs()

    @WorkerThread
    suspend fun deleteSong(song: Song) {
        musicDao.deleteSong(song)
    }

    val allPlaylists: Flow<List<Playlist>> = musicDao.getAllPlaylists()

    @WorkerThread
    suspend fun insertUpdatePlaylist(playlist: Playlist) {
        musicDao.insertUpdatePlaylist(playlist)
    }

    @WorkerThread
    suspend fun deletePlaylist(playlist: Playlist) {
        musicDao.deletePlaylist(playlist)
    }

}