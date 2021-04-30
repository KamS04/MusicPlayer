package com.kam.musicplayer.view.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.ItemSongBinding
import com.kam.musicplayer.models.entities.SONG_DIFF_CALLBACK
import com.kam.musicplayer.models.entities.Song
import com.kam.musicplayer.utils.Utils

open class SongsAdapter(
    private val context: Context,
    protected val optionsIcon: Int? = null,
) : ListAdapter<Song, SongsAdapter.ViewHolder>(SONG_DIFF_CALLBACK) {

    private var mOnActionListener: OnActionListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    /**
     * Binds the layout to the model
     *
     * Checks if the data list is empty,
     * if list is empty, it hides the actual layout and instead shows the [emptyDataMessage]
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = getItem(position)

        with(holder.mBinding) {
            root.setOnClickListener {
                mOnActionListener?.onClick(holder.adapterPosition)
            }

            titleTv.text = song.name
            artistTv.text = song.artist

            optionsIcon?.let {
                optionsIb.setImageResource(it)
            }
            optionsIb.setOnTouchListener { view, motionEvent ->
                mOnActionListener?.onOptionTouched(view, motionEvent, holder)
                if (motionEvent.action == MotionEvent.ACTION_UP)
                    view.performClick()
                true
            }

            optionsIb.setOnClickListener {
                mOnActionListener?.onOptionClicked(it, holder)
            }

            song.albumArt?.let {
                Utils.loadImage(context, coverIv, song.albumArt, R.drawable.ic_placeholder)
            }
        }
    }

    /**
     * Makes it easier for the user to attach this to a [RecyclerView]
     * It handles setting a layout manager
     */
    open fun attachToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = this
    }

    fun setOnActionListener(listener: OnActionListener) {
        mOnActionListener = listener
    }

    interface OnActionListener {
        fun onClick(position: Int)
        fun onOptionClicked(view: View, viewHolder: ViewHolder)
        fun onOptionTouched(view: View, event: MotionEvent, viewHolder: ViewHolder)
    }

    class ViewHolder(val mBinding: ItemSongBinding) : RecyclerView.ViewHolder(mBinding.root)

}