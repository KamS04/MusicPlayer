package com.kam.musicplayer.viewmodel.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kam.musicplayer.application.MusicApplication
import com.kam.musicplayer.viewmodel.MainActivityViewModel
import com.kam.musicplayer.viewmodel.MusicViewModel

class MainActivityViewModelFactory(private val application: MusicApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}