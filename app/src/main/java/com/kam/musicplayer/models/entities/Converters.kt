package com.kam.musicplayer.models.entities

import android.net.Uri
import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromStringUri(path: String?): Uri? {
        return path?.let { Uri.parse(it) }
    }

    @TypeConverter
    fun fromUri(uri: Uri?): String? {
        return uri?.toString()
    }

}