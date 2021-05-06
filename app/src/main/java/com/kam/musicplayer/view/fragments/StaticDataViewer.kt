package com.kam.musicplayer.view.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.FragmentDataViewerBinding
import com.kam.musicplayer.models.Album
import com.kam.musicplayer.models.Artist
import com.kam.musicplayer.models.entities.Song
import com.kam.musicplayer.services.MusicPlayerService
import com.kam.musicplayer.utils.Utils
import com.kam.musicplayer.utils.mContext
import com.kam.musicplayer.utils.musicApplication
import com.kam.musicplayer.view.adapters.SongsAdapter
import com.kam.musicplayer.view.dialogs.CreatePlaylistBuilder
import com.kam.musicplayer.view.dialogs.PickPlaylistBuilder
import com.kam.musicplayer.viewmodel.MainActivityViewModel
import com.kam.musicplayer.viewmodel.MusicViewModel
import com.kam.musicplayer.viewmodel.factories.MainActivityViewModelFactory
import com.kam.musicplayer.viewmodel.factories.MusicViewModelFactory

class StaticDataViewer : Fragment() {

    private val mViewModel: MainActivityViewModel by viewModels {
        MainActivityViewModelFactory(requireActivity().musicApplication)
    }

    private val mMusicModel: MusicViewModel by viewModels {
        MusicViewModelFactory(requireActivity().musicApplication)
    }

    private var _songsAdapter: SongsAdapter? = null
    private val mSongsAdapter: SongsAdapter
        get() {
            if (_songsAdapter == null)
                initializeSongsAdapter()
            return _songsAdapter!!
        }

    private var _binding: FragmentDataViewerBinding? = null
    private val mBinding: FragmentDataViewerBinding
        get() = _binding ?: throw Exception("Binding must not be accessed before creation or after destruction")

    private var isShowingAlbums = true // If this is false then we are assumed to be showing Artists

    private var albumLiveData: LiveData<Album>? = null
    private var artistLiveData: LiveData<Artist>? = null

    private val mAlbumObserver = Observer<Album> { album ->
        mAlbumDetails = Details(
                album.name,
                album.coverArt,
                album.songs
        )
        if (isShowingAlbums)
            showDetails()
    }
    private val mArtistObserver = Observer<Artist> { artist ->
        mArtistDetails = Details(
                artist.name,
                artist.coverArt,
                artist.songs
        )
        if (!isShowingAlbums)
            showDetails()
    }

    private var mAlbumDetails = Details("Album", null, listOf())
    private var mArtistDetails = Details("Artist", null, listOf())

    private val mSongs: List<Song>
        get() {
            return if (isShowingAlbums) mAlbumDetails.listData else mArtistDetails.listData
        }

    private fun initializeSongsAdapter() {
        _songsAdapter = SongsAdapter(
            mContext,
            R.drawable.ic_kebab
        )
        _songsAdapter!!.setOnActionListener(object: SongsAdapter.OnActionListener {
            override fun onClick(position: Int) {
                MusicPlayerService.run { it.setQueue(mSongs, mSongs[position]) }
            }

            override fun onOptionClicked(view: View, viewHolder: SongsAdapter.ViewHolder) {
                val popup = PopupMenu(mContext, view)

                popup.inflate(R.menu.songs_options_menu)

                popup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.play -> {
                            onClick(viewHolder.adapterPosition)
                        }
                        R.id.play_next -> {
                            val songs = mSongs
                            MusicPlayerService.run { service ->
                                service.playNext(songs[viewHolder.adapterPosition])
                            }
                        }
                        R.id.add_to_playlist -> {
                            val song = mSongs[viewHolder.adapterPosition]
                            PickPlaylistBuilder(mContext)
                                .setPlaylists(mMusicModel.allPlaylists.value ?: listOf())
                                .setOnSelected { playlist ->
                                    playlist.songs.add(song)
                                    mMusicModel.updatePlaylist(playlist)
                                }.setRequestCreate {
                                    CreatePlaylistBuilder(mContext)
                                        .setOnOk { name ->
                                            mMusicModel.createPlaylist(name, song)
                                        }.createDialog().show()
                                }.createDialog().show()
                        }
                    }
                    true
                }
            }

            override fun onOptionTouched(
                view: View,
                event: MotionEvent,
                viewHolder: SongsAdapter.ViewHolder
            ) {
                // Ignored
            }

        })
        _songsAdapter!!.attachToRecyclerView(mBinding.dataRv)
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

        with(mViewModel) {
            isShowingAlbum.observe(viewLifecycleOwner) {
                isShowingAlbums = it
                showDetails()
            }

            selectedAlbum.observe(viewLifecycleOwner) { liveData ->
                liveData?.let {
                    if (albumLiveData != null)
                        albumLiveData!!.removeObserver(mAlbumObserver)

                    albumLiveData = it
                    albumLiveData!!.observe(viewLifecycleOwner, mAlbumObserver)
                }
            }

            selectedArtist.observe(viewLifecycleOwner) { liveData ->
                liveData?.let {
                    if (artistLiveData != null)
                        artistLiveData!!.removeObserver(mArtistObserver)

                    artistLiveData = it
                    artistLiveData!!.observe(viewLifecycleOwner, mArtistObserver)
                }
            }
        }
    }

    private fun showDetails() {
        val details = if (isShowingAlbums) mAlbumDetails else mArtistDetails

        mViewModel.setTitle(MainActivityViewModel.Controller.Static, details.title)
        Utils.loadImage(mContext, mBinding.mainImageIv, details.coverUri, R.drawable.ic_placeholder)

        mSongsAdapter.submitList(mSongs)

        if (mSongs.isEmpty()) {
            mBinding.dataRv.visibility = View.GONE
            mBinding.noDataTv.visibility = View.VISIBLE
        } else {
            mBinding.dataRv.visibility = View.VISIBLE
            mBinding.noDataTv.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _songsAdapter = null
    }

    private data class Details(
        val title: String,
        val coverUri: Uri?,
        val listData: List<Song>
    )

}