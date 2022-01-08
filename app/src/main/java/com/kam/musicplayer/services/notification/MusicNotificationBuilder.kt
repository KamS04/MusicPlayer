package com.kam.musicplayer.services.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.media.session.MediaSession
import android.os.Build
import android.provider.MediaStore
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.kam.musicplayer.R
import com.kam.musicplayer.models.entities.Song
import com.kam.musicplayer.utils.Constants
import com.kam.musicplayer.view.activities.MainActivity

class MusicNotificationBuilder(private val context: Context, private val mediaSession: MediaSessionCompat) {

    private var mSong: Song? = null
    private var mIsPlaying: Boolean = false
    private var mIsCancellable: Boolean = false
    private var mIsApplicationRunning: Boolean = true

    fun setSong(song: Song): MusicNotificationBuilder {
        mSong = song
        return this
    }

    fun setPlaying(playing: Boolean): MusicNotificationBuilder {
        mIsPlaying = playing
        return this
    }

    fun setCancellable(cancellable: Boolean): MusicNotificationBuilder {
        mIsCancellable = cancellable
        return this
    }

    fun setApplicationRunning(isRunning: Boolean): MusicNotificationBuilder {
        mIsApplicationRunning = isRunning
        return this
    }

    fun build(): Notification {
        if (mSong != null) {
            val intentPrevious = Intent(context, NotificationReceiver::class.java).apply {
                action = NotificationConstants.ACTION_PREVIOUS
            }
            val intentTogglePlay = Intent(context, NotificationReceiver::class.java).apply {
                action = NotificationConstants.ACTION_TOGGLE_PLAY
            }
            val intentNext = Intent(context, NotificationReceiver::class.java).apply {
                action = NotificationConstants.ACTION_NEXT
            }

            val pendingPrevious = PendingIntent.getBroadcast(
                context,
                0,
                intentPrevious,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val pendingTogglePlay = PendingIntent.getBroadcast(
                context,
                0,
                intentTogglePlay,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val pendingNext = PendingIntent.getBroadcast(
                context,
                0,
                intentNext,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val activityIntent = Intent(context, MainActivity::class.java)
//                    .apply {
//                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
//            }

            val pendingActivity = PendingIntent.getActivity(context, 1, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val albumArt = if (mSong!!.albumArt != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, mSong!!.albumArt!!))
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, mSong!!.albumArt)
                }
            } else BitmapFactory.decodeResource(context.resources, R.drawable.ic_placeholder)

            val builder = NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentTitle(mSong!!.name)
                .setContentText(mSong!!.artist)
                .setContentIntent(pendingActivity)
                .setSmallIcon(R.drawable.ic_music)
                .setLargeIcon(albumArt)
                .addAction(
                    NotificationCompat.Action.Builder(
                        R.drawable.ic_previous,
                        "Previous",
                        pendingPrevious
                    ).build()
                ).addAction(
                    NotificationCompat.Action.Builder(
                        if (mIsPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                        if (mIsPlaying) "Pause" else "Play",
                        pendingTogglePlay
                    ).build()
                ).addAction(
                    NotificationCompat.Action.Builder(
                        R.drawable.ic_next,
                        "Next",
                        pendingNext
                    ).build()
                ).setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        //.setMediaSession(mediaSession.sessionToken)
                )


            return builder.build()
        } else {
            val builder = NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID)
                    .setOngoing(true)
                    .setContentTitle("No Song Playing")
                    .setSmallIcon(R.drawable.ic_music)
                    .setPriority(NotificationCompat.PRIORITY_LOW)

            val closeServiceIntent = Intent(context, NotificationReceiver::class.java).apply {
                action = NotificationConstants.ACTION_CLOSE_SERVICE
            }

            val closePendingIntent = PendingIntent.getBroadcast(context, 0, closeServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            builder.setContentIntent(closePendingIntent)

            return builder.build()
        }
    }
}