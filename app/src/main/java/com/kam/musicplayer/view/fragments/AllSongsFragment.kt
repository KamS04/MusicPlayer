package com.kam.musicplayer.view.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.FragmentGenericRecyclerBinding
import com.kam.musicplayer.databinding.FragmentSwipeRecyclerBinding
import com.kam.musicplayer.models.entities.Playlist
import com.kam.musicplayer.models.entities.Song
import com.kam.musicplayer.services.MusicPlayerService
import com.kam.musicplayer.utils.mContext
import com.kam.musicplayer.utils.musicApplication
import com.kam.musicplayer.view.adapters.SongsAdapter
import com.kam.musicplayer.view.dialogs.CreatePlaylistBuilder
import com.kam.musicplayer.view.dialogs.PickPlaylistBuilder
import com.kam.musicplayer.viewmodel.MusicViewModel
import com.kam.musicplayer.viewmodel.factories.MusicViewModelFactory

/**
 * Shows every song that the data source returns
 */
class AllSongsFragment : Fragment() {

    private val mMusicViewModel: MusicViewModel by viewModels {
        MusicViewModelFactory(requireActivity().musicApplication)
    }

    private lateinit var mSongsAdapter: SongsAdapter

    private var _binding: FragmentSwipeRecyclerBinding? = null
    private val mBinding: FragmentSwipeRecyclerBinding
        get() = _binding ?: throw Exception("Binding must not be accessed before creation or after destruction")

    private var mAllSongs: List<Song> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSwipeRecyclerBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    /**
     * Sets up adapter to [SongsAdapter]
     * And attaches it to the RecyclerView
     *
     * Sets up [SongsAdapter.OnActionListener] as well
     * Starts observing the [MusicViewModel]
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSongsAdapter = SongsAdapter()

        mSongsAdapter.setOnActionListener(object : SongsAdapter.OnActionListener {
            override fun onClick(position: Int) {
                MusicPlayerService.run {
                    it.setQueue(
                        mAllSongs,
                        mAllSongs[position]
                    )
                }
            }

            override fun onOptionClicked(view: View, viewHolder: SongsAdapter.ViewHolder) {
                val popup = PopupMenu(mContext, view)
                val position = viewHolder.bindingAdapterPosition

                popup.inflate(R.menu.songs_options_menu)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.play -> {
                            onClick(position)
                        }
                        R.id.play_next -> {
                            MusicPlayerService.run {
                                it.playNext(mAllSongs[position])
                            }
                        }
                        R.id.add_to_playlist -> {
                            val song = mAllSongs[position]

                            PickPlaylistBuilder(mContext)
                                .setPlaylists(mMusicViewModel.allPlaylistsOnce)
                                .setOnSelected { playlist ->
                                    mMusicViewModel.addSongsToPlaylist(playlist, song)
                                }.setRequestCreate {
                                    CreatePlaylistBuilder(mContext)
                                        .setOnOk { name ->
                                            mMusicViewModel.createPlaylist(name, song)
                                        }.createDialog().show()
                                }.createDialog().show()
                        }
                    }
                    true
                }

                popup.show()
            }

            override fun onOptionTouched(
                view: View,
                event: MotionEvent,
                viewHolder: SongsAdapter.ViewHolder
            ) {
                // Ignored
            }

        })

        mSongsAdapter.attachToRecyclerView(mBinding.listRv)

        mBinding.fsrSwipeSrl.setOnRefreshListener {
            mMusicViewModel.refreshSongs()
        }

        mMusicViewModel.allSongs.observe(viewLifecycleOwner) { songs ->
            //Log.i("ALLS", "Song 1 ${songs[0].name}} Song ${songs.size} ${songs.last().name}")
            if (mBinding.fsrSwipeSrl.isRefreshing)
                mBinding.fsrSwipeSrl.isRefreshing = false

            mAllSongs = songs
            if (mAllSongs.isEmpty()) {
                mBinding.fsrSwipeSrl.visibility = View.GONE
                mBinding.emptyArrayTv.visibility = View.VISIBLE
            } else {
                mBinding.fsrSwipeSrl.visibility = View.VISIBLE
                mBinding.emptyArrayTv.visibility = View.GONE
            }
            mSongsAdapter.submitList(songs)
            //Log.i("ALLS", "Submitted List")
        }
    }

}