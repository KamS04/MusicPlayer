package com.kam.musicplayer.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 * General class that holds stuff that multiple other
 * classes may need.
 */
object Utils {

    fun Context.getDimensionInPixels(dimenId: Int): Float {
        //Log.i("DIMENreal", "${resources.getDimension(dimenId)}")
        //Log.i("DIMENfixed", "${resources.getDimension(dimenId) * resources.displayMetrics.density}")
        return resources.getDimension(dimenId)
    }

    /**
     * shuffles a list
     */
    fun <T> shuffleList(list: List<T>): List<T> {
        return list.shuffled()
    }

    /**
     * moves elements in a [MutableList]
     */
    fun <T> MutableList<T>.moveElement(from: Int, to: Int) {
        val item = this[from]
        removeAt(from)
        add(to.coerceAtLeast(0).coerceAtMost(size), item)
    }

    /**
     * Set the height of a view
     * I dont know why I made this, I think I needed
     * for some custom made bottom sheet class I was trying to
     * make but then I found the greatness of [com.google.android.material]
     */
    fun setViewHeight(view: View, height: Int) {
        val lp = view.layoutParams
        lp.height = height
        view.layoutParams = lp
    }

    /**
     * Loads an image into a container with errors and null uris defaulting to a placeholder resource
     */
    fun loadImage(context: Context, container: ImageView, image: Uri?, placeholder: Int) {
        if (image != null) {
            Glide.with(context)
                .load(image)
                .placeholder(placeholder)
                .error(placeholder)
                .fitCenter()
                .into(container)
        } else {
            Glide.with(context)
                .load(placeholder)
                .fitCenter()
                .into(container)
        }
    }

    fun <T> swap(list: MutableList<T>, a: Int, b: Int) {
        val tmp = list[a]
        list[a] = list[b]
        list[b] = tmp
    }
}