package de.nicidienase.chaosflix.leanback.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.v17.leanback.app.BackgroundManager
import android.support.v17.leanback.app.BrowseSupportFragment
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.DiffCallback
import android.support.v17.leanback.widget.HeaderItem
import android.support.v17.leanback.widget.ListRow
import android.support.v17.leanback.widget.ListRowPresenter
import android.support.v17.leanback.widget.OnItemViewSelectedListener
import android.support.v17.leanback.widget.Presenter
import android.support.v17.leanback.widget.Row
import android.support.v17.leanback.widget.RowPresenter
import android.util.DisplayMetrics
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentConference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.mediadata.sync.Downloader
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.leanback.CardPresenter
import de.nicidienase.chaosflix.leanback.ItemViewClickedListener
import de.nicidienase.chaosflix.leanback.activities.EventsActivity
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import kotlin.collections.HashMap

class EventsBrowseFragment : BrowseSupportFragment() {

	private lateinit var viewModel: BrowseViewModel
	private val handler = Handler()
	private var rowsAdapter: ArrayObjectAdapter = ArrayObjectAdapter(ListRowPresenter())
	private var defaultBackground: Drawable? = null
	private var metrics: DisplayMetrics? = null
	private var backgroundTimer: Timer? = null
	private var backgroundURI: URI? = null
	private var backgroundManager: BackgroundManager? = null

	private val eventRows: MutableMap<String, ListRow> = HashMap()

	private val useTalksAsBackground = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		viewModel = ViewModelProviders
				.of(this, ViewModelFactory(requireContext()))
				.get(BrowseViewModel::class.java)


		val conference = this.activity
				?.intent
				?.getParcelableExtra<PersistentConference>(EventsActivity.CONFERENCE)
		if (conference == null) {
			throw IllegalStateException("No conference passed")
		}
		setupUIElements(conference)

		val cardPresenter = CardPresenter()

		prepareBackgroundManager()
		onItemViewClickedListener = ItemViewClickedListener(this)
//		onItemViewSelectedListener = ItemViewSelectedListener()

		var errorFragment: BrowseErrorFragment? = null
		viewModel.updateEventsForConference(conference).observe(this, Observer {
			when (it?.state) {
				Downloader.DownloaderState.RUNNING -> {
					errorFragment = BrowseErrorFragment.showErrorFragment(fragmentManager, FRAGMENT)
				}
				Downloader.DownloaderState.DONE -> {
					if (it.error != null) {
						errorFragment?.setErrorContent(it.error)
					} else {
						errorFragment?.dismiss()
					}
				}
			}
			val events = it?.data
			if (events != null) {
				Log.d(TAG, "Got ${events.size} events")
				val eventsByTags = getEventsByTags(events, conference.acronym)
				Log.d(TAG, "Got ${eventsByTags.keys.size} Tags")
				for (item in eventsByTags) {
					updateRowForTag(cardPresenter, item.key, item.value)
				}
				errorFragment?.dismiss()
			}
		})
		viewModel.getEventsforConference(conference).observe(
				this,
				Observer {
					if (it != null) {
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

	private fun getEventsByTags(events: List<PersistentEvent>, conferenceAcronym: String): Map<String, List<PersistentEvent>> {
		val eventsByTags = HashMap<String, MutableList<PersistentEvent>>()
		val other = LinkedList<PersistentEvent>()
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

	private fun updateRowForTag(cardPresenter: CardPresenter, tag: String, items: List<PersistentEvent>): Row {
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
		listRowAdapter.setItems(items, object : DiffCallback<PersistentEvent>() {
			override fun areItemsTheSame(oldItem: PersistentEvent, newItem: PersistentEvent): Boolean {
				return oldItem.guid == newItem.guid
			}

			override fun areContentsTheSame(oldItem: PersistentEvent, newItem: PersistentEvent): Boolean {
				return oldItem.title == newItem.title
			}

		})
		return row
	}

	private fun prepareBackgroundManager() {
		backgroundManager = BackgroundManager.getInstance(activity)
		backgroundManager?.attach(activity?.window)
		defaultBackground = resources.getDrawable(R.drawable.default_background)
		metrics = DisplayMetrics()
		activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
	}

	private fun setupUIElements(conference: PersistentConference) {
		Glide.with(activity)
				.load(conference.logoUrl)
				.centerCrop()
				.error(defaultBackground)
				.into(object : SimpleTarget<GlideDrawable>(432, 243) {
					override fun onResourceReady(resource: GlideDrawable,
					                             glideAnimation: GlideAnimation<in GlideDrawable>) {
						badgeDrawable = resource
					}
				})
		title = conference.title // Badge, when set, takes precedent
		// over title
		headersState = BrowseSupportFragment.HEADERS_ENABLED
		isHeadersTransitionOnBackEnabled = true

		// set fastLane (or headers) background color
		brandColor = resources.getColor(R.color.fastlane_background)
		// set search icon color
		searchAffordanceColor = resources.getColor(R.color.search_opaque)

	}

	protected fun updateBackground(uri: String) {
		val width = metrics?.widthPixels ?: 100
		val height = metrics?.heightPixels ?: 100
		Glide.with(activity)
				.load(uri)
				.centerCrop()
				.error(defaultBackground)
				.into<SimpleTarget<GlideDrawable>>(object : SimpleTarget<GlideDrawable>(width, height) {
					override fun onResourceReady(resource: GlideDrawable,
					                             glideAnimation: GlideAnimation<in GlideDrawable>) {
						backgroundManager?.drawable = resource
					}
				})
		backgroundTimer?.cancel()
	}

	private fun startBackgroundTimer() {
		if (null != backgroundTimer) {
			backgroundTimer!!.cancel()
		}
		backgroundTimer = Timer()
		backgroundTimer!!.schedule(UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY.toLong())
	}

	private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
		override fun onItemSelected(itemViewHolder: Presenter.ViewHolder, item: Any,
		                            rowViewHolder: RowPresenter.ViewHolder, row: Row) {
			if (item is PersistentEvent) {
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
		private val TAG = EventsBrowseFragment::class.java.simpleName

		private val BACKGROUND_UPDATE_DELAY = 300
		private val FRAGMENT = R.id.browse_fragment
	}
}
