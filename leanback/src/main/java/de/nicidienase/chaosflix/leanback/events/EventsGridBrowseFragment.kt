package de.nicidienase.chaosflix.leanback.events

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.v17.leanback.app.BackgroundManager
import android.support.v17.leanback.app.VerticalGridSupportFragment
import android.support.v17.leanback.widget.*
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.sync.Downloader
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.leanback.BrowseErrorFragment
import de.nicidienase.chaosflix.leanback.CardPresenter
import de.nicidienase.chaosflix.leanback.ItemViewClickedListener
import de.nicidienase.chaosflix.leanback.R
import java.net.URI
import java.net.URISyntaxException
import java.util.*

class EventsGridBrowseFragment : VerticalGridSupportFragment() {

	private val NUM_COLUMNS = 4;

	private lateinit var viewModel: BrowseViewModel
	private val handler = Handler()
	private lateinit var rowsAdapter: ArrayObjectAdapter
	private lateinit var defaultBackground: Drawable
	private var metrics: DisplayMetrics? = null
	private var backgroundTimer: Timer? = null
	private var backgroundURI: URI? = null
	private var backgroundManager: BackgroundManager? = null


	private val useTalksAsBackground = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		ContextCompat.getDrawable(requireContext(), R.drawable.default_background)?.apply {
			defaultBackground = this
		}

		viewModel = ViewModelProviders.of(this, ViewModelFactory(requireContext())).get(BrowseViewModel::class.java)


		val conference: Conference? = arguments?.getParcelable(EventsRowsBrowseFragment.CONFERENCE)
		if (conference == null) {
			throw IllegalStateException("No conference passed")
		}

		loadImage(conference.logoUrl, this::setBadgeDrawable)
		title = conference.title
		val presenter = VerticalGridPresenter(FocusHighlight.ZOOM_FACTOR_MEDIUM)
		presenter.numberOfColumns = NUM_COLUMNS
		gridPresenter = presenter
		val cardPresenter = CardPresenter(R.style.EventGridCardStyle)
		rowsAdapter = ArrayObjectAdapter(cardPresenter)

//		prepareBackgroundManager()
		onItemViewClickedListener = ItemViewClickedListener(this)
//		onItemViewSelectedListener = ItemViewSelectedListener()

		var errorFragment: BrowseErrorFragment? = null
		viewModel.updateEventsForConference(conference).observe(this, Observer { event ->
			when (event?.state) {
				Downloader.DownloaderState.RUNNING -> {
					fragmentManager?.let {
						errorFragment = BrowseErrorFragment.showErrorFragment(it, FRAGMENT)
					}
				}
				Downloader.DownloaderState.DONE -> {
					if (event.error != null) {
						val errorMessage = event.error ?: "Error refreshing events"
						errorFragment?.setErrorContent(errorMessage)
					} else {
						errorFragment?.dismiss()
					}
				}
			}
		})
		viewModel.getEventsforConference(conference).observe(this, Observer { events ->
			events?.let {
				rowsAdapter.setItems(events, null)
				errorFragment?.dismiss()
			}
		})
		adapter = rowsAdapter
	}

	override fun onDestroy() {
		super.onDestroy()
		if (null != backgroundTimer) {
			Log.d(TAG, "onDestroy: " + backgroundTimer?.toString())
			backgroundTimer?.cancel()
		}
	}

	private fun prepareBackgroundManager() {
		backgroundManager = BackgroundManager.getInstance(activity)
		backgroundManager?.attach(activity?.window)
		metrics = DisplayMetrics()
		activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
	}

	fun loadImage(url: String, consumer: (Drawable)-> Unit) {
		val options = RequestOptions()
		options.centerCrop()

		Glide.with(this)
				.load(url)
				.apply(options)
				.into(object : SimpleTarget<Drawable>() {
					override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
						consumer.invoke(resource)
					}

				})
	}

	protected fun updateBackground(uri: String) {
		loadImage(uri) {
			backgroundManager?.drawable = it
		}
		backgroundTimer?.cancel()
	}

	private fun startBackgroundTimer() {
		if (null != backgroundTimer) {
			backgroundTimer!!.cancel()
		}
		backgroundTimer = Timer()
		backgroundTimer?.schedule(UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY.toLong())
	}

	private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
		override fun onItemSelected(itemViewHolder: Presenter.ViewHolder, item: Any,
		                            rowViewHolder: RowPresenter.ViewHolder, row: Row) {
			if (item is Event) {
				try {
					backgroundURI = URI(item.posterUrl)
				} catch (e: URISyntaxException) {
					e.printStackTrace()
				}

				// TODO make configurable (enable/disable)
				if (useTalksAsBackground) {
					startBackgroundTimer();
				}
			}

		}
	}

	private inner class UpdateBackgroundTask : TimerTask() {

		override fun run() {
			handler.post {
				if (backgroundURI != null) {
					updateBackground(backgroundURI!!.toString())
				}
			}

		}
	}

	companion object {
		private val TAG = EventsGridBrowseFragment::class.java.simpleName

		private val BACKGROUND_UPDATE_DELAY = 300
		private val FRAGMENT = R.id.browse_fragment
		val CONFERENCE = "conference"

		fun create(conference: Conference): EventsGridBrowseFragment {
			return EventsGridBrowseFragment().apply {
				arguments = Bundle().apply { putParcelable(CONFERENCE, conference) }
			}
		}
	}
}
