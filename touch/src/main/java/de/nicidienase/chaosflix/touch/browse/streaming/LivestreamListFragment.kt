package de.nicidienase.chaosflix.touch.browse.streaming

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.BrowseFragment
import de.nicidienase.chaosflix.touch.databinding.FragmentLivestreamsBinding

class LivestreamListFragment : BrowseFragment() {

    private lateinit var listener: InteractionListener
    private lateinit var binding: FragmentLivestreamsBinding
    lateinit var adapter: LivestreamAdapter
    lateinit var snackbar: Snackbar

    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            columnCount = arguments!!.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is InteractionListener) {
            listener = context
            adapter = LivestreamAdapter(listener)
        } else {
            throw RuntimeException("$context must implement LivestreamListFragment.InteractionListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLivestreamsBinding.inflate(inflater, container, false)
        setupToolbar(binding.incToolbar.toolbar, R.string.livestreams)
        if (columnCount <= 1) {
            binding.list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        } else {
            binding.list.layoutManager =
                androidx.recyclerview.widget.GridLayoutManager(context, columnCount)
        }
        binding.list.adapter = adapter
        binding.swipeRefreshLayout.setOnRefreshListener {
            updateList()
        }
        viewModel.getLivestreams().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                adapter.setContent(it)
                if (it.isEmpty() && !snackbar.isShown) {
                    snackbar.show()
                } else {
                    snackbar.dismiss()
                }
            }
            binding.swipeRefreshLayout.isRefreshing = false
        })
        viewModel.updateLiveStreams()

        snackbar = Snackbar.make(binding.root, R.string.no_livestreams, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.reload) { this.updateList() }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateList()
    }

    private fun updateList() {
        binding.swipeRefreshLayout.isRefreshing = true
        Log.d(TAG, "Refresh starting")
        viewModel.updateLiveStreams()
    }

    interface InteractionListener {
        fun onStreamSelected(streamingItem: StreamingItem)
    }

    companion object {
        private val ARG_COLUMN_COUNT = "column-count"

        private val TAG = LivestreamListFragment::class.simpleName

        fun newInstance(columnCount: Int): LivestreamListFragment {
            val fragment = LivestreamListFragment()
            val args = Bundle()
            args.putInt(ARG_COLUMN_COUNT, columnCount)
            fragment.arguments = args
            return fragment
        }
    }
}
