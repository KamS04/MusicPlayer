package com.kam.musicplayer.viewmodel

import androidx.lifecycle.*
import com.kam.musicplayer.models.Album
import com.kam.musicplayer.models.Artist
import com.kam.musicplayer.models.database.MusicRepository
import com.kam.musicplayer.models.entities.Playlist
import com.kam.musicplayer.models.entities.Song
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class MusicViewModel(private val repository: MusicRepository) : ViewModel() {

    val allSongs: LiveData<List<Song>> = repository.allSongs.asLiveData()

    private val _allAlbums: MutableLiveData<List<Album>> = MutableLiveData(listOf())
    val allAlbums: LiveData<List<Album>>
        get() = _allAlbums

    private val _allArtists: MutableLiveData<List<Artist>> = MutableLiveData(listOf())
    val allArtists: LiveData<List<Artist>>
        get() = _allArtists

    val allPlaylists: LiveData<List<Playlist>> = repository.allPlaylists.asLiveData()

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }

    init {
        viewModelScope.launch {
            allSongs.asFlow().collect { songs ->
                _allAlbums.value = songs.asSequence()
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

                _allArtists.value = songs.asSequence()
                    .filter { it.artist.isNotEmpty() }
                    .map { it.artist }
                    .distinct()
                    .map { artist ->
                        Artist(
                            artist,
                            songs.filter { it.artist == artist }
                        )
                    }.sortedBy { it.name }
                    .toList()
            }
        }
    }

}