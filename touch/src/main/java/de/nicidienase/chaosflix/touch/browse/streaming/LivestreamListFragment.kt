package de.nicidienase.chaosflix.touch.browse.streaming

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.google.android.material.snackbar.Snackbar
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamUrl
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.databinding.FragmentLivestreamsBinding
import de.nicidienase.chaosflix.touch.playback.PlaybackItem
import de.nicidienase.chaosflix.touch.browse.cast.CastService
import org.koin.android.ext.android.inject
import java.lang.Exception
import org.koin.android.viewmodel.ext.android.viewModel

class LivestreamListFragment : Fragment() {

    private val viewModel: BrowseViewModel by viewModel()

    private val castService: CastService by inject()

    private lateinit var binding: FragmentLivestreamsBinding
    lateinit var snackbar: Snackbar

    private var columnCount = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLivestreamsBinding.inflate(inflater, container, false)
        if (columnCount <= 1) {
            binding.list.layoutManager = LinearLayoutManager(context)
        } else {
            binding.list.layoutManager =
                GridLayoutManager(context, columnCount)
        }
        val livestreamAdapter = LivestreamAdapter {
            selectLivestream(it)
        }
        val eventInfoAdapter = EventInfoAdapter {
            try {
                val uri = Uri.parse(it.description)
                CustomTabsIntent.Builder()
                        .setToolbarColor(resources.getColor(R.color.primary))
                        .setStartAnimations(requireContext(), android.R.anim.fade_in, android.R.anim.fade_out)
                        .setExitAnimations(requireContext(), android.R.anim.fade_in, android.R.anim.fade_out)
                        .build()
                        .launchUrl(requireContext(), uri)
            } catch (ex: Exception) {
                Log.e(TAG, ex.message, ex)
            }
        }

//        binding.list.adapter = ConcatAdapter(livestreamAdapter, eventInfoAdapter)
        binding.list.adapter = livestreamAdapter
        binding.swipeRefreshLayout.setOnRefreshListener {
            updateList()
        }
        viewModel.getLivestreams().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                livestreamAdapter.setContent(it)
                if (it.isEmpty() && !snackbar.isShown) {
                    snackbar.show()
                } else {
                    snackbar.dismiss()
                }
            }
            binding.swipeRefreshLayout.isRefreshing = false
        })
        viewModel.getEventInfo().observe(viewLifecycleOwner, Observer {
            eventInfoAdapter.submitList(it)
        })
        viewModel.updateLiveStreams()
        viewModel.updateEventInfo()

        snackbar = Snackbar.make(binding.root, R.string.no_livestreams, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.reload) { this.updateList() }
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
        findNavController().navigate(LivestreamListFragmentDirections.actionLivestreamListFragmentToPlayerActivity(item))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateList()
    }

    private fun updateList() {
        binding.swipeRefreshLayout.isRefreshing = true
        Log.d(TAG, "Refresh starting")
        viewModel.updateLiveStreams()
        viewModel.updateEventInfo()
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
