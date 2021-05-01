package com.kam.musicplayer.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import com.kam.musicplayer.R
import com.kam.musicplayer.databinding.FragmentGenericRecyclerBinding
import com.kam.musicplayer.models.entities.Song
import com.kam.musicplayer.services.MusicPlayerService
import com.kam.musicplayer.utils.mContext
import com.kam.musicplayer.utils.musicApplication
import com.kam.musicplayer.view.adapters.SongsAdapter
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

    private var _binding: FragmentGenericRecyclerBinding? = null
    private val mBinding: FragmentGenericRecyclerBinding
        get() = _binding ?: throw Exception("Binding must not be accessed before creation or after destruction")

    private var mAllSongs: List<Song> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenericRecyclerBinding.inflate(inflater, container, false)
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

        mSongsAdapter = SongsAdapter(mContext)

        mSongsAdapter.setOnActionListener(object: SongsAdapter.OnActionListener{
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
                val position = viewHolder.adapterPosition

                popup.inflate(R.menu.songs_options_menu)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.play -> {
                            onClick(position)
                            true
                        }
                        R.id.play_next -> {
                            MusicPlayerService.run {
                                it.playNext(mAllSongs[position])
                            }
                            true
                        }
                        R.id.add_to_playlist -> {
                            val song = mAllSongs[position]
                            // TODO create Playlist dialog
                            true
                        }
                        else -> false
                    }
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

        mMusicViewModel.allSongs.observe(viewLifecycleOwner) { songs ->
            mAllSongs = songs
            if (mAllSongs.isEmpty()) {
                mBinding.listRv.visibility = View.GONE
                mBinding.emptyArrayTv.visibility = View.VISIBLE
            } else {
                mBinding.listRv.visibility = View.VISIBLE
                mBinding.emptyArrayTv.visibility = View.GONE
            }
            mSongsAdapter.submitList(mAllSongs)
        }
    }

}