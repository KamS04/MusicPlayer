package com.kam.musicplayer.models.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kam.musicplayer.models.database.songs.SongDao
import com.kam.musicplayer.models.entities.Convertors
import com.kam.musicplayer.models.entities.Song

@Database(entities = [Song::class], version = 1)
@TypeConverters(Convertors::class)
abstract class MusicRoomDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: MusicRoomDatabase? = null

        fun getDatabase(context: Context): MusicRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicRoomDatabase::class.java,
                    "music_database")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

}