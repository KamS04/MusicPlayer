package com.kam.musicplayer.models.database

import android.net.Uri
import android.util.Log
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

    val allSongs: Flow<List<Song>> = flow {
        musicDao.getAllSongs().collect { songs ->
            emit(
                songs.sortedBy { it.name }
            )
        }
    }

    val allPlaylists: Flow<List<Playlist>> = flow {
        musicDao.getAllPlaylists().collect { playlists ->
            emit(
                playlists.sortedBy { it.info.name }
            )
        }
    }

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

    @WorkerThread
    suspend fun getSong(uri: Uri): Song? {
        return ioDatabase.getSong(uri)
    }

    suspend fun refreshSongs() {
        val newSongs = ioDatabase.refreshAllSongs()
        musicDao.getAllSongs().take(1).collect { oldSongs ->

            val oldSongsMap = oldSongs.map { it.songId to it }.toMap()
            val newSongsMap = newSongs.map { it.songId to it }.toMap()

            val differentSongs = mutableListOf<Song>()

            for (newSong in newSongs) {

                val oldSong = oldSongsMap[newSong.songId]
                if (oldSong == null || !SongDiff.areContentsTheSameDeep(oldSong, newSong))
                    differentSongs.add(newSong)

            }

            val deletedSongs = oldSongs.filter { newSongsMap[it.songId] == null }

            insertUpdateSongs(differentSongs)

            deleteSongs(deletedSongs)

//            differentSongs.forEach {
//                insertUpdateSong(it)
//            }
//
//            deletedSongs.forEach {
//                deleteSong(it)
//            }

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
    suspend fun insertUpdateSongs(songs: List<Song>) {
        musicDao.insertUpdateSongs(songs)
    }

    @WorkerThread
    suspend fun deleteSong(song: Song) {
        musicDao.deleteSong(song)
    }

    @WorkerThread
    suspend fun deleteSongs(song: List<Song>) {
        musicDao.deleteSongs(song)
    }

    fun getPlaylist(id: Long): Flow<Playlist> {
        return musicDao.getPlaylist(id)
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
        musicDao.insertPlaylist(name, *songs)
    }

    @WorkerThread
    suspend fun renamePlaylist(playlist: Playlist, newName: String) {
        musicDao.updatePlaylistInfo(
                PlaylistInfo(newName, playlist.info.playlistId)
        )
    }

    @WorkerThread
    suspend fun addSongsToPlaylist(playlist: Playlist, vararg songs: Song) {
        musicDao.addSongsToPlaylist(playlist, *songs)
    }

    @WorkerThread
    suspend fun moveSongInPlaylist(playlist: Playlist, from: Int, to: Int) {
        Log.i("KMUSIC3", "Repository")
        musicDao.moveSongInPlaylist(playlist, from, to)
    }

    @WorkerThread
    suspend fun removeSongFromPlaylist(playlist: Playlist, index: Int) {
        musicDao.removeSongFromPlaylist(playlist, index)
    }

}