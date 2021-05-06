package com.kam.musicplayer.models.database.music

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kam.musicplayer.models.entities.Converters
import com.kam.musicplayer.models.entities.PlaylistInfo
import com.kam.musicplayer.models.entities.PlaylistSongCrossRef
import com.kam.musicplayer.models.entities.Song

@Database(entities = [Song::class, PlaylistInfo::class, PlaylistSongCrossRef::class], version = 1)
@TypeConverters(Converters::class)
abstract class MusicRoomDatabase : RoomDatabase() {

    abstract fun musicDao(): MusicDao

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