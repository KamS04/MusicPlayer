package com.kam.musicplayer.view.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.SingleRecyclerViewBinding
import com.kam.musicplayer.models.Album
import com.kam.musicplayer.models.Artist
import com.kam.musicplayer.models.entities.Playlist
import com.kam.musicplayer.models.entities.Song
import com.kam.musicplayer.services.MusicPlayerService
import com.kam.musicplayer.utils.mContext
import com.kam.musicplayer.utils.musicApplication
import com.kam.musicplayer.view.adapters.SongsAdapter
import com.kam.musicplayer.view.adapters.concatable.InfoHeaderAdapter
import com.kam.musicplayer.view.adapters.concatable.SingleMessageAdapter
import com.kam.musicplayer.view.dialogs.CreatePlaylistBuilder
import com.kam.musicplayer.view.dialogs.PickPlaylistBuilder
import com.kam.musicplayer.viewmodel.MainActivityViewModel
import com.kam.musicplayer.viewmodel.MusicViewModel
import com.kam.musicplayer.viewmodel.factories.MainActivityViewModelFactory
import com.kam.musicplayer.viewmodel.factories.MusicViewModelFactory

class SuperStaticDataViewer : Fragment() {

    private val mViewModel: MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory(requireActivity().musicApplication)
    }

    private val mMusicModel: MusicViewModel by viewModels {
        MusicViewModelFactory(requireActivity().musicApplication)
    }

    private lateinit var infoHeaderAdapter: InfoHeaderAdapter
    private lateinit var singleMessageAdapter: SingleMessageAdapter

    private var _songsAdapter: SongsAdapter? = null
    private val mSongsAdapter: SongsAdapter
        get() {
            if (_songsAdapter == null)
                initializeSongsAdapter()
            return _songsAdapter!!
        }

    private var _binding: SingleRecyclerViewBinding? = null
    private val mBinding: SingleRecyclerViewBinding
        get() = _binding ?: throw Exception("Binding must not be accessed before creation or after destruction of fragment")


    private var mCurrentlyShowing: DataType = DataType.Artist

    private var albumLiveData: LiveData<Album>? = null
    private var artistLiveData: LiveData<Artist>? = null
    private var playlistLiveData: LiveData<Playlist>? = null

    private var mAlbumDetails = Details("Album", null, listOf())
    private var mArtistDetails = Details("Album", null, listOf())
    private var mPlaylistDetails = Details("Playlist", null, listOf())

    private var mCurrentPlaylist: Playlist? = null

    private val mAlbumObserver = Observer<Album> { album ->
        mAlbumDetails = Details(
                album.name,
                album.coverArt,
                album.songs
        )
        if (mCurrentlyShowing == DataType.Album)
            showDetails()
    }

    private val mArtistObserver = Observer<Artist> { artist ->
        mArtistDetails = Details(
                artist.name,
                artist.coverArt,
                artist.songs
        )
        if (mCurrentlyShowing == DataType.Artist)
            showDetails()
    }

    private val mPlaylistObserver = Observer<Playlist> { playlist ->
        mPlaylistDetails = Details(
            playlist.info.name,
            playlist.albumArt,
            playlist.songs
        )
        mCurrentPlaylist = playlist
        if (mCurrentlyShowing == DataType.Playlist)
            showDetails()
    }

    private val mSongs: List<Song>
        get() {
            return when (mCurrentlyShowing) {
                DataType.Artist -> mArtistDetails
                DataType.Album -> mAlbumDetails
                DataType.Playlist -> mPlaylistDetails
            }.listData
        }

    private fun initializeSongsAdapter() {
        _songsAdapter = SongsAdapter(R.drawable.ic_kebab)

        _songsAdapter!!.setOnActionListener(object : SongsAdapter.OnActionListener {
            override fun onClick(position: Int) {
                MusicPlayerService.run { it.setQueue(mSongs, mSongs[position]) }
            }

            override fun onOptionClicked(view: View, viewHolder: SongsAdapter.ViewHolder) {
                val popup = PopupMenu(mContext, view)

                popup.inflate(
                    if (mCurrentlyShowing == DataType.Playlist) R.menu.playlist_song_menu else R.menu.songs_options_menu
                )

                popup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.play -> {
                            onClick(viewHolder.bindingAdapterPosition)
                        }
                        R.id.play_next -> {
                            val songs = mSongs
                            MusicPlayerService.run { service ->
                                service.playNext(songs[viewHolder.bindingAdapterPosition])
                            }
                        }
                        R.id.add_to_playlist -> {
                            val song = mSongs[viewHolder.bindingAdapterPosition]
                            PickPlaylistBuilder(mContext)
                                    .setPlaylists(mMusicModel.allPlaylistsOnce)
                                    .setOnSelected { playlist ->
                                        mMusicModel.addSongsToPlaylist(playlist, song)
                                    }.setRequestCreate {
                                        CreatePlaylistBuilder(mContext)
                                                .setOnOk { name ->
                                                    mMusicModel.createPlaylist(name, song)
                                                }.createDialog().show()
                                    }.createDialog().show()
                        }
                        R.id.remove_song -> {
                            mCurrentPlaylist?.let { playlist ->
                                mMusicModel.removeSongFromPlaylist(playlist, viewHolder.bindingAdapterPosition)
                            }
                        }
                    }
                    true
                }

                popup.show()
            }

            override fun onOptionTouched(view: View, event: MotionEvent, viewHolder: SongsAdapter.ViewHolder) {
                // Ignored
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = SingleRecyclerViewBinding.inflate(inflater, container, false)
        infoHeaderAdapter = InfoHeaderAdapter(R.drawable.ic_placeholder)
        singleMessageAdapter = SingleMessageAdapter(getString(R.string.no_songs))
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel.currentlyDisplaying.observe(viewLifecycleOwner) {
            mCurrentlyShowing = it
            showDetails()
        }

        mViewModel.selectedAlbum.observe(viewLifecycleOwner) { liveData ->
            liveData?.let {
                if (albumLiveData != null)
                    albumLiveData!!.removeObserver(mAlbumObserver)

                albumLiveData = it
                albumLiveData!!.observe(viewLifecycleOwner, mAlbumObserver)
            }
        }

        mViewModel.selectedArtist.observe(viewLifecycleOwner) { liveData ->
            liveData?.let {
                if (artistLiveData != null)
                    artistLiveData!!.removeObserver(mArtistObserver)

                artistLiveData = it
                artistLiveData!!.observe(viewLifecycleOwner, mArtistObserver)
            }
        }

        mViewModel.selectedPlaylist.observe(viewLifecycleOwner) { liveData ->
            liveData?.let {
                if (playlistLiveData != null)
                    playlistLiveData!!.removeObserver(mPlaylistObserver)

                playlistLiveData = it
                playlistLiveData!!.observe(viewLifecycleOwner, mPlaylistObserver)
            }
        }

        val finalAdapter = ConcatAdapter(infoHeaderAdapter, mSongsAdapter, singleMessageAdapter)
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        mBinding.recyclerview.adapter = finalAdapter
    }

    private fun showDetails() {
        val details = when (mCurrentlyShowing) {
            DataType.Playlist -> mPlaylistDetails
            DataType.Artist -> mArtistDetails
            DataType.Album -> mAlbumDetails
        }

        mViewModel.setTitle(MainActivityViewModel.Controller.Static, details.title)

        infoHeaderAdapter.resetData(details.coverUri)
        if (mCurrentlyShowing == DataType.Playlist) {
            infoHeaderAdapter.showActionBtn(R.drawable.ic_reorder) {
                mCurrentPlaylist?.let { mViewModel.startReordering(it) }
            }
        }

        mSongsAdapter.submitList(details.listData)
        singleMessageAdapter.setShowing(details.listData.isEmpty())
    }

    private data class Details(
            val title: String,
            val coverUri: Uri?,
            val listData: List<Song>
    )

    enum class DataType {
        Artist,
        Album,
        Playlist
    }

}