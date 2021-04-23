package com.kam.musicplayer.models.database

import androidx.annotation.WorkerThread
import com.kam.musicplayer.models.database.songs.SongDao
import com.kam.musicplayer.models.entities.Song
import kotlinx.coroutines.flow.Flow

class MusicRepository(private val database: MusicRoomDatabase) {

    private val songDao: SongDao = database.songDao()

    @WorkerThread
    suspend fun insertUpdateSong(song: Song) {
        songDao.insertUpdateSong(song)
    }

    val allSongs: Flow<List<Song>> = songDao.getAllSongs()

    @WorkerThread
    suspend fun deleteSong(song: Song) {
        songDao.deleteSong(song)
    }

}