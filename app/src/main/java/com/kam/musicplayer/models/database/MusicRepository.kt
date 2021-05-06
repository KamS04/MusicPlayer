package com.kam.musicplayer.models.database

import androidx.annotation.WorkerThread
import com.kam.musicplayer.models.Album
import com.kam.musicplayer.models.Artist
import com.kam.musicplayer.models.database.music.MusicDao
import com.kam.musicplayer.models.database.music.MusicRoomDatabase
import com.kam.musicplayer.models.entities.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MusicRepository(private val scope: CoroutineScope, private val database: MusicRoomDatabase, private val ioDatabase: IODatabase) {

    private val musicDao: MusicDao = database.musicDao()

    val allSongs: Flow<List<Song>> = musicDao.getAllSongs()

    val allPlaylists: Flow<List<Playlist>> = musicDao.getAllPlaylists()

    val allAlbums: Flow<List<Album>> = flow {
        allSongs.collect { songs ->
            emit(
                songs.asSequence()
                    .filter { it.album.isNotEmpty() }
                    .map { it.album }
                    .distinct()
                    .map { albumName ->
                        Album(
                            albumName,
                            songs.filter { it.album == albumName }
                        )
                    }.sortedBy { it.name }
                    .toList()
            )
        }
    }

    val allArtists: Flow<List<Artist>> = flow {
        allSongs.collect { songs ->
            emit(
                songs.asSequence()
                    .filter { it.artist.isNotEmpty() }
                    .map { it.artist }
                    .distinct()
                    .map { artistName ->
                        Artist(
                            artistName,
                            songs.filter { it.artist == artistName }
                        )
                    }.sortedBy { it.name }
                    .toList()
            )
        }
    }

    init {
        scope.launch {
            refreshSongs()
        }
    }

    suspend fun refreshSongs() {
        val newSongs = ioDatabase.refreshAllSongs()
        musicDao.getAllSongs().take(1).collect { oldSongs ->

            val oldSongsMap = oldSongs.map { it.id to it }.toMap()
            val newSongsMap = newSongs.map { it.id to it }.toMap()

            val differentSongs = mutableListOf<Song>()

            for (newSong in newSongs) {

                val oldSong = oldSongsMap[newSong.id]
                if (oldSong == null || SongDiff.areContentsTheSameDeep(oldSong, newSong))
                    differentSongs.add(newSong)

            }

            val deletedSongs = oldSongs.filter { newSongsMap[it.id] == null }

            differentSongs.forEach {
                insertUpdateSong(it)
            }

            deletedSongs.forEach {
                deleteSong(it)
            }

        }
    }

    fun getAlbum(name: String): Flow<Album> {
        return flow {
            allAlbums.collect { albums ->
                emit( albums.first { it.name == name } )
            }
        }
    }

    fun getArtist(name: String): Flow<Artist> {
        return flow {
            allArtists.collect { artist ->
                emit( artist.first { it.name == name } )
            }
        }
    }

    @WorkerThread
    suspend fun insertUpdateSong(song: Song) {
        musicDao.insertUpdateSong(song)
    }

    @WorkerThread
    suspend fun deleteSong(song: Song) {
        musicDao.deleteSong(song)
    }

    fun getPlaylist(id: Long): Flow<Playlist> {
        return musicDao.getPlaylist(id)
    }

    @WorkerThread
    suspend fun insertPlaylist(playlist: Playlist) {
        musicDao.insertPlaylist(playlist)
    }

    @WorkerThread
    suspend fun updatePlaylist(playlist: Playlist) {
        musicDao.updatePlaylist(playlist)
    }

    @WorkerThread
    suspend fun deletePlaylist(playlist: Playlist) {
        musicDao.deletePlaylist(playlist)
    }

    @WorkerThread
    suspend fun createPlaylist(name: String, vararg songs: Song) {
        val playlist = Playlist(
                PlaylistInfo(name),
                songs.toMutableList()
        )
        insertPlaylist(playlist)
    }


}