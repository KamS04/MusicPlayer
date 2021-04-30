package com.kam.musicplayer.viewmodel

import androidx.lifecycle.*
import com.kam.musicplayer.models.Album
import com.kam.musicplayer.models.database.MusicRepository
import com.kam.musicplayer.models.entities.Song
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class MusicViewModel(private val repository: MusicRepository) : ViewModel() {

    val allSongs: LiveData<List<Song>> = repository.allSongs.asLiveData()

    private val _allAlbums: MutableLiveData<List<Album>> = MutableLiveData(listOf())
    val allAlbums: LiveData<List<Album>>
        get() = _allAlbums

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
            }
        }
    }

}