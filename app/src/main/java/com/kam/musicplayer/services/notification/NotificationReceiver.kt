package com.kam.musicplayer.services.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kam.musicplayer.services.MusicPlayerService

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("NREC", "${intent.action}")
        when (intent.action) {
            NotificationConstants.ACTION_TOGGLE_PLAY -> {
                MusicPlayerService.run { it.togglePlayPause() }
            }
            NotificationConstants.ACTION_PREVIOUS -> {
                MusicPlayerService.run { it.skipBackward() }
            }
            NotificationConstants.ACTION_NEXT -> {
                MusicPlayerService.run { it.skipForward() }
            }
            NotificationConstants.ACTION_CLOSE_SERVICE -> {
                context.stopService(Intent(context, MusicPlayerService::class.java))
            }
        }
    }

}