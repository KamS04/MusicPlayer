package com.kam.musicplayer.view.adapters

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.ItemGenericBinding
import com.kam.musicplayer.utils.Utils
import com.kam.musicplayer.utils.Utils.getDimensionInPixels

/**
 * Generic Items Adapter, Inflates [R.layout.item_generic]
 * It is open so that other classes can add onto it.
 */
open class GenericItemsAdapter<T>(
    private val context: Context,
    diffUtil: DiffUtil.ItemCallback<T>,
    private val spanCount: Int = 2,
    val getDetails: (item: T) -> Details,
) : ListAdapter<T, GenericItemsAdapter.ViewHolder>(diffUtil) {

    private var mOnActionListener: OnActionListener? = null
    private val mVerticalSpaceDecorator = VerticalSpaceDecorator(
        spanCount,
        context.getDimensionInPixels(R.dimen.generic_item_size).toInt()
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGenericBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    /**
     * Bind the [holder]'s layout to data received
     * from the map function defined in the constructor
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val details = getDetails(item)

        with(holder.mBinding) {
            headingTv.text = details.heading
            subHeadingTv.text = details.subHeading

//            root.post {
//                Log.i("Item Size", root.width.toString())
//            }

            Utils.loadImage(context, imageIv, details.imageUri, details.placeHolder)

            root.setOnClickListener {
                mOnActionListener?.onClick(holder.adapterPosition)
            }
            holder.itemView.setOnLongClickListener {
                mOnActionListener?.onLongClick(holder)
                true
            }
        }
    }

    /**
     * Makes it easier for the user to attach this to a [RecyclerView]
     * It handles setting a layout manager
     */
    open fun attachToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = GridLayoutManager(context, spanCount)
        recyclerView.adapter = this
        recyclerView.addItemDecoration(mVerticalSpaceDecorator)
    }

    fun setActionListener(listener: OnActionListener) {
        mOnActionListener = listener
    }

    interface OnActionListener {
        fun onClick(position: Int)
        fun onLongClick(viewHolder: ViewHolder) {}
    }

    class ViewHolder(val mBinding: ItemGenericBinding) : RecyclerView.ViewHolder(mBinding.root)

    private class VerticalSpaceDecorator(
        private val spanCount: Int,
        private val viewSize: Int
    ) :RecyclerView.ItemDecoration() {

//        init {
//            Log.i("ViewSize", viewSize.toString())
//        }

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val margin = parent.width - (spanCount * viewSize)
            val padding = margin / (spanCount + 1)
            // Log.i("Padding", padding.toString())
            val isLeft = parent.getChildAdapterPosition(view) % spanCount == 0
            outRect.left = padding / ( if (isLeft) 1 else 2)
            outRect.right = padding / ( if (isLeft) 2 else 1)
            outRect.bottom = padding / 2
            outRect.top = padding / 2
        }

    }

    data class Details(
        val heading: String,
        val subHeading: String,
        val imageUri: Uri?,
        val placeHolderImage: Int? = null
    ) {
        val placeHolder: Int
            get() = placeHolderImage ?: R.drawable.ic_placeholder
    }
}