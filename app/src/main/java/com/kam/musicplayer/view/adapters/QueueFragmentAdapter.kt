package com.kam.musicplayer.view.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.ItemSongBinding
import com.kam.musicplayer.models.entities.Song
import com.kam.musicplayer.utils.Utils
import com.kam.musicplayer.utils.colorFromAttr

class QueueFragmentAdapter(
    protected val optionsIcon: Int,
    ) : RecyclerView.Adapter<SongsAdapter.ViewHolder>() {

    private var mOnActionListener: OnQueueActionListener? = null
    private val mQueueSimpleCallback = ItemMoveHelper()
    private val mItemTouchHelper = ItemTouchHelper(mQueueSimpleCallback)

    private var mCurrentSong: Int = 0
    private var _queue: MutableList<Song> = mutableListOf()

    /**
     * [QueueFragmentAdapter] implements a custom action listener rather than using
     * [SongsAdapter.OnActionListener], but it still needs calls that the base listener
     * receives so it inserts this listener into the base listener allowing and makes the
     * the [QueueFragmentAdapter.OnQueueActionListener] as the front facing listener
     */
    init {
        mQueueSimpleCallback.setOnMoveFunction { from, to ->
            mOnActionListener?.onMove(from, to)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsAdapter.ViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongsAdapter.ViewHolder(binding)
    }

    override fun getItemCount(): Int = _queue.size


    /**
     * This sets the background of the current song to
     * [R.color.color_current_song]
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: SongsAdapter.ViewHolder, position: Int) {
        val song = _queue[position]

        with(holder.mBinding) {
            root.setOnClickListener {
                mOnActionListener?.onClick(holder.bindingAdapterPosition)
            }

            titleTv.text = song.name
            artistTv.text = song.artist

            optionsIb.setImageResource(optionsIcon)

            optionsIb.setOnTouchListener { view, motionEvent ->
                if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                    mItemTouchHelper.startDrag(holder)
                }
                if (motionEvent.action == MotionEvent.ACTION_UP)
                    view.performClick()
                true
            }

            song.albumArt?.let {
                Utils.loadImage(holder.itemView.context, coverIv, song.albumArt, R.drawable.ic_placeholder)
            }

            val bgColor = if (position == mCurrentSong) R.attr.colorSpecialBackground else R.attr.colorCardBackground
            rootMcv.setCardBackgroundColor(holder.itemView.context.colorFromAttr(bgColor))
        }


    }

    /**
     * Attaches the ItemTouchHelper to the recyclerview
     */
    fun attachToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager =
                LinearLayoutManager(recyclerView.context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = this

        mItemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun setCurrentSong(currentSong: Int) {
        val oldSong = mCurrentSong
        mCurrentSong = currentSong
        notifyItemChanged(oldSong)
        notifyItemChanged(currentSong)
    }

    /**
     * The custom action listener that the [QueueFragmentAdapter] uses
     */
    fun setOnActionListener(onQueueActionListener: OnQueueActionListener) {
        mOnActionListener = onQueueActionListener
    }

    fun submitList(songs: List<Song>) {
        _queue = songs.toMutableList()
        notifyDataSetChanged()
    }

    /**
     * OnQueueActionListener adds the onMove function
     */
    interface OnQueueActionListener {
        fun onClick(position: Int)
        fun onMove(from: Int, to: Int)
    }

}
