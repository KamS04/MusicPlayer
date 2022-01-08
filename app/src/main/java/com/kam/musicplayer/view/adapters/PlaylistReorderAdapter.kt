package com.kam.musicplayer.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.ItemSongBinding
import com.kam.musicplayer.models.entities.Song
import com.kam.musicplayer.utils.Utils

class PlaylistReorderAdapter(
        private val optionsIcon: Int
) : RecyclerView.Adapter<SongsAdapter.ViewHolder>() {

    private var onMove: (from: Int, to: Int) -> Unit = { _, _ -> }

    private val mItemMoveHelper= ItemMoveHelper()
    private val mItemTouchHelper = ItemTouchHelper(mItemMoveHelper)

    private var mSongs = mutableListOf<Song>()

    init {
        mItemMoveHelper.setOnMoveFunction { from, to ->
            onMove(from, to)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsAdapter.ViewHolder {
        return SongsAdapter.ViewHolder(
                ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int = mSongs.size

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: SongsAdapter.ViewHolder, position: Int) {
        val song = mSongs[position]

        with (holder.mBinding) {
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
        }
    }

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = this

        mItemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun setOnActionListener(listener: (from: Int, to: Int) -> Unit) {
        onMove = listener
    }
    fun submitList(songs: MutableList<Song>) {
        mSongs = songs
        notifyDataSetChanged()
    }
}