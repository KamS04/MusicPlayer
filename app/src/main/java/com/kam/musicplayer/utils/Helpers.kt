package com.kam.musicplayer.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.MutableLiveData
import com.kam.musicplayer.application.MusicApplication

val Activity.musicApplication: MusicApplication
    get() {
        return application as MusicApplication
    }

val <T> MutableLiveData<T>.nvalue: T
    get() {
        return value!!
    }

fun Int.between(min: Int, max: Int): Int {
    return this.coerceAtLeast(min).coerceAtMost(max)
}

val Context.isLargeDevice: Boolean
    get() {
        return when (resources.configuration.screenLayout) {
            Configuration.SCREENLAYOUT_SIZE_SMALL -> false
            Configuration.SCREENLAYOUT_SIZE_NORMAL -> false
            Configuration.SCREENLAYOUT_SIZE_LARGE -> true
            Configuration.SCREENLAYOUT_SIZE_XLARGE -> true
            else -> false
        }
    }