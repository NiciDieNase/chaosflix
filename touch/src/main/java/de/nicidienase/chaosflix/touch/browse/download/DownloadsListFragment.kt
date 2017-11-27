package de.nicidienase.chaosflix.touch.browse.download

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.download.OfflineEvent
import de.nicidienase.chaosflix.common.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.entities.streaming.Stream
import de.nicidienase.chaosflix.databinding.FragmentDownloadsBinding
import de.nicidienase.chaosflix.touch.browse.BrowseFragment

class DownloadsListFragment : BrowseFragment() {

	private lateinit var listener: InteractionListener
	private lateinit var binding: FragmentDownloadsBinding

	private val handler = Handler()

	private val UPDATE_DELAY = 700L

	override fun onAttach(context: Context?) {
		super.onAttach(context)
		if (context is InteractionListener) {
			listener = context
		} else {
			throw RuntimeException(context.toString() + " must implement LivestreamListFragment.InteractionListener")
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		binding = FragmentDownloadsBinding.inflate(inflater, container, false)
		setupToolbar(binding.incToolbar?.toolbar!!, R.string.downloads)
		overlay = binding.incOverlay?.loadingOverlay
		viewModel.getOfflineEvents().observe(this, Observer { events: List<OfflineEvent>? ->
			events?.let {
				binding.list.layoutManager = LinearLayoutManager(context)
				binding.list.adapter =
					OfflineEventAdapter(events, viewModel)
				setLoadingOverlayVisibility(false)
			}
		})
		return binding.root
	}

	private var updateRunnable: Runnable? = null

	override fun onResume() {
		super.onResume()
		updateRunnable = object: Runnable {
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

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setLoadingOverlayVisibility(false)
	}

	interface InteractionListener {
		fun onStreamSelected(conference: LiveConference, stream: Stream)
	}
}