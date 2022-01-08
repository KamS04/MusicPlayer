package com.kam.musicplayer.view.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.FragmentQueueBinding
import com.kam.musicplayer.services.MusicPlayerService
import com.kam.musicplayer.utils.mContext
import com.kam.musicplayer.view.adapters.QueueFragmentAdapter

/**
 * Displays the current queue in the [AudioPlayerService]
 * Also handles dragging the songs around in the queue
 * and letting the [AudioPlayerService] know of the users actions
 */
class QueueFragment : Fragment() {

    private var _binding: FragmentQueueBinding? = null
    private val mBinding: FragmentQueueBinding
        get() = _binding ?: throw Exception("Binding must not be accessed before creation or after destruction")

    private lateinit var mQueueAdapter: QueueFragmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQueueBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mQueueAdapter = QueueFragmentAdapter(R.drawable.ic_hamburger)

        mQueueAdapter.setOnActionListener(object: QueueFragmentAdapter.OnQueueActionListener {
            override fun onClick(position: Int) {
                MusicPlayerService.run { it.skipToSong(position) }
            }

            override fun onMove(from: Int, to: Int) {
                Log.i("KMUSIC", "$from $to")
                MusicPlayerService.run { it.moveSong(from, to) }
            }
        })

        mQueueAdapter.attachToRecyclerView(mBinding.queueRv)

        MusicPlayerService.scheduleTask(mContext, viewLifecycleOwner) { service ->
            with(service) {
                currentPosition.observe(viewLifecycleOwner) {
                    mQueueAdapter.setCurrentSong(it)
                }

                currentQueue.observe(viewLifecycleOwner) { queue ->
                    mQueueAdapter.submitList(queue)
                    mQueueAdapter.notifyDataSetChanged()
                }
            }

        }
    }

}