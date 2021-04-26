package com.kam.musicplayer.application

import android.app.Application
import com.kam.musicplayer.models.database.MusicRepository
import com.kam.musicplayer.models.database.MusicRoomDatabase

class MusicApplication : Application() {

    private val database by lazy {
        MusicRoomDatabase.getDatabase(this)
    }

    // TODO add Inject a IO Database System and then inject it into the Repository

    val repository by lazy {
        MusicRepository(database)
    }

}