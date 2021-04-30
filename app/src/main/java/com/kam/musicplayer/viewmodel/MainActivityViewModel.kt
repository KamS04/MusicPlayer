package com.kam.musicplayer.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kam.musicplayer.R
import com.kam.musicplayer.application.MusicApplication

class MainActivityViewModel(private val application: MusicApplication) : ViewModel() {

    private val appName = application.applicationContext.resources.getString(R.string.app_name)
    private val _title = MutableLiveData<String>(appName)
    val title: LiveData<String>
        get() = _title

    fun clearTitle() {
        _title.value = appName
    }
}