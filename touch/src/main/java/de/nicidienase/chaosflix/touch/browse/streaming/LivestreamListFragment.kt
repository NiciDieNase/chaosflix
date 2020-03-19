package de.nicidienase.chaosflix.touch.browse.streaming

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamUrl
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.BrowseFragment
import de.nicidienase.chaosflix.touch.databinding.FragmentLivestreamsBinding
import de.nicidienase.chaosflix.touch.playback.PlaybackItem

class LivestreamListFragment : BrowseFragment() {

    private lateinit var binding: FragmentLivestreamsBinding
    lateinit var adapter: LivestreamAdapter
    lateinit var snackbar: Snackbar

    private var columnCount = 1

    private var castService = object {
        val connected = false
        fun castStream(streamingItem: StreamingItem, streamUrl: StreamUrl, s: String) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            columnCount = arguments!!.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLivestreamsBinding.inflate(inflater, container, false)
        setupToolbar(binding.incToolbar.toolbar, R.string.livestreams)
        if (columnCount <= 1) {
            binding.list.layoutManager = LinearLayoutManager(context)
        } else {
            binding.list.layoutManager =
                GridLayoutManager(context, columnCount)
        }
        adapter = LivestreamAdapter {
            // TODO navigat to player/livestream-details
            selectLivestream(it)
        }
        binding.list.adapter = adapter
        binding.swipeRefreshLayout.setOnRefreshListener {
            updateList()
        }
        snackbar = Snackbar.make(binding.root, R.string.no_livestreams, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.reload, View.OnClickListener { this.updateList() })
        return binding.root
    }

    private fun selectLivestream(streamingItem: StreamingItem) {
        val entries = HashMap<String, StreamUrl>()

        if (castService.connected) {
            val hdStreams = streamingItem.room.streams // .filter { it.slug.startsWith("hd-") }
            Log.i(TAG, "found ${hdStreams.size} suitable streams, starting selection")
            if (hdStreams.size > 1) {
                val dialog = AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.select_stream))
                        .setItems(hdStreams.map { it.display }.toTypedArray()) { _, i ->
                            val stream = hdStreams[i]
                            val keys = stream.urls.keys.toTypedArray()
                            val dialog = AlertDialog.Builder(requireContext())
                                    .setTitle(this.getString(R.string.select_stream))
                                    .setItems(keys) { _: DialogInterface?, which: Int ->
                                        val streamUrl = stream.urls[keys[which]]
                                        if (streamUrl != null) {
                                            castService.castStream(streamingItem, streamUrl, keys[which])
                                        } else {
                                            Snackbar.make(binding.root, "could not play stream", Snackbar.LENGTH_SHORT).show()
                                        }
                                    }
                                    .create()
                            dialog.show()
                        }
                        .create()
                dialog.show()
            } else {
                Log.i(TAG, "Found no HD-Stream")
            }
        } else {
            val dashStreams = streamingItem.room.streams.filter { it.slug == "dash-native" }
            if (dashStreams.size > 0 &&
                    viewModel.getAutoselectStream()) {
                val streamUrl = dashStreams.first().urls["dash"]
                if (streamUrl != null) {
                    playLivestream(PlaybackItem.fromStream(streamingItem.conference.conference,
                            streamingItem.room.display,
                            streamUrl))
                }
            } else {
                streamingItem.room.streams.flatMap { stream ->
                    stream.urls.map { entry ->
                        entries.put(stream.slug + " " + entry.key, entry.value)
                    }
                }

                val builder = AlertDialog.Builder(requireContext())
                val strings = entries.keys.sorted().toTypedArray()
                builder.setTitle(getString(R.string.select_stream))
                        .setItems(strings) { _, i ->
                            Toast.makeText(requireContext(), strings[i], Toast.LENGTH_LONG).show()
                            val streamUrl = entries[strings[i]]
                            if (streamUrl != null) {
                                playLivestream(PlaybackItem.fromStream(
                                        streamingItem.conference.conference,
                                        streamingItem.room.display,
                                        streamUrl))
                            }
                        }
                builder.create().show()
            }
        }
    }

    fun playLivestream(item: PlaybackItem) {
        findNavController().navigate(LivestreamListFragmentDirections.actionLivestreamListFragmentToExoPlayerFragment(item))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateList()
    }

    private val TAG = LivestreamListFragment::class.simpleName

    private fun updateList() {
// 		binding.swipeRefreshLayout.postDelayed( Runnable {
// 			binding.swipeRefreshLayout.isRefreshing = true
// 		}, 500)
        binding.swipeRefreshLayout.isRefreshing = true
        Log.d(TAG, "Refresh starting")
        viewModel.getLivestreams().observe(viewLifecycleOwner, Observer {
            it?.let { adapter.setContent(it) }
            binding.swipeRefreshLayout.isRefreshing = false
            if (it?.size == 0 && !snackbar.isShown) {
                snackbar.show()
            } else {
                snackbar.dismiss()
            }
            Log.d(TAG, "Refresh done")
        })
    }

    companion object {
        private val ARG_COLUMN_COUNT = "column-count"

        fun newInstance(columnCount: Int): LivestreamListFragment {
            val fragment = LivestreamListFragment()
            val args = Bundle()
            args.putInt(ARG_COLUMN_COUNT, columnCount)
            fragment.arguments = args
            return fragment
        }
    }
}
