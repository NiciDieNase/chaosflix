package de.nicidienase.chaosflix.leanback.events

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.v17.leanback.app.BackgroundManager
import android.support.v17.leanback.app.VerticalGridSupportFragment
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.FocusHighlight
import android.support.v17.leanback.widget.OnItemViewSelectedListener
import android.support.v17.leanback.widget.Presenter
import android.support.v17.leanback.widget.Row
import android.support.v17.leanback.widget.RowPresenter
import android.support.v17.leanback.widget.VerticalGridPresenter
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.leanback.CardPresenter
import de.nicidienase.chaosflix.leanback.ItemViewClickedListener
import de.nicidienase.chaosflix.leanback.R
import java.net.URI
import java.net.URISyntaxException
import java.util.Timer
import java.util.TimerTask

class EventsGridBrowseFragment : VerticalGridSupportFragment(), EventsActivity.EventsFragment {
    private val NUM_COLUMNS = 4

    private val handler = Handler()
    private val rowsAdapter: ArrayObjectAdapter =
        ArrayObjectAdapter(CardPresenter(R.style.EventGridCardStyle))
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

        val conference: Conference? = arguments?.getParcelable(EventsRowsBrowseFragment.CONFERENCE)
        if (conference == null) {
            throw IllegalStateException("No conference passed")
        }

        loadImage(conference.logoUrl, this::setBadgeDrawable)
        title = conference.title
        val presenter = VerticalGridPresenter(FocusHighlight.ZOOM_FACTOR_MEDIUM)
        presenter.numberOfColumns = NUM_COLUMNS
        gridPresenter = presenter

// 		prepareBackgroundManager()
        onItemViewClickedListener = ItemViewClickedListener(this)
// 		onItemViewSelectedListener = ItemViewSelectedListener()

        adapter = rowsAdapter
    }

    override fun updateEvents(conference: Conference, events: List<Event>) {
        rowsAdapter.setItems(events, null)
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

    fun loadImage(url: String, consumer: (Drawable) -> Unit) {
        val options = RequestOptions()
        options.centerCrop()

        Glide.with(this)
            .load(url)
            .apply(options)
            .into(object : SimpleTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
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
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder,
            item: Any,
            rowViewHolder: RowPresenter.ViewHolder,
            row: Row
        ) {
            if (item is Event) {
                try {
                    backgroundURI = URI(item.posterUrl)
                } catch (e: URISyntaxException) {
                    e.printStackTrace()
                }

                // TODO make configurable (enable/disable)
                if (useTalksAsBackground) {
                    startBackgroundTimer()
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

        private const val BACKGROUND_UPDATE_DELAY = 300
        private const val CONFERENCE = "conference"

        fun create(conference: Conference): EventsGridBrowseFragment {
            return EventsGridBrowseFragment().apply {
                arguments = Bundle().apply { putParcelable(CONFERENCE, conference) }
            }
        }
    }
}
