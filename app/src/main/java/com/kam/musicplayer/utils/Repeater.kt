package com.kam.musicplayer.utils

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.Dispatchers

/**
 * I do not like the name of this.
 * This class runs a function at every [mInterval]
 */
abstract class Repeater(interval: Int) {

    /**
     * This is the handler used to recursively call and delay the timer.
     * It is a property in order to save time and resources
     * in creating a new handler at every interval
     */
    private var mHandler: Handler? = null

    private val mInterval = interval

    /** This controls whether the timer should continue running */
    private var isStopped = true

    /**
     * This is the main timer Runnable.
     * It recursively calls itself but uses [mHandler] to delay its execution by [mInterval]
     * This timer also checks [isStopped] each time. If [isStopped] is true it halts its execution.
     */
    private val clock = object: Runnable {
        override fun run() {
            if (!isStopped) {
                tick()
                mHandler!!.postDelayed(this, mInterval.toLong())
            }
        }
    }

    /** Runs at every interval. */
    private fun tick() {
        onTick()
    }

    /**
     *  public function to start the timer
     *  this only starts the timer if [isStopped] is false
     */
    fun start() {
        if (isStopped) {
            if (mHandler != null) {
                mHandler!!.removeCallbacks(clock)
            } else {
                mHandler = Handler(Looper.getMainLooper())
            }

            isStopped = false
            mHandler!!.post(clock)
        }
    }

    /** pauses the timer obviously */
    fun pause() {
        isStopped = true
    }
    /** this should be overridden by whatever class calls this */
    abstract fun onTick()
}