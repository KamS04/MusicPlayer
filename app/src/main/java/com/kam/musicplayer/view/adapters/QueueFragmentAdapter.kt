package com.kam.musicplayer.view.adapters

import android.content.Context
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.kam.musicplayer.R
import com.kam.musicplayer.utils.colorFromAttr

class QueueFragmentAdapter(
    context: Context,
    optionsIcon: Int,
    ) : SongsAdapter(context, optionsIcon) {

    private var mOnQActionListener: OnQueueActionListener? = null
    private val mQueueSimpleCallback = ItemMoveHelper()
    private val mQItemTouchHelper = ItemTouchHelper(mQueueSimpleCallback)

    private var mCurrentSong: Int = 0

    /**
     * [QueueFragmentAdapter] implements a custom action listener rather than using
     * [SongsAdapter.OnActionListener], but it still needs calls that the base listener
     * receives so it inserts this listener into the base listener allowing and makes the
     * the [QueueFragmentAdapter.OnQueueActionListener] as the front facing listener
     */
    init {
        super.setOnActionListener(object: OnActionListener {
            override fun onClick(position: Int) {
                mOnQActionListener?.onClick(position)
            }

            override fun onOptionClicked(view: View, viewHolder: ViewHolder) {
                // Ignored
            }

            override fun onOptionTouched(view: View, event: MotionEvent, viewHolder: ViewHolder) {
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    mQItemTouchHelper.startDrag(viewHolder)
                }
            }

        })

        mQueueSimpleCallback.setOnMoveFunction { from, to ->
            mOnQActionListener?.onMove(from, to)
        }
    }

    /**
     * This sets the background of the current song to
     * [R.color.color_current_song]
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val bgColor = if (position == mCurrentSong) R.attr.colorSpecialBackground else R.attr.colorCardBackground
        holder.mBinding.rootMcv.setCardBackgroundColor(context.colorFromAttr(bgColor))
    }

    /**
     * Attaches the ItemTouchHelper to the recyclerview
     */
    override fun attachToRecyclerView(recyclerView: RecyclerView) {
        super.attachToRecyclerView(recyclerView)

        mQItemTouchHelper.attachToRecyclerView(recyclerView)
    }

    @Deprecated("QueueFragmentAdapter has a custom listener.", ReplaceWith("Use setQueueActionListener instead"))
    override fun setOnActionListener(listener: OnActionListener) {
        throw Exception("On Action Listener is not allowed in Queue Fragment Listener. Use Wrapper QueueFragmentAdapter.setQueueActionListener")
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
    fun setQueueActionListener(onQueueActionListener: OnQueueActionListener) {
        mOnQActionListener = onQueueActionListener
    }

    /**
     * OnQueueActionListener adds the onMove function
     */
    interface OnQueueActionListener {
        fun onClick(position: Int)
        fun onMove(from: Int, to: Int)
    }

}
