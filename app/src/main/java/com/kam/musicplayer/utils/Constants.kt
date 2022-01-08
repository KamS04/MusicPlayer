package com.kam.musicplayer.utils

import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat

/**
 * General class to hold constant data that multiple classes may need.
 */
object Constants {

    const val MEDIA_SESSION_TAG = "K_PLAYER"
    const val NOTIFICATION_ID = 1

    /** Magic string that contains artworks, it works because Stack Overflow said so  */
    val artworksUri: Uri = Uri.parse("content://media/external/audio/albumart")

    const val SHARED_PREFS = "com.kam.musicplayer.SHARED_PREFS"
    const val SHUFFLE = "com.kam.musicplayer.SHUFFLE"
    const val REPEAT = "com.kam.musicplayer.REPEAT"

    const val READ_STORAGE_PERMISSION_CODE = 1
    const val READ_PHONE_STATE_PERMISSION_CODE = 2

    const val PERMISSION_RATIONALE = "It looks like you have turned off permissions " +
            "required for this feature. It can be enabled under Application Settings"
}