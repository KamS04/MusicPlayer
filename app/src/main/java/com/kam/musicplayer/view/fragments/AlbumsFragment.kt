package com.kam.musicplayer.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.kam.musicplayer.databinding.FragmentGenericRecyclerBinding
import com.kam.musicplayer.models.ALBUM_DIFF_CALLBACK
import com.kam.musicplayer.models.Album
import com.kam.musicplayer.utils.mContext
import com.kam.musicplayer.utils.musicApplication
import com.kam.musicplayer.view.adapters.GenericItemsAdapter
import com.kam.musicplayer.viewmodel.MainActivityViewModel
import com.kam.musicplayer.viewmodel.MusicViewModel
import com.kam.musicplayer.viewmodel.factories.MainActivityViewModelFactory
import com.kam.musicplayer.viewmodel.factories.MusicViewModelFactory

/**
 * Fragment for viewing Albums
 */
class AlbumsFragment : Fragment() {

    private val mViewModel: MusicViewModel by viewModels {
        MusicViewModelFactory(requireActivity().musicApplication)
    }

    private val mMainViewModel: MainActivityViewModel by viewModels {
        MainActivityViewModelFactory(requireActivity().musicApplication)
    }

    private lateinit var mAlbumsAdapter: GenericItemsAdapter<Album>

    private var _binding: FragmentGenericRecyclerBinding? = null
    private val mBinding: FragmentGenericRecyclerBinding
        get() = _binding ?: throw Exception("Binding must not be accessed before creation or after destruction")

    private var mAlbums: List<Album> = listOf()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenericRecyclerBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    /**
     * Sets up adapter to [GenericItemsAdapter]
     * And attaches it to the RecyclerView
     *
     * Sets up [GenericItemsAdapter.OnActionListener] as well
     * Starts observing [MusicViewModel]
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAlbumsAdapter = GenericItemsAdapter(
            mContext,
            ALBUM_DIFF_CALLBACK,
            2
        ) { album ->
            GenericItemsAdapter.Details(
                album.name,
                album.songsCount.toString(),
                album.coverArt
            )
        }

        mAlbumsAdapter.setActionListener(object: GenericItemsAdapter.OnActionListener {
            override fun onClick(position: Int) {
                val album = mAlbums[position]
                mMainViewModel.showAlbum(mViewModel.getAlbum(album.name))
            }
        })

        mAlbumsAdapter.attachToRecyclerView(mBinding.listRv)

        mViewModel.allAlbums.observe(viewLifecycleOwner) { albums ->
            mAlbums = albums
            if (mAlbums.isEmpty()) {
                mBinding.listRv.visibility = View.GONE
                mBinding.emptyArrayTv.visibility = View.VISIBLE
            } else {
                mBinding.listRv.visibility = View.VISIBLE
                mBinding.emptyArrayTv.visibility = View.GONE
            }
            mAlbumsAdapter.submitList(mAlbums)
        }
    }
}