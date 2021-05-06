package com.kam.musicplayer.view.adapters

import android.content.Context
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class PlaylistSongsAdapter(
    context: Context,
    optionsIcon: Int,
) : SongsAdapter(context, optionsIcon) {

    private var mOnPlaylistActionListener: OnPlayListActionListener? = null
    private var mPlaylistSimpleCallback = ItemMoveHelper()
    private var mPItemTouchHelper = ItemTouchHelper(mPlaylistSimpleCallback)

    init {
        super.setOnActionListener(object: OnActionListener {
            override fun onClick(position: Int) {
                mOnPlaylistActionListener?.onClick(position)
            }

            override fun onOptionClicked(view: View, viewHolder: ViewHolder) {
                mOnPlaylistActionListener?.onOptionsClicked(view, viewHolder)
            }

            override fun onOptionTouched(view: View, event: MotionEvent, viewHolder: ViewHolder) {
                if (event.actionMasked == MotionEvent.ACTION_DOWN)
                    mPItemTouchHelper.startDrag(viewHolder)
            }

        })

        mPlaylistSimpleCallback.setOnMoveFunction { from, to ->
            mOnPlaylistActionListener?.onMove(from, to)
            notifyItemMoved(from, to)
        }
    }

    override fun attachToRecyclerView(recyclerView: RecyclerView) {
        super.attachToRecyclerView(recyclerView)

        mPItemTouchHelper.attachToRecyclerView(recyclerView)
    }

    /**
     * This is a method from the [SongsAdapter] but the [PlaylistDataAdapter] uses
     * a custom listener
     * @see [addQueueActionListener]]
     */
    @Deprecated("PlaylistSongsAdapter has a custom Listener.", ReplaceWith("Use setPlaylistListener instead"))
    override fun setOnActionListener(listener: OnActionListener) {
        throw Exception("On Action Listener is not allowed in Playlist Song Listener. Use Wrapper QueueFragmentAdapter.OnPlayListActionListenerr")
    }

    /**
     * The custom action listener that the [PlaylistSongsAdapter] uses
     */
    fun setPlaylistActionListener(listener: OnPlayListActionListener) {
        mOnPlaylistActionListener = listener
    }

    interface OnPlayListActionListener {
        fun onClick(position: Int)
        fun onMove(from: Int, to: Int)
        fun onOptionsClicked(view: View, viewHolder: ViewHolder)
    }
}