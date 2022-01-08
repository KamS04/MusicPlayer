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
import com.kam.musicplayer.databinding.FragmentGenericRecyclerBinding
import com.kam.musicplayer.models.ARTIST_DIFF_CALLBACK
import com.kam.musicplayer.models.Artist
import com.kam.musicplayer.utils.mContext
import com.kam.musicplayer.utils.musicApplication
import com.kam.musicplayer.view.adapters.GenericItemsAdapter
import com.kam.musicplayer.view.dialogs.CreatePlaylistBuilder
import com.kam.musicplayer.view.dialogs.PickPlaylistBuilder
import com.kam.musicplayer.viewmodel.MainActivityViewModel
import com.kam.musicplayer.viewmodel.MusicViewModel
import com.kam.musicplayer.viewmodel.factories.MainActivityViewModelFactory
import com.kam.musicplayer.viewmodel.factories.MusicViewModelFactory
import java.lang.Exception

class ArtistsFragment : Fragment() {

    private val mViewModel: MusicViewModel by viewModels {
        MusicViewModelFactory(requireActivity().musicApplication)
    }

    private val mMainViewModel: MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory(requireActivity().musicApplication)
    }

    private var _binding: FragmentGenericRecyclerBinding? = null
    private val mBinding: FragmentGenericRecyclerBinding
        get() = _binding ?: throw Exception("Binding must not be accessed before creation or after destruction")

    private lateinit var mArtistsAdapter: GenericItemsAdapter<Artist>

    private var mArtists: List<Artist> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenericRecyclerBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mArtistsAdapter = GenericItemsAdapter(
            mContext,
            ARTIST_DIFF_CALLBACK,
            2
        ) { artist ->
            GenericItemsAdapter.Details(
                artist.name,
                artist.songsCount.toString(),
                artist.coverArt
            )
        }

        mArtistsAdapter.setActionListener(object: GenericItemsAdapter.OnActionListener {
            override fun onClick(position: Int) {
                val artist = mArtists[position]
                mMainViewModel.showArtist(mViewModel.getArtist(artist.name))
            }

            override fun onLongClick(viewHolder: GenericItemsAdapter.ViewHolder) {
                val popup = PopupMenu(mContext, viewHolder.itemView)
                popup.inflate(R.menu.song_group_options)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.add_group_to_playlist -> {
                            if (viewHolder.bindingAdapterPosition != RecyclerView.NO_POSITION) {
                                val artist = mArtists[viewHolder.bindingAdapterPosition]

                                PickPlaylistBuilder(mContext)
                                    .setPlaylists(mViewModel.allPlaylistsOnce)
                                    .setOnSelected { playlist ->
                                        mViewModel.addSongsToPlaylist(playlist, *artist.songs.toTypedArray())
                                    }.setRequestCreate {
                                        CreatePlaylistBuilder(mContext)
                                            .setOnOk { name ->
                                                mViewModel.createPlaylist(name, *artist.songs.toTypedArray())
                                            }.createDialog().show()
                                    }.createDialog().show()
                            }
                        }
                    }
                    true
                }

                popup.show()
            }
        })

        mArtistsAdapter.attachToRecyclerView(mBinding.listRv)

        mViewModel.allArtists.observe(viewLifecycleOwner) { artists ->
            mArtists = artists
            if (mArtists.isEmpty()) {
                mBinding.emptyArrayTv.visibility = View.VISIBLE
                mBinding.listRv.visibility = View.GONE
            } else {
                mBinding.emptyArrayTv.visibility = View.GONE
                mBinding.listRv.visibility = View.VISIBLE
            }

            mArtistsAdapter.submitList(mArtists)
        }
    }

}