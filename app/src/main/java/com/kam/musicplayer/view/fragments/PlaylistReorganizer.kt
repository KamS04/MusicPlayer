package com.kam.musicplayer.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.SingleRecyclerViewBinding
import com.kam.musicplayer.models.entities.Playlist
import com.kam.musicplayer.utils.musicApplication
import com.kam.musicplayer.view.adapters.PlaylistReorderAdapter
import com.kam.musicplayer.viewmodel.MainActivityViewModel
import com.kam.musicplayer.viewmodel.MusicViewModel
import com.kam.musicplayer.viewmodel.factories.MainActivityViewModelFactory
import com.kam.musicplayer.viewmodel.factories.MusicViewModelFactory

class PlaylistReorganizer : Fragment() {

    private val mViewModel: MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory(requireActivity().musicApplication)
    }

    private val mMusicModel: MusicViewModel by viewModels {
        MusicViewModelFactory(requireActivity().musicApplication)
    }

    private var _binding: SingleRecyclerViewBinding? = null
    private val mBinding: SingleRecyclerViewBinding
        get() = _binding ?: throw Exception("Binding must not be accessed before creation or after destruction of fragment")

    private var _songsAdapter: PlaylistReorderAdapter? = null
    private val mSongsAdapter: PlaylistReorderAdapter
        get() {
            if (_songsAdapter == null)
                initializeSongsAdapter()
            return _songsAdapter!!
        }

    private var playlistLiveData: LiveData<Playlist>? = null

    private var currentPlaylist: Playlist? = null

    private val mPlaylistObserver = Observer<Playlist> { playlist ->
        currentPlaylist = playlist
        Log.i("KMUSIC", "PR received stuff")
        mSongsAdapter.submitList(currentPlaylist!!.songs.toMutableList())
    }

    private fun initializeSongsAdapter() {
        _songsAdapter = PlaylistReorderAdapter(R.drawable.ic_hamburger)

        _songsAdapter!!.setOnActionListener { from, to ->
            currentPlaylist?.let {
                Log.i("KMUSIC", "PlaylistReorganizer $mMusicModel")
                mMusicModel.moveSongInPlaylist(it, from, to)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = SingleRecyclerViewBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel.selectedPlaylist.observe(viewLifecycleOwner) { liveData ->
            liveData?.let {
                if (playlistLiveData != null)
                    playlistLiveData!!.removeObserver(mPlaylistObserver)

                playlistLiveData = it
                playlistLiveData!!.observe(viewLifecycleOwner, mPlaylistObserver)
            }
        }

        mSongsAdapter.attachToRecyclerView(mBinding.recyclerview)
    }

}