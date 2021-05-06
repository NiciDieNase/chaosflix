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
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
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
import de.nicidienase.chaosflix.StageConfiguration
import de.nicidienase.chaosflix.common.ChaosflixUtil
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.viewmodel.DetailsViewModel
import de.nicidienase.chaosflix.common.viewmodel.PlayerViewModel
import de.nicidienase.chaosflix.leanback.CardPresenter
import de.nicidienase.chaosflix.leanback.DiffCallbacks
import de.nicidienase.chaosflix.leanback.ItemViewClickedListener
import de.nicidienase.chaosflix.leanback.R
import de.nicidienase.chaosflix.leanback.conferences.ConferencesActivity
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.viewmodel.ext.android.viewModel

class EventDetailsFragment : DetailsSupportFragment() {

    private var selectDialog: AlertDialog? = null
    private var loadingDialog: AlertDialog? = null

    private val detailsViewModel: DetailsViewModel by viewModel()
    private val playerViewModel: PlayerViewModel by viewModel()

    private val detailsBackgroundController = DetailsSupportFragmentBackgroundController(this)

    private val playerDelegate = lazy {
        ExoPlayerFactory.newSimpleInstance(
                requireContext(),
                DefaultTrackSelector(
                        AdaptiveTrackSelection.Factory()))
    }
    private val player: SimpleExoPlayer by playerDelegate
    private val playerAdapter: LeanbackPlayerAdapter by lazy { LeanbackPlayerAdapter(requireContext(), player, 16) }
    private val playerGlue: ChaosMediaPlayerGlue by lazy {
        ChaosMediaPlayerGlue(requireContext(), playerAdapter) {
            detailsViewModel.createBookmark()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val detailsPresenter = FullWidthDetailsOverviewRowPresenter(
                EventDetailsDescriptionPresenter(requireContext()))

        val helper = FullWidthDetailsOverviewSharedElementHelper()
        helper.setSharedElementEnterTransition(activity,
                DetailsActivity.SHARED_ELEMENT_NAME)
        detailsPresenter.setListener(helper)
        prepareEntranceTransition()

        detailsPresenter.onActionClickedListener = DetailActionClickedListener()

        val selector = ClassPresenterSelector()
        val rowsAdapter = ArrayObjectAdapter(selector)
        selector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
        selector.addClassPresenter(ListRow::class.java, ListRowPresenter())
        adapter = rowsAdapter

        val guid = arguments?.getString(ARG_EVENT_GUID)
                ?: arguments?.getParcelable<Event>(ARG_EVENT)?.guid
                ?: error("Missing Argument Event")
        playerViewModel.setEvent(guid)
        val eventLiveData = detailsViewModel.setEventByGuid(guid)
        eventLiveData.observe(viewLifecycleOwner, Observer { event ->
            if (event != null) {
                title = event.title

                initializeBackgroundWithImage(event.posterUrl)

                playerGlue.title = event.title
                playerGlue.subtitle = event.subtitle ?: ""
                detailsBackgroundController.setupVideoPlayback(playerGlue)
            }
        })
        lifecycleScope.launch {
            val event = detailsViewModel.getEvent(guid)
            event?.let { onCreateRecording(it, eventLiveData, rowsAdapter) }
            detailsBackgroundController.enableParallax()

            onItemViewClickedListener = ItemViewClickedListener(this@EventDetailsFragment)

            startEntranceTransition()
            setupObserver(detailsViewModel)
        }
    }

    private fun onCreateRecording(event: Event, liveData: LiveData<Event?>, rowsAdapter: ArrayObjectAdapter) {
        val actionAdapter = ArrayObjectAdapter()
        actionAdapter.add(Action(ACTION_PLAY, "Play"))
        val watchlistAction = Action(ACTION_ADD_WATCHLIST, getString(R.string.add_to_watchlist))
        actionAdapter.add(watchlistAction)

        loadPlaybackProgress()
        detailsViewModel.getBookmarkForEvent(event.guid).observe(viewLifecycleOwner, Observer { watchlistItem ->
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
        actionAdapter.add(Action(ACTION_RELATED, getString(R.string.related_talks)))

        val detailsOverview = DetailsOverviewRow(event)
        detailsOverview.actionsAdapter = actionAdapter
        event.thumbUrl.let { setThumb(it, detailsOverview) }
        liveData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                detailsOverview.item = it
            }
        })

        rowsAdapter.add(detailsOverview)

        var relatedEventsAdapter: ArrayObjectAdapter? = null
        detailsViewModel.getRelatedEvents().observe(viewLifecycleOwner, Observer { events ->
            if (relatedEventsAdapter == null) {
                relatedEventsAdapter = ArrayObjectAdapter(CardPresenter(R.style.EventCardStyle))
                val header = HeaderItem(getString(R.string.related_talks))
                rowsAdapter.add(ListRow(header, relatedEventsAdapter))
            }
            relatedEventsAdapter?.setItems(events, DiffCallbacks.eventDiffCallback)
        })
    }

