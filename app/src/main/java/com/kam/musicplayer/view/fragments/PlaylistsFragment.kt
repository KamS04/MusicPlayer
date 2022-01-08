package com.kam.musicplayer.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.FragmentPlaylistBinding
import com.kam.musicplayer.models.entities.PLAYLIST_DIFF_UTIL
import com.kam.musicplayer.models.entities.Playlist
import com.kam.musicplayer.utils.mContext
import com.kam.musicplayer.utils.musicApplication
import com.kam.musicplayer.view.adapters.GenericItemsAdapter
import com.kam.musicplayer.view.dialogs.CreatePlaylistBuilder
import com.kam.musicplayer.view.dialogs.RenamePlaylistBuilder
import com.kam.musicplayer.viewmodel.MainActivityViewModel
import com.kam.musicplayer.viewmodel.MusicViewModel
import com.kam.musicplayer.viewmodel.factories.MainActivityViewModelFactory
import com.kam.musicplayer.viewmodel.factories.MusicViewModelFactory

class PlaylistsFragment : Fragment() {

    private val mViewModel: MusicViewModel by viewModels {
        MusicViewModelFactory(requireActivity().musicApplication)
    }

    private val mMainViewModel: MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory(requireActivity().musicApplication)
    }

    private var _binding: FragmentPlaylistBinding? = null
    private val mBinding: FragmentPlaylistBinding
        get() = _binding ?: throw Exception("Binding must not be accessed before creation or after destruction")

    private lateinit var mPlaylistsAdapter: GenericItemsAdapter<Playlist>
    private var mPlaylists: List<Playlist> = listOf()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPlaylistsAdapter = GenericItemsAdapter(mContext, PLAYLIST_DIFF_UTIL, 2) { playlist ->
            GenericItemsAdapter.Details(
                playlist.info.name,
                playlist.songsCount.toString(),
                playlist.albumArt,
            )
        }

        mPlaylistsAdapter.setActionListener(object: GenericItemsAdapter.OnActionListener {
            override fun onClick(position: Int) {
                mMainViewModel.showPlaylist(mViewModel.getPlaylist(mPlaylists[position].info.playlistId))
            }

            override fun onLongClick(viewHolder: GenericItemsAdapter.ViewHolder) {
                val popup = PopupMenu(mContext, viewHolder.itemView).apply {
                    inflate(R.menu.playlist_options_menu)

                    setOnMenuItemClickListener {
                        when(it.itemId) {
                            R.id.rename_playlist -> {
                                val playlist = mPlaylists[viewHolder.bindingAdapterPosition]

                                RenamePlaylistBuilder(mContext)
                                    .setOldName(playlist.info.name)
                                    .addOnOk { newName ->
                                        mViewModel.renamePlaylist(playlist, newName)
                                    }.createDialog().show()
                            }
                            R.id.delete_playlist -> {
                                val position = viewHolder.bindingAdapterPosition

                                if (position != RecyclerView.NO_POSITION)
                                    mViewModel.deletePlaylist(mPlaylists[position])
                            }
                        }
                        true
                    }
                }

                popup.show()
            }
        })

        mPlaylistsAdapter.attachToRecyclerView(mBinding.playlistsRv)

        mViewModel.allPlaylists.observe(viewLifecycleOwner) { playlists ->
            mPlaylists = playlists
            if (mPlaylists.isEmpty()) {
                mBinding.emptyArrayTv.visibility = View.VISIBLE
                mBinding.playlistsRv.visibility = View.GONE
            } else {
                mBinding.emptyArrayTv.visibility = View.GONE
                mBinding.playlistsRv.visibility = View.VISIBLE
            }

            mPlaylistsAdapter.submitList(mPlaylists)
        }

        mBinding.addPlaylistFab.setOnClickListener { _ ->
            CreatePlaylistBuilder(mContext)
                .setOnOk { name ->
                    mViewModel.createPlaylist(name)
                }.createDialog().show()
        }
    }

}