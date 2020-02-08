package de.nicidienase.chaosflix.leanback.detail

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v17.leanback.app.DetailsSupportFragment
import android.support.v17.leanback.app.DetailsSupportFragmentBackgroundController
import android.support.v17.leanback.widget.Action
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.ClassPresenterSelector
import android.support.v17.leanback.widget.DetailsOverviewRow
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter
import android.support.v17.leanback.widget.FullWidthDetailsOverviewSharedElementHelper
import android.support.v17.leanback.widget.HeaderItem
import android.support.v17.leanback.widget.ListRow
import android.support.v17.leanback.widget.ListRowPresenter
import android.support.v17.leanback.widget.OnActionClickedListener
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Util
import de.nicidienase.chaosflix.common.ChaosflixUtil
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room
import de.nicidienase.chaosflix.common.mediadata.network.ApiFactory
import de.nicidienase.chaosflix.common.viewmodel.DetailsViewModel
import de.nicidienase.chaosflix.common.viewmodel.PlayerViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.leanback.CardPresenter
import de.nicidienase.chaosflix.leanback.DiffCallbacks
import de.nicidienase.chaosflix.leanback.EventDetailsDescriptionPresenter
import de.nicidienase.chaosflix.leanback.ItemViewClickedListener
import de.nicidienase.chaosflix.leanback.R
import de.nicidienase.chaosflix.leanback.conferences.ConferencesActivity

class EventDetailsFragment : DetailsSupportFragment() {

    private lateinit var detailsViewModel: DetailsViewModel
    private lateinit var playerViewModel: PlayerViewModel

    private var event: Event? = null
    private var room: Room? = null

    private var currentRecordings: List<Recording>? = null

    private lateinit var rowsAdapter: ArrayObjectAdapter

    private var relatedEventsAdapter: ArrayObjectAdapter? = null

    private val detailsBackgroundController = DetailsSupportFragmentBackgroundController(this)

    private val playerDelegate = lazy {
        ExoPlayerFactory.newSimpleInstance(
            activity,
            DefaultTrackSelector(
                AdaptiveTrackSelection.Factory()))
    }
    private val player: SimpleExoPlayer by playerDelegate
    private lateinit var playerAdapter: LeanbackPlayerAdapter
    private lateinit var playerGlue: ChaosMediaPlayerGlue

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModelFactory = ViewModelFactory(requireContext())
        detailsViewModel = ViewModelProviders.of(this, viewModelFactory).get(DetailsViewModel::class.java)
        playerViewModel = ViewModelProviders.of(this, viewModelFactory).get(PlayerViewModel::class.java)

        event = activity?.intent?.getParcelableExtra(DetailsActivity.EVENT)
        room = activity?.intent?.getParcelableExtra(DetailsActivity.ROOM)

        val eventType =
            when {
                event != null -> DetailsActivity.TYPE_RECORDING
                room != null -> DetailsActivity.TYPE_STREAM
                else -> -1
            }

        title = event?.title

        val selector = ClassPresenterSelector()
        val detailsPresenter = FullWidthDetailsOverviewRowPresenter(
            EventDetailsDescriptionPresenter(requireContext()))

        val helper = FullWidthDetailsOverviewSharedElementHelper()
        helper.setSharedElementEnterTransition(activity,
            DetailsActivity.SHARED_ELEMENT_NAME)
        detailsPresenter.setListener(helper)
        prepareEntranceTransition()

        detailsPresenter.onActionClickedListener = DetailActionClickedListener()

        selector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
        selector.addClassPresenter(ListRow::class.java, ListRowPresenter())
        rowsAdapter = ArrayObjectAdapter(selector)

        detailsBackgroundController.enableParallax()
        playerGlue = buildPlayerGlue()
        playerGlue.title = event?.title ?: room?.display
        playerGlue.subtitle = event?.subtitle ?: ""
        detailsBackgroundController.setupVideoPlayback(playerGlue)

        onItemViewClickedListener = ItemViewClickedListener(this)

        when (eventType) {
            DetailsActivity.TYPE_RECORDING -> event?.let { onCreateRecording(it, rowsAdapter) }
            DetailsActivity.TYPE_STREAM -> room?.let { onCreateStream(it, rowsAdapter) }
        }
        adapter = this.rowsAdapter

