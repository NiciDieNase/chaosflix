package de.nicidienase.chaosflix.touch.browse.download

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.touch.browse.mediathek.MediathekFragmentDirections
import de.nicidienase.chaosflix.touch.databinding.FragmentDownloadsBinding
import org.koin.android.viewmodel.ext.android.viewModel

class DownloadsListFragment : Fragment() {

    private val viewModel: BrowseViewModel by viewModel()

    private val handler = Handler()

    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        columnCount = arguments?.getInt(ARG_COLUMN_COUNT) ?: columnCount
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        with(FragmentDownloadsBinding.inflate(inflater, container, false)) {
            val offlineEventAdapter = OfflineEventAdapter(viewModel.offlineItemManager, viewModel::deleteOfflineItem) { guid ->
                findNavController().navigate(MediathekFragmentDirections.actionMyChaosflixFragmentToEventDetailsFragment(eventGuid = guid))
            }
            list.adapter = offlineEventAdapter
            if (columnCount <= 1) {
                list.layoutManager = LinearLayoutManager(context)
            } else {
                list.layoutManager =
                    StaggeredGridLayoutManager(columnCount - 1, StaggeredGridLayoutManager.VERTICAL)
            }
            viewModel.getOfflineDisplayEvents().observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    this.incOverlay.loadingOverlay.visibility = View.INVISIBLE
                    offlineEventAdapter.items = it
                }
            })
            return this.root
        }
    }

    private var updateRunnable: Runnable? = null

    override fun onResume() {
        super.onResume()
        updateRunnable = object : Runnable {
            override fun run() {
                viewModel.updateDownloadStatus()
                handler.postDelayed(this, UPDATE_DELAY)
            }
        }
        handler.post(updateRunnable)
    }

    override fun onPause() {
        handler.removeCallbacks(updateRunnable)
        super.onPause()
    }

    companion object {
        private val TAG = DownloadsListFragment::class.java.simpleName
        private const val ARG_COLUMN_COUNT = "column_count"
        private const val UPDATE_DELAY = 700L

        fun getInstance(columnCount: Int = 1): DownloadsListFragment {
            val fragment = DownloadsListFragment()
            val args = Bundle()
            args.putInt(ARG_COLUMN_COUNT, columnCount)
            fragment.arguments = args
            return fragment
        }
    }
}