    private fun setupObserver(detailsViewModel: DetailsViewModel) {
        detailsViewModel.state.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is DetailsViewModel.State.PlayOnlineItem -> {
                    detailsBackgroundController.switchToVideo()
                    prepareSeekProvider(state.event.length)
                    preparePlayer(state.recording.recordingUrl)
                    playerAdapter.play()
                    val progress = state.progress
                    if (progress != null && progress > 10_000L) {
                        playerAdapter.seekTo(progress - 5_000)
                    }
                }
                is DetailsViewModel.State.SelectRecording -> {
                    loadingDialog?.dismiss()
                    loadingDialog = null
                    val recordings: List<Recording> = state.recordings
                            .filterNot { it.mimeType.startsWith("audio") }
                    if (recordings.isNotEmpty()) {
                        selectRecordingFromList(state.event, recordings) {
                            detailsViewModel.recordingSelected(state.event, it)
                        }
                    } else {
                        showError("Sorry, could not load recordings")
                    }
                }
                is DetailsViewModel.State.Error -> {
                    showError(state.message)
                }
                DetailsViewModel.State.LoadingRecordings -> {
                    showLoadingDialog()
                }
                is DetailsViewModel.State.PlayExternal -> {
                    if (state.recordings.size == 1) {
                        val recordingUrl = state.recordings.first().recordingUrl
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(recordingUrl)))
                    } else {
                        selectRecordingFromList(state.event, state.recordings) {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.recordingUrl)))
                        }
                    }
                }
                is DetailsViewModel.State.DisplayEvent -> {}
                is DetailsViewModel.State.OpenCustomTab -> {}
                is DetailsViewModel.State.PlayOfflineItem -> irrelevantCase()
                is DetailsViewModel.State.DownloadRecording -> irrelevantCase()
                is DetailsViewModel.State.PlayLocalFileExternal -> irrelevantCase()
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

    private fun selectRecordingFromList(event: Event, recordings: List<Recording>, resultHandler: (Recording) -> Unit) {
        val onClickListener = DialogInterface.OnClickListener { _, which -> resultHandler.invoke(recordings[which]) }
        if (selectDialog != null) {
            selectDialog?.dismiss()
        }
        val strings = recordings.map { ChaosflixUtil.getStringForRecording(it) }.toTypedArray()
        selectDialog = AlertDialog.Builder(requireContext())
                .setItems(strings, onClickListener)
                .setNegativeButton("Autoselect") { _, _ ->
                    detailsViewModel.playEvent(true)
                }
                .setPositiveButton("Always select automatically") { _, _ ->
                    detailsViewModel.autoselectRecording = true
                    detailsViewModel.playEvent(true)
                }
                .create()

        selectDialog?.show()
    }

    private fun prepareSeekProvider(length: Long) {
        lifecycleScope.launch {
            playerViewModel.getThumbInfo()?.let {
                ChaosflixSeekDataProvider.setSeekProvider(
                        playerGlue,
                        requireContext(),
                        length,
                        it
                )
            }
        }
    }

    override fun onPause() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || activity?.isInPictureInPictureMode == false) {
            playerViewModel.setPlaybackProgress(playerAdapter.currentPosition)
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

    private fun loadPlaybackProgress() {
        lifecycleScope.launch {
            val playbackProgress = playerViewModel.getPlaybackProgress()
            playbackProgress?.progress?.let { playerAdapter.seekTo(it) }
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
                get<StageConfiguration>().buildUserAgent(),
                bandwidthMeter,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true /* allowCrossProtocolRedirects */)
    }

    companion object {
        private val BANDWIDTH_METER = DefaultBandwidthMeter()
        private val TAG = EventDetailsFragment::class.java.simpleName

        private const val DETAIL_THUMB_WIDTH = 254
        private const val DETAIL_THUMB_HEIGHT = 143
        private val DEFAULT_DRAWABLE = R.drawable.default_background

        private const val ACTION_PLAY: Long = 0L
        private const val ACTION_ADD_WATCHLIST = 1L
        private const val ACTION_REMOVE_WATCHLIST = 2L
        private const val ACTION_RELATED = 3L

        const val ARG_EVENT = "event"
        const val ARG_EVENT_GUID = "event_guid"
    }

    private inner class DetailActionClickedListener : OnActionClickedListener {
        override fun onActionClicked(action: Action) {
            Log.d(TAG, "OnActionClicked")
            when (action.id) {
                ACTION_ADD_WATCHLIST -> {
                    detailsViewModel.createBookmark()
                    val preferences = requireActivity().getSharedPreferences(getString(R.string.watchlist_preferences_key), Context.MODE_PRIVATE)
                    if (preferences.getBoolean(getString(R.string.watchlist_dialog_needed), true)) { // new item
                        showWatchlistInfoDialog(preferences)
                    }
                }
                ACTION_REMOVE_WATCHLIST -> {
                    detailsViewModel.removeBookmark()
                }
                ACTION_PLAY -> {
                    if (player.playbackState == Player.STATE_IDLE) {
                        detailsViewModel.playEvent()
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
