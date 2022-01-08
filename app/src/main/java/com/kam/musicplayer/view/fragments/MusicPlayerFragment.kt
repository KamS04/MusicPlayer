package com.kam.musicplayer.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.FragmentMusicPlayerBinding
import com.kam.musicplayer.services.MusicPlayerService
import com.kam.musicplayer.utils.Utils
import com.kam.musicplayer.utils.colorFromAttr
import com.kam.musicplayer.utils.mContext

/**
 * Expanded version of [MinimizedPlayerFragment]
 * Essentially this shows what song is playing, also shows the current position in the song
 * that the [AudioPlayerService] is at
 * It also has controls for communicating with the [AudioPlayerService]
 */
class MusicPlayerFragment : Fragment() {

    private var _binding: FragmentMusicPlayerBinding? = null
    private val mBinding: FragmentMusicPlayerBinding
        get() = _binding ?: throw Exception("Binding must not be accessed before creation or after destruction")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMusicPlayerBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MusicPlayerService.scheduleTask(mContext, viewLifecycleOwner) { service ->
            with(service) {
                currentSong.observe(viewLifecycleOwner) { song ->
                    activity?.runOnUiThread {
                        if (song != null) {
                            mBinding.titleTv.text = song.name
                            mBinding.artistTv.text = song.artist
                            Utils.loadImage(mContext, mBinding.coverArtIv, song.albumArt, R.drawable.ic_placeholder)
                        } else {
                            mBinding.titleTv.text = ""
                            mBinding.artistTv.text = ""
                            mBinding.coverArtIv.setImageResource(R.drawable.ic_placeholder)
                        }
                    }
                }

                isPlaying.observe(viewLifecycleOwner) { playing ->
                    mBinding.playPauseIb.setImageResource(if (playing) R.drawable.ic_pause else R.drawable.ic_play)
                }

                isRepeatOn.observe(viewLifecycleOwner) { repeat ->
                    mBinding.repeatIb.setColorFilter(
                        mContext.colorFromAttr(
                            if (repeat) R.attr.colorSelected else R.attr.colorDeselected
                        )
                    )
                }

                isShuffleOn.observe(viewLifecycleOwner) { shuffle ->
                    mBinding.shuffleIb.setColorFilter(
                        mContext.colorFromAttr(
                            if (shuffle) R.attr.colorSelected else R.attr.colorDeselected
                        )
                    )
                }

                currentTime.observe(viewLifecycleOwner) { time ->
                    mBinding.seekbarSb.progress = time
                }

                currentDuration.observe(viewLifecycleOwner) { duration ->
                    mBinding.seekbarSb.max = duration
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

                shuffleIb.setOnClickListener {
                    MusicPlayerService.run { it.toggleShuffle() }
                }

                repeatIb.setOnClickListener {
                    MusicPlayerService.run { it.toggleRepeat() }
                }

                seekbarSb.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser)
                            MusicPlayerService.run { it.seek(progress) }
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {
                        // Ignored
                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {
                        // Ignored
                    }

                })
            }
        }
    }

}