package de.nicidienase.chaosflix.leanback.detail

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.app.DetailsSupportFragmentBackgroundController
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.DetailsOverviewRow
import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter
import androidx.leanback.widget.FullWidthDetailsOverviewSharedElementHelper
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnActionClickedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
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
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room
import de.nicidienase.chaosflix.common.mediadata.network.ApiFactory
import de.nicidienase.chaosflix.common.util.RecordingUtil
import de.nicidienase.chaosflix.common.viewmodel.DetailsViewModel
import de.nicidienase.chaosflix.common.viewmodel.PlayerViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.leanback.CardPresenter
import de.nicidienase.chaosflix.leanback.DiffCallbacks
import de.nicidienase.chaosflix.leanback.ItemViewClickedListener
import de.nicidienase.chaosflix.leanback.R
import de.nicidienase.chaosflix.leanback.conferences.ConferencesActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventDetailsFragment : DetailsSupportFragment() {

    private var selectDialog: AlertDialog? = null
    private var loadingDialog: AlertDialog? = null

    private val uiScope = CoroutineScope(Dispatchers.Main)

    private lateinit var detailsViewModel: DetailsViewModel
    private lateinit var playerViewModel: PlayerViewModel

    private var event: Event? = null
    private var room: Room? = null

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
        val viewModelFactory = ViewModelFactory.getInstance(requireContext())
        detailsViewModel = ViewModelProvider(this, viewModelFactory).get(DetailsViewModel::class.java)
        playerViewModel = ViewModelProvider(this, viewModelFactory).get(PlayerViewModel::class.java)

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

        startEntranceTransition()
        setupObserver(detailsViewModel)
    }

    private fun setupObserver(detailsViewModel: DetailsViewModel) {
        detailsViewModel.state.observe(viewLifecycleOwner, Observer { state ->
            when (state.state) {
                DetailsViewModel.State.PlayOnlineItem -> {
                    val recording: Recording? = state.data?.getParcelable(DetailsViewModel.RECORDING)
                    val parcelable: Event? = state.data?.getParcelable(DetailsViewModel.EVENT)
                    val url = state.data?.getString(DetailsViewModel.THUMBS_URL)
                    val progress: Long? = state.data?.getLong(DetailsViewModel.PROGRESS)
                    if (recording != null) {
                        if (parcelable != null) {
                            prepareSeekProvider(parcelable.length, url ?: recording.recordingUrl)
                        }
                        detailsBackgroundController.switchToVideo()
                        preparePlayer(recording.recordingUrl)
                        playerAdapter.play()
                        if (progress != null && progress > 10_000L) {
                            playerAdapter.seekTo(progress - 5_000)
                        }
                    }
                }
                DetailsViewModel.State.SelectRecording -> {
                    loadingDialog?.dismiss()
                    loadingDialog = null
                    val event: Event? = state.data?.getParcelable(DetailsViewModel.EVENT)
                    val recordings: List<Recording>? = state.data?.getParcelableArrayList<Recording>(DetailsViewModel.KEY_SELECT_RECORDINGS)
                            ?.filterNot { it.mimeType.startsWith("audio") }
                    if (event != null && recordings != null && recordings.isNotEmpty()) {
//                        RecordingSelectDialog.create(recordings) {
//                            detailsViewModel.recordingSelected(event, it)
//                        }.show(childFragmentManager)
                        selectRecordingFromList(event, recordings) {
                            detailsViewModel.recordingSelected(event, it)
                        }
                    } else {
                        showError("Sorry, could not load recordings")
                    }
                }
                DetailsViewModel.State.DisplayEvent -> {
                }
                DetailsViewModel.State.Error -> {
                    showError(state.error)
                }
                DetailsViewModel.State.Loading -> {
                    showLoadingDialog()
                }
                DetailsViewModel.State.PlayExternal -> {
                    state.data?.getParcelable<Recording>(DetailsViewModel.RECORDING)?.let { recording ->
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(recording.recordingUrl)))
                    }
                }
                DetailsViewModel.State.PlayOfflineItem -> irrelevantCase()
                DetailsViewModel.State.DownloadRecording -> irrelevantCase()
            }
        })
    }

    private fun showLoadingDialog() {
        loadingDialog = AlertDialog.Builder(requireContext())
                .setTitle("Loading Recordings")
                .create().apply { show() }
    }

    private fun irrelevantCase() {
        Log.e(TAG, "Case not relevant for leanback UI, this should not happen")
    }

    private fun showError(errorMessage: String?) {
        if (errorMessage != null && errorMessage.isNotBlank()) {
            view?.let {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectRecordingFromList(event: Event, items: List<Recording>, resultHandler: (Recording) -> Unit) {
        val onClickListener = DialogInterface.OnClickListener { _, which -> resultHandler.invoke(items[which]) }
        if (selectDialog != null) {
            selectDialog?.dismiss()
        }
        val strings = items.map { RecordingUtil.getStringForRecording(it) }.toTypedArray()
        selectDialog = AlertDialog.Builder(requireContext())
                .setItems(strings, onClickListener)
                .setNegativeButton("Autoselect") { _, _ ->
                    detailsViewModel.play(event, true)
                }
                .setPositiveButton("Always select automatically") { _, _ ->
                    detailsViewModel.autoselectRecording = true
                    detailsViewModel.play(event, true)
                }
                .create()

        selectDialog?.show()
    }

    private fun onCreateRecording(event: Event, rowsAdapter: ArrayObjectAdapter) {
        detailsViewModel.setEvent(event)

        val detailsOverview = DetailsOverviewRow(event)
        val actionAdapter = ArrayObjectAdapter()
        actionAdapter.add(Action(ACTION_PLAY, "Play"))
        val watchlistAction = Action(ACTION_ADD_WATCHLIST, getString(R.string.add_to_watchlist))
        actionAdapter.add(watchlistAction)
        event.guid.let {
            loadPlaybackProgress(it)
            detailsViewModel.getBookmarkForEvent(it).observe(viewLifecycleOwner, Observer { watchlistItem ->
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
        actionAdapter.add(Action(ACTION_RELATED, getString(R.string.related_talks)))
        detailsOverview.actionsAdapter = actionAdapter

        rowsAdapter.add(detailsOverview)
        setThumb(event.thumbUrl, detailsOverview)

        initializeBackgroundWithImage(event.posterUrl)

        detailsViewModel.getRelatedEvents(event).observe(viewLifecycleOwner, Observer { events ->
            if (relatedEventsAdapter == null) {
                relatedEventsAdapter = ArrayObjectAdapter(CardPresenter(R.style.EventCardStyle))
                val header = HeaderItem(getString(R.string.related_talks))
                rowsAdapter.add(ListRow(header, relatedEventsAdapter))
            }
            relatedEventsAdapter?.setItems(events, DiffCallbacks.eventDiffCallback)
        })
    }

    private fun prepareSeekProvider(length: Long, url: String) {
            ChaosflixSeekDataProvider.setSeekProvider(
                    playerGlue,
                    requireContext(),
                    length,
                    url
            )
    }

    private fun onCreateStream(room: Room, rowsAdapter: ArrayObjectAdapter) {
        val detailsOverview = DetailsOverviewRow(room)

        setThumb(room.thumb, detailsOverview)
        initializeBackgroundWithImage(room.thumb)

        val dashStreams = room.streams.filter { it.slug == "dash-native" }
        if (dashStreams.isNotEmpty()) { // && detailsViewModel.autoselectStream ) {
            dashStreams.first().urls["dash"]?.url?.let { preparePlayer(it, "") }
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
                        detailsOverview.imageDrawable = ContextCompat.getDrawable(requireContext(), DEFAULT_DRAWABLE)
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
            uiScope.launch {
                val playbackProgress = playerViewModel.getPlaybackProgress(eventGuid)
                playbackProgress?.progress?.let { playerAdapter.seekTo(it) }
            }
        }
    }

    private fun buildPlayerGlue(): ChaosMediaPlayerGlue {
        playerAdapter = LeanbackPlayerAdapter(context, player, 16)
        return ChaosMediaPlayerGlue(requireContext(), playerAdapter) {
            event?.guid?.let { detailsViewModel.createBookmark(it) }
        }
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
        @JvmStatic
        private val ACTION_RELATED = 3L
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
                    }
                }
                ACTION_REMOVE_WATCHLIST -> {
                    event?.guid?.let { detailsViewModel.removeBookmark(it) }
                }
                ACTION_PLAY -> {
                    if (player.playbackState == Player.STATE_IDLE) {
                        event?.let { detailsViewModel.play(it) }
                    } else {
                        detailsBackgroundController.switchToVideo()
                    }
                }
                ACTION_RELATED -> {
                    setSelectedPosition(1)
                }
            }
        }

        fun showWatchlistInfoDialog(preferences: SharedPreferences) {
            val builder = AlertDialog.Builder(requireContext())
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
