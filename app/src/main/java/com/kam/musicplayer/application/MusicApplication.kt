package com.kam.musicplayer.application

import android.app.Application
import com.kam.musicplayer.models.database.IODatabase
import com.kam.musicplayer.models.database.MusicRepository
import com.kam.musicplayer.models.database.music.MusicRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MusicApplication : Application() {

    private val scope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    private val database by lazy {
        MusicRoomDatabase.getDatabase(this)
    }

    private val ioDatabase by lazy {
        IODatabase(this, database.musicDao())
    }

    val repository by lazy {
        MusicRepository(scope, database, ioDatabase)
    }

}