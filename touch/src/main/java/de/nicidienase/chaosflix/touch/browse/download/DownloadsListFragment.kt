package de.nicidienase.chaosflix.touch.browse.download

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.download.OfflineEvent
import de.nicidienase.chaosflix.databinding.FragmentDownloadsBinding
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.touch.browse.BrowseFragment

class DownloadsListFragment : BrowseFragment() {

	private lateinit var listener: InteractionListener
	private lateinit var binding: FragmentDownloadsBinding

	private val handler = Handler()

	private val UPDATE_DELAY = 700L
	private var columnCount = 1;

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		columnCount = arguments?.getInt(ARG_COLUMN_COUNT) ?: 1
	}

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
				if (columnCount <= 1) {
					binding.list.layoutManager = LinearLayoutManager(context)
				} else {
					binding.list.layoutManager = GridLayoutManager(context, columnCount)
				}
				binding.list.adapter =
					OfflineEventAdapter(events, viewModel, listener)
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

	companion object {
		private val ARG_COLUMN_COUNT = "column_count"

		fun getInstance(columnCount: Int = 1): DownloadsListFragment{
			val fragment = DownloadsListFragment()
			val args = Bundle()
			args.putInt(ARG_COLUMN_COUNT, columnCount)
			fragment.arguments = args
			return fragment
		}
	}

	interface InteractionListener: OnEventSelectedListener {
	}
}