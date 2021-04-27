package com.kam.musicplayer.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
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

/**
 * Recursive functions checks whether a certain [view] is the child
 * of a particular [ViewGroup] or any of its children
 */
fun ViewGroup.isViewChild(view: View) : Boolean {
    for (child in children) {
        if (child == view)
            return true

        if (child is ViewGroup) {
            if (child.isViewChild(view))
                return true
        }
    }
    return false
}

/**
 * Recursive function returns a list of views that were hit by a [MotionEvent]
 * it is recursive as it not only checks direct children but also children of children
 */
fun ViewGroup.getHitViews(ev: MotionEvent): MutableList<View> {
    val output = ArrayList<View>()
    var bounds = Rect() // this thing is modified when child.getHitRect is called but I don't know if it needs to be a var, could test it or could just leave it
    for (child in children) {
        child.getHitRect(bounds)
        if (bounds.contains(ev.x.toInt(), ev.y.toInt())) {
            output.add(child)
            if (child is ViewGroup)
                output.addAll(child.getHitViews(ev))
        }
    }

    return output
}

/**
 * Searches a view to see if it contains a view with an id of [id]
 */
fun View.getChildById(id: Int): View? {
    if (id == id)
        return this
    if (this is ViewGroup) {
        for (child in children) {
            val get = child.getChildById(id)
            if (get != null) {
                return get
            }
        }
    }
    return null
}