        Handler().postDelayed(this::startEntranceTransition, 500)
    }

    private fun onCreateRecording(event: Event, rowsAdapter: ArrayObjectAdapter) {

        val detailsOverview = DetailsOverviewRow(event)
        val actionAdapter = ArrayObjectAdapter()
        actionAdapter.add(Action(ACTION_PLAY, "Play"))
        val watchlistAction = Action(ACTION_ADD_WATCHLIST, getString(R.string.add_to_watchlist))
        actionAdapter.add(watchlistAction)
        event.guid.let {
            loadPlaybackProgress(it)
            detailsViewModel.getBookmarkForEvent(it).observe(this, Observer { watchlistItem ->
                if (watchlistItem != null) {
                    watchlistAction.id = ACTION_REMOVE_WATCHLIST
                    watchlistAction.label1 = getString(R.string.remove_from_watchlist)
                    actionAdapter.notifyItemRangeChanged(actionAdapter.indexOf(watchlistAction), 1)
                } else {
                    watchlistAction.id = ACTION_ADD_WATCHLIST
                    watchlistAction.label1 = getString(R.string.add_to_watchlist)
                    actionAdapter.notifyItemRangeChanged(actionAdapter.indexOf(watchlistAction), 1)
                }
            })
        }
        detailsOverview.actionsAdapter = actionAdapter

        rowsAdapter.add(detailsOverview)
        setThumb(event.thumbUrl, detailsOverview)

        initializeBackgroundWithImage(event.posterUrl)

        detailsViewModel.getRecordingForEvent(event).observe(this, Observer { recordings ->
            if (recordings != null && !recordings.isEmpty()) {
                onNewRecordings(event, recordings)
            } else {
                Log.d(TAG, "no recording for thumbs found")
            }
        })

        detailsViewModel.getRelatedEvents(event).observe(this, Observer { events ->
            if (relatedEventsAdapter == null) {
                relatedEventsAdapter = ArrayObjectAdapter(CardPresenter(R.style.EventCardStyle))
                val header = HeaderItem(getString(R.string.related_talks))
                rowsAdapter.add(ListRow(header, relatedEventsAdapter))
            }
            relatedEventsAdapter?.setItems(events, DiffCallbacks.eventDiffCallback)
        })
    }

    private fun onNewRecordings(event: Event, recordings: List<Recording>) {
        if (currentRecordings.isNullOrEmpty()) {
            preparePlayer(recordings, event)
            prepareSeekProvider(recordings, event)
        }
        currentRecordings = recordings
    }

    private fun prepareSeekProvider(
        recordings: List<Recording>,
        event: Event
    ) {
        ChaosflixUtil.getRecordingForThumbs(recordings)?.recordingUrl?.let {
            ChaosflixSeekDataProvider.setSeekProvider(
                playerGlue,
                requireContext(),
                event.length,
                it
            )
        }
    }

    private fun preparePlayer(
        recordings: List<Recording>,
        event: Event
    ) {
        val optimalRecording = ChaosflixUtil.getOptimalRecording(recordings, event.originalLanguage)
        preparePlayer(optimalRecording.recordingUrl)
    }

    fun play(action: Action?) {
        detailsBackgroundController.switchToVideo()
        playerAdapter.play()
        action?.label1 = getString(R.string.pause)
    }

    private fun onCreateStream(room: Room, rowsAdapter: ArrayObjectAdapter) {
        val detailsOverview = DetailsOverviewRow(room)

        setThumb(room.thumb, detailsOverview)
        initializeBackgroundWithImage(room.thumb)

        val dashStreams = room.streams.filter { it.slug == "dash-native" }
        if (dashStreams.size > 0) {
// 				&& detailsViewModel.getAutoselectStream()) {
            dashStreams.first().urls.get("dash")?.url?.let { preparePlayer(it, "") }
        }

        val actionAdapter = ArrayObjectAdapter()

        val playAction = Action(ACTION_PLAY, "Play")
        actionAdapter.add(playAction)

        detailsOverview.actionsAdapter = actionAdapter
        rowsAdapter.add(detailsOverview)
    }

    override fun onPause() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || activity?.isInPictureInPictureMode == false) {
            event?.let { event ->
                playerViewModel.setPlaybackProgress(event.guid, playerAdapter.currentPosition)
            }
            playerAdapter.pause()
        }
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        if (playerDelegate.isInitialized()) {
            player.release()
        }
    }

    private fun setThumb(thumbUrl: String, detailsOverview: DetailsOverviewRow) {
        Glide.with(requireContext())
            .asBitmap()
            .load(thumbUrl)
            .into(object : SimpleTarget<Bitmap>(DETAIL_THUMB_WIDTH, DETAIL_THUMB_HEIGHT) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    detailsOverview.setImageBitmap(requireContext(), resource)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    detailsOverview.setImageDrawable(ContextCompat.getDrawable(requireContext(), DEFAULT_DRAWABLE))
                }
            })
    }

    private fun initializeBackgroundWithImage(url: String) {
        detailsBackgroundController.enableParallax()
        val options = RequestOptions()
            .fallback(R.drawable.default_background)
        Glide.with(requireContext())
            .asBitmap()
            .load(url)
            .apply(options)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    detailsBackgroundController.coverBitmap = resource
                }
            })
    }

    private fun loadPlaybackProgress(eventGuid: String? = null) {
        if (eventGuid != null) {
            playerViewModel.getPlaybackProgress(eventGuid)
                .observe(this@EventDetailsFragment, Observer { progress ->
                    progress?.let {
                        playerAdapter.seekTo(it.progress)
                    }
                })
        }
    }

    private fun buildPlayerGlue(): ChaosMediaPlayerGlue {
        playerAdapter = LeanbackPlayerAdapter(context, player, 16)
        return ChaosMediaPlayerGlue(requireContext(), playerAdapter)
    }

    private fun preparePlayer(url: String, overrideExtension: String = "") {
        player.prepare(buildMediaSource(Uri.parse(url), overrideExtension))
    }

    private fun buildMediaSource(uri: Uri, overrideExtension: String): MediaSource {
        val mediaDataSourceFactory = buildDataSourceFactory()
        val type = if (TextUtils.isEmpty(overrideExtension)) {
            Util.inferContentType(uri)
        } else
            Util.inferContentType(".$overrideExtension")
        when (type) {
            C.TYPE_DASH -> return DashMediaSource.Factory(
                DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                buildDataSourceFactory())
                .createMediaSource(uri)
            C.TYPE_HLS -> return HlsMediaSource.Factory(buildDataSourceFactory())
                .createMediaSource(uri)
            C.TYPE_SS, C.TYPE_OTHER -> return ExtractorMediaSource.Factory(mediaDataSourceFactory)
                .createMediaSource(uri)
            else -> {
                throw IllegalStateException("Unsupported type: $type")
            }
        }
    }

    private fun buildDataSourceFactory(useBandwidthMeter: Boolean = true): DataSource.Factory {
        return buildDataSourceFactory(if (useBandwidthMeter) BANDWIDTH_METER else null)
    }

    private fun buildDataSourceFactory(bandwidthMeter: DefaultBandwidthMeter?): DataSource.Factory {
        return DefaultDataSourceFactory(requireContext(), bandwidthMeter,
            buildHttpDataSourceFactory(bandwidthMeter))
    }

    private fun buildHttpDataSourceFactory(bandwidthMeter: DefaultBandwidthMeter?): HttpDataSource.Factory {
        return DefaultHttpDataSourceFactory(
            ApiFactory.buildUserAgent(),
            bandwidthMeter,
            DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
            DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
            true /* allowCrossProtocolRedirects */)
    }

    companion object {
        private val BANDWIDTH_METER = DefaultBandwidthMeter()
        @JvmStatic
        val TAG = EventDetailsFragment::class.java.simpleName

        @JvmStatic
        private val DETAIL_THUMB_WIDTH = 254
        @JvmStatic
        private val DETAIL_THUMB_HEIGHT = 143
        @JvmStatic
        val DEFAULT_DRAWABLE = R.drawable.default_background

        @JvmStatic
        private val ACTION_PLAY: Long = 0L
        @JvmStatic
        private val ACTION_ADD_WATCHLIST = 1L
        @JvmStatic
        private val ACTION_REMOVE_WATCHLIST = 2L
    }

    private inner class DetailActionClickedListener : OnActionClickedListener {
        override fun onActionClicked(action: Action) {
            Log.d(TAG, "OnActionClicked")
            when (action.id) {
                ACTION_ADD_WATCHLIST -> {
                    event?.guid?.let { detailsViewModel.createBookmark(it) }
                    val preferences = requireActivity().getSharedPreferences(getString(R.string.watchlist_preferences_key), Context.MODE_PRIVATE)
                    if (preferences.getBoolean(getString(R.string.watchlist_dialog_needed), true)) { // new item
                        showWatchlistInfoDialog(preferences)
                    } }
                ACTION_REMOVE_WATCHLIST -> { event?.guid?.let { detailsViewModel.removeBookmark(it) } }
                ACTION_PLAY -> { play(action) }
            }
        }

        fun showWatchlistInfoDialog(preferences: SharedPreferences) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(R.string.watchlist_message)
            builder.setNegativeButton(R.string.return_to_homescreen) { _, _ ->
                val i = Intent(activity, ConferencesActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
            }
            builder.setPositiveButton("OK") { _, _ -> }
            val edit = preferences.edit()
            edit.putBoolean(getString(R.string.watchlist_dialog_needed), false)
            edit.apply()

            builder.create().show()
        }
    }
}
