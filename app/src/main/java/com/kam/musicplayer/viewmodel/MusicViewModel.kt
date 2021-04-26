package com.kam.musicplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.kam.musicplayer.models.database.MusicRepository
import com.kam.musicplayer.models.entities.Song

class MusicViewModel(private val repository: MusicRepository) : ViewModel() {

    val allSongs: LiveData<List<Song>> = repository.allSongs.asLiveData()
}