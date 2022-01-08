package com.kam.musicplayer.view.adapters

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Extension of [ItemTouchHelper.SimpleCallback]
 */
class ItemMoveHelper : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    ItemTouchHelper.UP or ItemTouchHelper.DOWN
) {
    private var mFrom: Int = 0
    private var mTo: Int = 0

    private var onMove: ((from: Int, to: Int) -> Unit)? = null

    /**
     * Called every time an item moves up or down
     */
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        mTo = target.bindingAdapterPosition
        recyclerView.adapter?.notifyItemMoved(viewHolder.bindingAdapterPosition, mTo)
        return true
    }

    /**
     * Called when an item is touched
     * Changes [mFrom] so that swapping can happen easily
     * Also changes the selected viewholder's alpha to 0.5
     */
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        viewHolder?.let {
            mFrom = it.bindingAdapterPosition
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            viewHolder?.itemView?.alpha = 0.5f
        }
    }

    /**
     * Called when the user lets go of the item
     * Reset alpha
     * notifies the listener to the onMove
     */
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        viewHolder.itemView.alpha = 1f
        onMove?.let {
            it(mFrom, mTo)
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Ignored
    }

    fun setOnMoveFunction(moveFunction: (from: Int, to: Int) -> Unit) {
        onMove = moveFunction
    }

}