package com.kam.musicplayer.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.FragmentMiniPlayerBinding
import com.kam.musicplayer.services.MusicPlayerService
import com.kam.musicplayer.utils.Utils
import com.kam.musicplayer.utils.mContext

/**
 * Exactly what the name implies. It shows what song is playing
 * and then also has controls to work with the [AudioPlayerService]
 */
class MiniFragmentPlayer : Fragment() {

    private var _binding: FragmentMiniPlayerBinding? = null
    private val mBinding: FragmentMiniPlayerBinding
        get() = _binding ?: throw Exception("Binding must not be accessed before creation or after destruction")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMiniPlayerBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MusicPlayerService.scheduleTask(viewLifecycleOwner) { service ->

            service.currentSong.observe(viewLifecycleOwner) { song ->
                song?.let {
                    activity?.runOnUiThread {
                        mBinding.titleTv.text = it.name
                        mBinding.artistTv.text = it.artist

                        Utils.loadImage(
                            mContext,
                            mBinding.coverArtIv,
                            song.albumArt,
                            R.drawable.ic_placeholder
                        )
                    }
                }
            }

            service.isPlaying.observe(viewLifecycleOwner) { playing ->
                activity?.runOnUiThread {
                    val icon = if (playing) R.drawable.ic_pause else R.drawable.ic_play
                    mBinding.playPauseIb.setImageResource(icon)
                }
            }

        }

        with(mBinding) {

            playPauseIb.setOnClickListener {
                MusicPlayerService.run { it.togglePlayPause() }
            }

            previousIb.setOnClickListener {
                MusicPlayerService.run { it.skipBackward() }
            }

            nextIb.setOnClickListener {
                MusicPlayerService.run { it.skipForward() }
            }
        }

    }
}