package com.kam.musicplayer.view.adapters.concatable

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kam.musicplayer.databinding.InfoHeaderBinding
import com.kam.musicplayer.utils.Utils

class InfoHeaderAdapter(
        private val default: Int
) : RecyclerView.Adapter<InfoHeaderAdapter.ViewHolder>() {

    private var mCurrentUri : Uri? = null
    private var actionBtn: Int? = null
    private var mOnClick: ((holder: ViewHolder) -> Unit)? = null

    fun resetData(uri: Uri?) {
        mCurrentUri = uri
        actionBtn = null
        notifyDataSetChanged()
    }

    fun showActionBtn(resource: Int, onClick: (holder: ViewHolder) -> Unit) {
        actionBtn = resource
        mOnClick = onClick
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                InfoHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Utils.loadImage(holder.itemView.context, holder.mBinding.imageIv, mCurrentUri, default)

        holder.mBinding.actionBtnIb.visibility = if (actionBtn == null) View.GONE else View.VISIBLE

        actionBtn?.let {
            holder.mBinding.actionBtnIb.setImageResource(it)
            holder.mBinding.actionBtnIb.setOnClickListener { mOnClick?.let{ action -> action(holder)  } }
        }

    }

    override fun getItemCount(): Int = 1

    class ViewHolder(val mBinding: InfoHeaderBinding) : RecyclerView.ViewHolder(mBinding.root)
}