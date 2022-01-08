package com.kam.musicplayer.view.adapters.concatable

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kam.musicplayer.databinding.SingleMessageBinding


class SingleMessageAdapter (
        private val message: String
) : RecyclerView.Adapter<SingleMessageAdapter.ViewHolder>() {

    private var mShowingMessage = false

    fun setShowing(shouldShow: Boolean) {
        if (shouldShow != mShowingMessage) {
            mShowingMessage = shouldShow
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                SingleMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mBinding.messageTv.text = message
    }

    override fun getItemCount(): Int = if (mShowingMessage) 1 else 0

    class ViewHolder(val mBinding: SingleMessageBinding) : RecyclerView.ViewHolder(mBinding.root)
}