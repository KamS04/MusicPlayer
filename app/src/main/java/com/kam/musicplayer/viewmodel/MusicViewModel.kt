package com.kam.musicplayer.viewmodel

import androidx.lifecycle.*
import com.kam.musicplayer.models.Album
import com.kam.musicplayer.models.Artist
import com.kam.musicplayer.models.database.MusicRepository
import com.kam.musicplayer.models.entities.Playlist
import com.kam.musicplayer.models.entities.Song
import kotlinx.coroutines.launch

class MusicViewModel(private val repository: MusicRepository) : ViewModel() {

    val allSongs: LiveData<List<Song>> = repository.allSongs.asLiveData()

    val allAlbums: LiveData<List<Album>> = repository.allAlbums.asLiveData()

    val allArtists: LiveData<List<Artist>> = repository.allArtists.asLiveData()

    val allPlaylists: LiveData<List<Playlist>> = repository.allPlaylists.asLiveData()

    fun getAlbum(name: String): LiveData<Album> = repository.getAlbum(name).asLiveData()

    fun getArtist(name: String): LiveData<Artist> = repository.getArtist(name).asLiveData()

    fun getPlaylist(id: Long): LiveData<Playlist> = repository.getPlaylist(id).asLiveData()

    fun createPlaylist(name: String, vararg songs: Song) = viewModelScope.launch {
        repository.createPlaylist(name, *songs)
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