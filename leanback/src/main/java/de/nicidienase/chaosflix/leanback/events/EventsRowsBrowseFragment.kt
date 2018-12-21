package de.nicidienase.chaosflix.leanback.events

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.v17.leanback.app.BackgroundManager
import android.support.v17.leanback.app.BrowseSupportFragment
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
import kotlin.collections.HashMap

class EventsRowsBrowseFragment : BrowseSupportFragment() {

	private lateinit var viewModel: BrowseViewModel
	private val handler = Handler()
	private var rowsAdapter: ArrayObjectAdapter = ArrayObjectAdapter(ListRowPresenter())
	private lateinit var defaultBackground: Drawable
	private var metrics: DisplayMetrics? = null
	private var backgroundTimer: Timer? = null
	private var backgroundURI: URI? = null
	private var backgroundManager: BackgroundManager? = null

	private val eventRows: MutableMap<String, ListRow> = HashMap()

	private val useTalksAsBackground = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		ContextCompat.getDrawable(requireContext(), R.drawable.default_background)?.apply {
			defaultBackground = this
		}

		viewModel = ViewModelProviders
				.of(this, ViewModelFactory(requireContext()))
				.get(BrowseViewModel::class.java)


		val conference: Conference? = arguments?.getParcelable(CONFERENCE)
		if (conference == null) {
			throw IllegalStateException("No conference passed")
		}
		setupUIElements(conference)

		val cardPresenter = CardPresenter(R.style.EventCardStyle)

		prepareBackgroundManager()
		onItemViewClickedListener = ItemViewClickedListener(this)
//		onItemViewSelectedListener = ItemViewSelectedListener()

		var errorFragment: BrowseErrorFragment? = null
		viewModel.updateEventsForConference(conference).observe(this, Observer {
			when (it?.state) {
				Downloader.DownloaderState.RUNNING -> {
					fragmentManager?.let {
						errorFragment = BrowseErrorFragment.showErrorFragment(it, FRAGMENT)
					}
				}
				Downloader.DownloaderState.DONE -> {
					if (it.error != null) {
						val errorMessage = it.error ?: "Error refreshing events"
						errorFragment?.setErrorContent(errorMessage)
					} else {
						errorFragment?.dismiss()
					}
				}
			}
//			val events = it?.data
//			if (events != null) {
//				Log.d(TAG, "Got ${events.size} events")
//				val eventsByTags = getEventsByTags(events, conference.acronym)
//				Log.d(TAG, "Got ${eventsByTags.keys.size} Tags")
//				for (item in eventsByTags) {
//					updateRowForTag(cardPresenter, item.key, item.value)
//				}
//				errorFragment?.dismiss()
//			}
		})
		viewModel.getEventsforConference(conference).observe(
				this,
				Observer {
					if (it != null && it.size > 0) {
						val eventsByTags = getEventsByTags(it, conference.acronym)
						for (item in eventsByTags) {
							updateRowForTag(cardPresenter, item.key, item.value)
						}
						errorFragment?.dismiss()
					}
				}
		)
		adapter = rowsAdapter
	}

	override fun onDestroy() {
		super.onDestroy()
		if (null != backgroundTimer) {
			Log.d(TAG, "onDestroy: " + backgroundTimer?.toString())
			backgroundTimer?.cancel()
		}
	}

	private fun getEventsByTags(events: List<Event>, conferenceAcronym: String): Map<String, List<Event>> {
		val eventsByTags = HashMap<String, MutableList<Event>>()
		val other = LinkedList<Event>()
		for (event in events) {
			val tags: List<String> = event
					.tags
					?.filter { !android.text.TextUtils.isDigitsOnly(it) && !it.equals(conferenceAcronym) }
					?: emptyList<String>()
			if (tags.size == 0) {
				other.add(event)
			} else {
				tags.forEach {
					if (!eventsByTags.keys.contains(it)) {
						eventsByTags.put(it, ArrayList())
					}
					eventsByTags[it]?.add(event)
				}
			}
		}
		if (other.size > 0) {
			eventsByTags.put("other", other)
		}
		return eventsByTags
	}

	private fun updateRowForTag(cardPresenter: CardPresenter, tag: String, items: List<Event>): Row {
		var row = eventRows[tag]
		val header: HeaderItem
		val listRowAdapter: ArrayObjectAdapter
		if (row == null) {
			header = HeaderItem(tag)
			listRowAdapter = ArrayObjectAdapter(cardPresenter)
			row = ListRow(header, listRowAdapter)
			eventRows.put(tag, row)
			rowsAdapter.add(row)
		} else {
			listRowAdapter = row.adapter as ArrayObjectAdapter
		}
		Collections.sort(items)
		listRowAdapter.setItems(items, object : DiffCallback<Event>() {
			override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
				return oldItem.guid == newItem.guid
			}

			override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
				return oldItem.title == newItem.title
			}

		})
		return row
	}

	private fun prepareBackgroundManager() {
		backgroundManager = BackgroundManager.getInstance(activity)
		backgroundManager?.attach(activity?.window)
		metrics = DisplayMetrics()
		activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
	}

	private fun setupUIElements(conference: Conference) {

		loadImage(conference.logoUrl, this::setBadgeDrawable)
		title = conference.title // Badge, when set, takes precedent
		// over title
		headersState = BrowseSupportFragment.HEADERS_ENABLED
		isHeadersTransitionOnBackEnabled = true

		// set fastLane (or headers) background color
//		brandColor = resources.getColor(R.color.fastlane_background)
		// set search icon color
//		searchAffordanceColor = resources.getColor(R.color.search_opaque)
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
		private val TAG = EventsRowsBrowseFragment::class.java.simpleName

		private val BACKGROUND_UPDATE_DELAY = 300
		private val FRAGMENT = R.id.browse_fragment

		val CONFERENCE = "conference"

		fun create(conference: Conference): EventsRowsBrowseFragment {
			return EventsRowsBrowseFragment().apply {
				arguments = Bundle().apply { putParcelable(CONFERENCE, conference) }
			}
		}
	}
}
