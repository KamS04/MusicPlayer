package com.kam.musicplayer.services

import android.content.*
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.telephony.TelephonyManager
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.kam.musicplayer.utils.between
import com.kam.musicplayer.models.entities.Song
import com.kam.musicplayer.utils.nvalue
import com.kam.musicplayer.utils.Constants
import com.kam.musicplayer.utils.Repeater
import com.kam.musicplayer.utils.Utils
import com.kam.musicplayer.utils.Utils.moveElement
import kotlin.random.Random

class MusicPlayerService : LifecycleService() {

    private var mediaPlayer: MediaPlayer? = null

    private val sharedPrefs: SharedPreferences?
        get() = applicationContext?.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE)

    //region Properties

    /**
     * These here are the properties. All of them either have
     * custom getters or setters many are LiveData so that others
     * can subscribe to them
     */

    private val mCurrentTime: MutableLiveData<Int> = MutableLiveData(0)
    val currentTime: LiveData<Int>
        get() = mCurrentTime

    private val mCurrentDuration: MutableLiveData<Int> = MutableLiveData(0)
    val currentDuration: LiveData<Int>
        get() = mCurrentDuration

    private val mCurrentPosition: MutableLiveData<Int> = MutableLiveData(0)
    val currentPosition: LiveData<Int>
        get() = mCurrentPosition

    private val mCurrentQueue: MutableLiveData<MutableList<Song>> = MutableLiveData(mutableListOf())
    val currentQueue: LiveData<MutableList<Song>>
        get() = mCurrentQueue

    private val hasAnotherSong: Boolean
        get() = mCurrentQueue.nvalue.size > mCurrentPosition.nvalue + 1

    private val mCurrentSong: MutableLiveData<Song?> = MutableLiveData(null)
    val currentSong: LiveData<Song?>
        get() = mCurrentSong

    private val mIsShuffleOn: MutableLiveData<Boolean> = MutableLiveData( sharedPrefs?.getBoolean(Constants.SHUFFLE, false) ?: false )
    val isShuffleOn: LiveData<Boolean>
        get() = mIsShuffleOn

    private val mIsRepeatOn: MutableLiveData<Boolean> = MutableLiveData( sharedPrefs?.getBoolean(Constants.REPEAT, false) ?: false )
    val isRepeatOn: LiveData<Boolean>
        get() = mIsRepeatOn

    private val mIsPlaying: MutableLiveData<Boolean> = MutableLiveData(false)
    val isPlaying: LiveData<Boolean>
        get() = mIsPlaying

    val hasQueue: Boolean
        get() = mCurrentQueue.value?.isNotEmpty() ?: false

    //endregion

    //region Members

    /**
     * Real swell guy, its a thing that runs the same function every 1000 milliseconds (1 second)
     * keeps track of where the mediaPlayer is in the song and also notifies any listeners of
     * changes. Also it can be paused @see [Repeater]
     */
    private val clock = object: Repeater(1000) {
        override fun onTick() {
            if (mIsPlaying.nvalue) {
                mCurrentTime.value = mediaPlayer!!.currentPosition
            }
        }
    }

    //endregion

    //region Overrides

    /**
     * Logs the start of the app
     * Creates the media player
     * starts observing values
     * starts observing queuedTasks
     * registers receivers
     */
    override fun onCreate() {
        super.onCreate()

        Log.i("APS", "Player Service started")

        instance = this

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                    AudioAttributes
                            .Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
            )
            setOnCompletionListener { playNextSongInQueue() }
        }

        mIsShuffleOn.observe(this) {
            sharedPrefs?.edit()?.putBoolean(Constants.SHUFFLE, it)?.apply()
        }

        mIsRepeatOn.observe(this) {
            sharedPrefs?.edit()?.putBoolean(Constants.REPEAT, it)?.apply()
        }

        mIsPlaying.observe(this) {
            if (it)
                clock.start()
            else
                clock.pause()
        }

        hasTasks.observe(this) {
            if (it) {
                for (task in queuedTasks) {
                    if (task.lifecycleOwner.lifecycle.currentState != Lifecycle.State.DESTROYED) {
                        task.action(this)
                    }
                }
                queuedTasks.clear()
            }
        }

        registerReceiver(SpeakerChangeReceiver(), IntentFilter(AudioManager.ACTION_HEADSET_PLUG))
        registerReceiver(OnCallReceiver(), IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // TODO call the following
        //startForeground(id, notification)
        // This will make it a foreground service

        return START_NOT_STICKY
    }

    //endregion

    //region Song/State Functions

    /**
     * These functions handle communicating with the [mediaPlayer]
     */

    /**
     * This sets the [mediaPlayer]'s data source
     * then it prepares and starts the [mediaPlayer]
     * It also sets [isPlaying] to true and sets the
     * [mCurrentSong]
     */
    private fun loadSong(song: Song) {
        mediaPlayer!!.reset()

        mediaPlayer!!.setDataSource(applicationContext, song.path)

        mediaPlayer!!.prepare()
        mediaPlayer!!.start()
        mIsPlaying.value = true
        mCurrentSong.value = song
        mCurrentDuration.value = mediaPlayer!!.duration
    }

    /**
     * Simply starts the player
     * Errors could occur if the [mediaPlayer]
     * is not prepared.
     */
    private fun startPlayer() {
        if (mediaPlayer != null && !mIsPlaying.nvalue) {
            mediaPlayer!!.start()
            mIsPlaying.value = true
        }
    }

    /**
     * pauser the player
     * errors could occur if the [mediaPlayer]
     * if not prepared
     */
    private fun pausePlayer() {
        if (mediaPlayer != null && mIsPlaying.nvalue) {
            mediaPlayer!!.pause()
            mIsPlaying.value = false
        }
    }

    /**
     * This moves the [mCurrentPosition] then it performs repeat
     * and end of queue checks. Then it loads the song using [loadSong]
     * This is called whenever the [mediaPlayer] is finished or when the
     * [mCurrentQueue] is started
     */
    private fun playNextSongInQueue(moveToNext: Boolean = true) {
        if (moveToNext)
            mCurrentPosition.value = mCurrentPosition.nvalue + 1

        if (mCurrentQueue.nvalue.size <= mCurrentPosition.nvalue) {
            when {
                mIsRepeatOn.nvalue -> {
                    mCurrentPosition.value = 0
                }
                else -> {
                    pausePlayer()
                    return
                }
            }
        }

        val song = mCurrentQueue.nvalue[mCurrentPosition.nvalue]

        loadSong(song)
    }

    //endregion

    //region FrontEnd Functions

    //region Toggles

    fun togglePlayPause() {
        if (mediaPlayer != null && mCurrentSong.value != null) {
            if (mIsPlaying.nvalue) {
                pausePlayer()
            } else {
                startPlayer()
            }
        }
    }

    /**
     * Toggle the shuffle and then changes the [mCurrentQueue] if need be
     */
    fun toggleShuffle() {
        mIsShuffleOn.value = !mIsShuffleOn.nvalue

        if (mIsShuffleOn.nvalue) {
            if (mCurrentQueue.nvalue.isNotEmpty()) {
                val song = mCurrentSong.nvalue

                val nQueue = Utils.shuffleList(mCurrentQueue.nvalue) as MutableList<Song>

                val nPos = nQueue.indexOf(song)
                Utils.swap(nQueue, 0, nPos)

                mCurrentQueue.value = nQueue

                mCurrentPosition.value = 0
            }
        }
    }

    fun toggleRepeat() {
        mIsRepeatOn.value = !mIsRepeatOn.nvalue
    }

    //endregion

    //region Skipper/Seek Functions

    /**
     * Skips to a specified [position]
     * in the [mCurrentQueue]
     */
    fun skipToSong(position: Int) {
        mCurrentPosition.value = position.between(0, mCurrentQueue.nvalue.size)
        playNextSongInQueue(false)
    }

    /** Restarts the song or goes back by 1 song */
    fun skipBackward() {
        if (mCurrentTime.nvalue < 10000)
            skipToSong(mCurrentPosition.nvalue - 1)
        else
            seek(0)
    }

    /** Goes forward 1 song */
    fun skipForward() {
        skipToSong(mCurrentPosition.nvalue + 1)
    }

    /**
     * Seeks to a specific [time] in the song
     * note time is in milliseconds
     */
    fun seek(time: Int) {
        try {
            mediaPlayer!!.seekTo(time)

            mCurrentTime.value = mediaPlayer!!.currentPosition
        } catch (e: Exception) {
            // WARNING This is bad code. But like had to do it
        }
    }

    //endregion

    //region Setters

    /**
     * Sets the queue
     * checks whether shuffling is necessary
     * if shuffle is on it puts the chosen song at the start of the [mCurrentQueue]
     * otherwise it leaves it as is
     */
    fun setQueue(queue: Collection<Song>, song: Song) {
        pausePlayer()

        var nQueue: MutableList<Song> = if (queue is MutableList<Song>) queue else queue.toMutableList()

        if (mIsShuffleOn.nvalue) {
            nQueue = Utils.shuffleList(nQueue) as MutableList<Song>
        }

        mCurrentPosition.value = if (mIsShuffleOn.nvalue) {
            val pos = nQueue.indexOf(song)
            Utils.swap(nQueue, 0, pos)
            0
        } else {
            nQueue.indexOf(song)
        }

        mCurrentQueue.value = nQueue

        playNextSongInQueue(false)
    }

    /**
     * moves a song from one position in the [mCurrentQueue]
     * to another notifies [queueChangeListeners] as necessary
     */
    fun moveSong(from: Int, to: Int) {
        mCurrentQueue.nvalue.moveElement(from, to)

        mCurrentPosition.value = mCurrentQueue.nvalue.indexOf(mCurrentSong.nvalue)
    }

    /**
     * Adds a song right after the [mCurrentPosition]
     */
    fun playNext(song: Song) {
        val nQueue = mCurrentQueue.nvalue.toMutableList()
        nQueue.add(mCurrentPosition.nvalue + 1, song)
        mCurrentQueue.value = nQueue
    }

    //endregion

    //endregion

    //region Receivers

    /**
     * Listens for when headphones are plugged in or removed
     * Pauses the player when either happens
     */
    private inner class SpeakerChangeReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_HEADSET_PLUG) {
                pausePlayer()
            }
        }
    }

    /**
     * Listens for when the phone receives a call
     * Pauses the player when this happens
     */
    private inner class OnCallReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
                pausePlayer()
            }
        }
    }

    //endregion

    companion object {
        /** static instance */
        private var instance: MusicPlayerService? = null

        /**
         * list containing tasks for the player to do when it has time
         */
        private val queuedTasks: MutableList<OnStartTask> = mutableListOf()

        /** wrapper for checking if the service is started */
        val isServiceRunning: Boolean
            get() = instance != null

        /**
         * Service will subscribe here to make sure listen
         * for new tasks
         */
        private val hasTasks: MutableLiveData<Boolean> = MutableLiveData(false)

        /**
         * runs the [action] instantaneously if
         * the service is running
         */
        fun run(action: (service: MusicPlayerService) -> Unit) {
            if (isServiceRunning)
                action(instance!!)
        }

        /**
         * adds the task to the queue and sets [hasTasks] to true
         * the service will perform them
         */
        fun scheduleTask(lifecycleOwner: LifecycleOwner, action: (service: MusicPlayerService) -> Unit) {
            queuedTasks.add( OnStartTask(lifecycleOwner, action) )
            hasTasks.value = true
        }

        /**
         * Creates a new session
         */
        fun createSession() : ScheduleSession {
            return ScheduleSession()
        }

        /**
         * runs all tasks added to the [session]
         */
        fun scheduleSession(session: ScheduleSession) {
            queuedTasks.addAll( session.tasks.values )
            hasTasks.value = true
        }

        /** Simple data class to hold a task and its caller */
        data class OnStartTask(
            val lifecycleOwner: LifecycleOwner,
            val action: (service: MusicPlayerService) -> Unit
        )

        /**
         * Schedule class to run tasks in batches
         */
        class ScheduleSession() {
            /**
             * holds tasks in key value pairs of Ids
             * this way we can edit or delete them
             */
            internal val tasks: MutableMap<Int, OnStartTask> = mutableMapOf()

            /**
             * generates an id that doesn't exist in the tasks hashmap
             */
            private fun generateId() : Int {
                var id: Int;
                do {
                    id = Random.nextInt()
                } while (tasks.containsKey(id))

                return id;
            }

            /**
             * adds a task to [tasks] by generating an id
             */
            fun scheduleTask(lifecycleOwner: LifecycleOwner, action: (service: MusicPlayerService) -> Unit): Int {
                val taskId = generateId()
                val task = OnStartTask(lifecycleOwner, action)

                tasks[taskId] = task

                return taskId
            }

            /**
             * edits the [lifecycleOwner] and the [action] of a task in [tasks]
             * based on the [taskId]
             */
            fun changeTask(taskId: Int, lifecycleOwner: LifecycleOwner, action: (service: MusicPlayerService) -> Unit) {
                val newTask = OnStartTask(lifecycleOwner, action)
                if (!tasks.containsKey(taskId))
                    throw Exception("Task Id $taskId does not exist")

                tasks[taskId] = newTask
            }

            /**
             * deletes a task in [tasks] based on the [taskId]
             */
            fun unScheduleTask(taskId: Int) {
                if (!tasks.containsKey(taskId))
                    throw Exception("Task Id $taskId does not exist")

                tasks.remove(taskId)
            }
        }
    }
}