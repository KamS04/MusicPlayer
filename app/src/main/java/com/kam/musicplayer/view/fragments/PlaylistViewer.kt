package com.kam.musicplayer.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.FragmentDataViewerBinding
import com.kam.musicplayer.models.entities.Playlist
import com.kam.musicplayer.models.entities.PlaylistInfo
import com.kam.musicplayer.models.entities.Song
import com.kam.musicplayer.services.MusicPlayerService
import com.kam.musicplayer.utils.Utils
import com.kam.musicplayer.utils.Utils.moveElement
import com.kam.musicplayer.utils.mContext
import com.kam.musicplayer.utils.musicApplication
import com.kam.musicplayer.view.adapters.PlaylistSongsAdapter
import com.kam.musicplayer.view.adapters.SongsAdapter
import com.kam.musicplayer.viewmodel.MainActivityViewModel
import com.kam.musicplayer.viewmodel.MusicViewModel
import com.kam.musicplayer.viewmodel.factories.MainActivityViewModelFactory
import com.kam.musicplayer.viewmodel.factories.MusicViewModelFactory

class PlaylistViewer : Fragment() {

    private val mMainViewModel: MainActivityViewModel by viewModels {
        MainActivityViewModelFactory(requireActivity().musicApplication)
    }

    private val mViewModel: MusicViewModel by viewModels {
        MusicViewModelFactory(requireActivity().musicApplication)
    }

    private var _binding: FragmentDataViewerBinding? = null
    private val mBinding: FragmentDataViewerBinding
        get() = _binding ?: throw Exception("Binding must not be accessed before creation or after destruction")

    private var _songsAdapter: PlaylistSongsAdapter? = null
    private val mSongsAdapter: PlaylistSongsAdapter
        get() {
            if (_songsAdapter == null)
                initializeAdapter()
            return _songsAdapter!!
        }

    private var mPlaylist = Playlist(PlaylistInfo("Playlist", 0), mutableListOf())

    private var playlistLiveData: LiveData<Playlist>? = null

    private val observer = Observer<Playlist> { playlist ->
        mPlaylist = playlist

        showPlaylist()
    }

    private val mSongs: List<Song>
        get() = mPlaylist.songs

    private fun initializeAdapter() {
        _songsAdapter = PlaylistSongsAdapter(mContext, R.drawable.ic_kebab)

        _songsAdapter!!.setPlaylistActionListener(object: PlaylistSongsAdapter.OnPlayListActionListener {
            override fun onClick(position: Int) {
                MusicPlayerService.run { it.setQueue(mSongs, mSongs[position]) }
            }

            override fun onMove(from: Int, to: Int) {
                mPlaylist.songs.moveElement(from, to)
                mViewModel.updatePlaylist(mPlaylist)
            }

            override fun onOptionsClicked(view: View, viewHolder: SongsAdapter.ViewHolder) {
                val popup = PopupMenu(mContext, view)
                popup.inflate(R.menu.playlist_song_menu)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.remove_song -> {
                            if (viewHolder.adapterPosition != RecyclerView.NO_POSITION) {
                                mPlaylist.songs.removeAt(viewHolder.adapterPosition)
                                mViewModel.updatePlaylist(mPlaylist)
                            }
                        }
                        R.id.play_song -> {
                            onClick(viewHolder.adapterPosition)
                        }
                        R.id.play_next -> {
                            MusicPlayerService.run { it.playNext(mSongs[viewHolder.adapterPosition] )}
                        }
                    }
                    true
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataViewerBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(mMainViewModel) {
            selectedPlaylist.observe(viewLifecycleOwner) { liveData ->
                liveData?.let {
                    if (playlistLiveData != null)
                        playlistLiveData!!.removeObserver(observer)

                    playlistLiveData = it
                    playlistLiveData!!.observe(viewLifecycleOwner, observer)
                }
            }
        }
    }

    private fun showPlaylist() {
        mMainViewModel.setTitle(MainActivityViewModel.Controller.Playlist, mPlaylist.info.name)

        Utils.loadImage(mContext, mBinding.mainImageIv, mPlaylist.albumArt, R.drawable.ic_placeholder)

        mSongsAdapter.submitList(mSongs)

        if (mSongs.isEmpty()) {
            mBinding.dataRv.visibility = View.GONE
            mBinding.noDataTv.visibility = View.VISIBLE
        } else {
            mBinding.dataRv.visibility = View.VISIBLE
            mBinding.noDataTv.visibility = View.GONE
        }
    }


}