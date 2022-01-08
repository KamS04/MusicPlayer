package com.kam.musicplayer.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.kam.musicplayer.models.Album
import com.kam.musicplayer.models.Artist
import com.kam.musicplayer.models.database.MusicRepository
import com.kam.musicplayer.models.entities.Playlist
import com.kam.musicplayer.models.entities.Song
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MusicViewModel(private val repository: MusicRepository) : ViewModel() {

    val allSongs: LiveData<List<Song>> = repository.allSongs.asLiveData()

    val allAlbums: LiveData<List<Album>> = repository.allAlbums.asLiveData()

    val allArtists: LiveData<List<Artist>> = repository.allArtists.asLiveData()

    val allPlaylists: LiveData<List<Playlist>> = repository.allPlaylists.asLiveData()

    var allPlaylistsOnce: List<Playlist> = listOf()

    init {
        viewModelScope.launch {
            repository.allPlaylists.collect { playlist ->
                allPlaylistsOnce = playlist
            }
        }
    }

    fun getAlbum(name: String): LiveData<Album> = repository.getAlbum(name).asLiveData()

    fun getArtist(name: String): LiveData<Artist> = repository.getArtist(name).asLiveData()

    fun getPlaylist(id: Long): LiveData<Playlist> = repository.getPlaylist(id).asLiveData()

    fun createPlaylist(name: String, vararg songs: Song) = viewModelScope.launch {
        repository.createPlaylist(name, *songs)
    }

    fun addSongsToPlaylist(playlist: Playlist, vararg songs: Song) = viewModelScope.launch {
        repository.addSongsToPlaylist(playlist, *songs)
    }

    fun renamePlaylist(playlist: Playlist, newName: String) = viewModelScope.launch {
        repository.renamePlaylist(playlist, newName)
    }

    fun moveSongInPlaylist(playlist: Playlist, from: Int, to: Int) {
        Log.i("KMUSIC1", "ViewModel $viewModelScope")
        viewModelScope.launch {
            Log.i("KMUSIC2", "ViewModelScope")
            repository.moveSongInPlaylist(playlist, from, to)
        }
    }

    fun removeSongFromPlaylist(playlist: Playlist, index: Int) = viewModelScope.launch {
        repository.removeSongFromPlaylist(playlist, index)
    }

    fun updatePlaylist(playlist: Playlist) = viewModelScope.launch {
        repository.updatePlaylist(playlist)
    }

    fun deletePlaylist(playlist: Playlist) = viewModelScope.launch {
        repository.deletePlaylist(playlist)
    }

    fun refreshSongs() = viewModelScope.launch {
        repository.refreshSongs()
    }
}