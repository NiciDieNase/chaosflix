package de.nicidienase.chaosflix.leanback.fragments

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
import android.support.v17.leanback.media.PlaybackTransportControlGlue
import android.support.v17.leanback.widget.Action
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.ClassPresenterSelector
import android.support.v17.leanback.widget.DetailsOverviewRow
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter
import android.support.v17.leanback.widget.FullWidthDetailsOverviewSharedElementHelper
import android.support.v17.leanback.widget.ListRow
import android.support.v17.leanback.widget.ListRowPresenter
import android.support.v17.leanback.widget.OnActionClickedListener
import android.text.TextUtils
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Util
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.ChaosflixUtil
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room
import de.nicidienase.chaosflix.common.viewmodel.DetailsViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.leanback.EventDetailsDescriptionPresenter
import de.nicidienase.chaosflix.leanback.activities.ConferencesActivity
import de.nicidienase.chaosflix.leanback.activities.DetailsActivity
import de.nicidienase.chaosflix.leanback.activities.EventDetailsActivity


class NewEventsDetailsFragment : DetailsSupportFragment() {

	private lateinit var viewModel: DetailsViewModel

	private var eventType: Int = DetailsActivity.TYPE_RECORDING
	private var event: PersistentEvent? = null
	private var room: Room? = null

	private lateinit var rowsAdapter: ArrayObjectAdapter

	private val detailsBackground = DetailsSupportFragmentBackgroundController(this)

	private val mainHandler = Handler()

	private var player: SimpleExoPlayer? = null
	private lateinit var playerAdapter: LeanbackPlayerAdapter
	private lateinit var playerGlue: ChaosMediaPlayerGlue

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		viewModel = ViewModelProviders.of(this, ViewModelFactory(requireContext())).get(DetailsViewModel::class.java)

		eventType = activity!!.intent.getIntExtra(DetailsActivity.TYPE, -1)
		event = requireActivity().intent.getParcelableExtra<PersistentEvent>(DetailsActivity.EVENT)
		room = requireActivity().intent.getParcelableExtra<Room>(DetailsActivity.ROOM)

		title = event?.title

		val selector = ClassPresenterSelector()
		val detailsPresenter = FullWidthDetailsOverviewRowPresenter(
				EventDetailsDescriptionPresenter(requireContext()))

		val helper = FullWidthDetailsOverviewSharedElementHelper()
		helper.setSharedElementEnterTransition(activity,
				EventDetailsActivity.SHARED_ELEMENT_NAME)
		detailsPresenter.setListener(helper)
		prepareEntranceTransition()

		detailsPresenter.onActionClickedListener = DetailActionClickedListener()

		selector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
		selector.addClassPresenter(ListRow::class.java,
				ListRowPresenter())
		rowsAdapter = ArrayObjectAdapter(selector)

		when (eventType) {
			DetailsActivity.TYPE_RECORDING -> event?.let { onCreateRecording(it, rowsAdapter) }
			DetailsActivity.TYPE_STREAM -> room?.let { onCreateStream(it, rowsAdapter) }
		}

		adapter = this.rowsAdapter

		Handler().postDelayed(this::startEntranceTransition, 500);

	}


	fun play() {
		detailsBackground.switchToVideo()
		playerAdapter.play()
	}

	private fun onCreateRecording(event: PersistentEvent, rowsAdapter: ArrayObjectAdapter) {

		val detailsOverview = DetailsOverviewRow(event)

		setThumb(event.thumbUrl, detailsOverview)

		viewModel.getRecordingForEvent(event).observe(this, Observer { recordings ->
			if (recordings != null) {
				val optimalRecording = ChaosflixUtil.getOptimalRecording(recordings)
				optimalRecording?.recordingUrl?.let { initializeBackgroundAndPlayer(it) }
			}
		})

		val actionAdapter = ArrayObjectAdapter()

		val playAction = Action(ACTION_PLAY, "Play")
		actionAdapter.add(playAction)

		val watchlistAction = Action(ACTION_ADD_WATCHLIST, getString(R.string.add_to_watchlist))
		actionAdapter.add(watchlistAction)
		event.guid.let {
			viewModel.getBookmarkForEvent(it).observe(this, Observer { watchlistItem ->
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
		rowsAdapter.add(detailsOverview);
	}

	private fun onCreateStream(room: Room, rowsAdapter: ArrayObjectAdapter) {
		val detailsOverview = DetailsOverviewRow(room)

		setThumb(room.thumb, detailsOverview)

		val dashStreams = room.streams.filter { it.slug == "dash-native" }
		if (dashStreams.size > 0){
//				&& viewModel.getAutoselectStream()) {
			dashStreams.first().urls["dash"]?.url?.let { initializeBackgroundAndPlayer(it, "") }
		}

		val actionAdapter = ArrayObjectAdapter()

		val playAction = Action(ACTION_PLAY, "Play")
		actionAdapter.add(playAction)

		detailsOverview.actionsAdapter = actionAdapter
		rowsAdapter.add(detailsOverview);
	}

	override fun onPause() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || activity?.isInPictureInPictureMode == false) {
			val currentMillis = playerAdapter.currentPosition
			val durationMillis = playerAdapter.duration
			if (currentMillis != null && durationMillis != null) {
				// TODO save progress
			}
			detailsBackground.playbackGlue?.pause()
		}
		super.onPause()
	}

	fun setThumb(thumbUrl: String, detailsOverview: DetailsOverviewRow) {
		Glide.with(activity)
				.asBitmap()
				.load(thumbUrl)
				.into(object : SimpleTarget<Bitmap>(DETAIL_THUMB_WIDTH, DETAIL_THUMB_HEIGHT) {
					override fun onResourceReady(resource: Bitmap?, transition: Transition<in Bitmap>?) {
						if (resource != null) {
							detailsOverview.setImageBitmap(requireContext(), resource)
						}
					}

					override fun onLoadFailed(errorDrawable: Drawable?) {
						detailsOverview.setImageDrawable(resources.getDrawable(DEFAULT_DRAWABLE))
					}
				})
	}

	private fun initializeBackgroundAndPlayer(url: String, overrideExtension: String = "") {
		detailsBackground.enableParallax()
		detailsBackground.setupVideoPlayback(initializePlayer(url,overrideExtension))
		Log.d(TAG, "Player ready")

	}

	private fun initializePlayer(url: String, overrideExtension: String = ""): PlaybackTransportControlGlue<LeanbackPlayerAdapter> {
		val bandwidthMeter = DefaultBandwidthMeter()
		val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
		val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)

		player = ExoPlayerFactory.newSimpleInstance(activity, trackSelector)
		playerAdapter = LeanbackPlayerAdapter(activity, player, 16)

		player?.prepare(buildMediaSource(Uri.parse(url), overrideExtension))

		playerGlue = ChaosMediaPlayerGlue(requireContext(), playerAdapter)

		return playerGlue
	}

//	private fun buildMediaSource(uri: Uri, overrideExtension: String = ""): MediaSource {
//		val bandwidthMeter = if (true) BANDWIDTH_METER else null
//		val mediaDataSourceFactory = DefaultDataSourceFactory(requireContext(), bandwidthMeter, DefaultHttpDataSourceFactory(Util.getUserAgent(requireContext(), getResources().getString(R.string.app_name)), bandwidthMeter,
//				DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
//				DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
//				true /* allowCrossProtocolRedirects */))
//
//		val type = if (TextUtils.isEmpty(overrideExtension))
//			Util.inferContentType(uri)
//		else
//			Util.inferContentType(".$overrideExtension")
//		when (type) {
//			C.TYPE_SS -> return ExtractorMediaSource.Factory(mediaDataSourceFactory)
//					.createMediaSource(uri)
//			C.TYPE_DASH -> return DashMediaSource(uri, buildDataSourceFactory(false),
//					DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, null)
//			C.TYPE_HLS -> return HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, null)
//			C.TYPE_OTHER -> return ExtractorMediaSource(uri, mediaDataSourceFactory, DefaultExtractorsFactory(),
//					mainHandler, null)
//			else -> {
//				throw IllegalStateException("Unsupported type: $type")
//			}
//		}
//	}


	private fun buildMediaSource(uri: Uri, overrideExtension: String): MediaSource {
		val mediaDataSourceFactory = buildDataSourceFactory(true)
		val type = if (TextUtils.isEmpty(overrideExtension)) {
			Util.inferContentType(uri)
		}
		else
			Util.inferContentType(".$overrideExtension")
		when (type) {
			C.TYPE_DASH -> return DashMediaSource.Factory(
						DefaultDashChunkSource.Factory(mediaDataSourceFactory),
						buildDataSourceFactory(true))
						.createMediaSource(uri)
			C.TYPE_HLS -> return HlsMediaSource.Factory(buildDataSourceFactory(true))
					.createMediaSource(uri)
			C.TYPE_SS, C.TYPE_OTHER -> return ExtractorMediaSource.Factory(mediaDataSourceFactory)
					.createMediaSource(uri)
			else -> {
				throw IllegalStateException("Unsupported type: $type")
			}
		}
	}

	private fun buildDataSourceFactory(useBandwidthMeter: Boolean): DataSource.Factory {
		return buildDataSourceFactory(if (useBandwidthMeter) BANDWIDTH_METER else null)
	}

	private fun buildDataSourceFactory(bandwidthMeter: DefaultBandwidthMeter?): DataSource.Factory {
		return DefaultDataSourceFactory(requireContext(), bandwidthMeter,
				buildHttpDataSourceFactory(bandwidthMeter))
	}

	private fun buildHttpDataSourceFactory(bandwidthMeter: DefaultBandwidthMeter?): HttpDataSource.Factory {
		return DefaultHttpDataSourceFactory(
				Util.getUserAgent(requireContext(), getResources().getString(R.string.app_name)),
				bandwidthMeter,
				DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
				DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
				true /* allowCrossProtocolRedirects */)
	}

	companion object {
		private val BANDWIDTH_METER = DefaultBandwidthMeter()
		@JvmStatic
		val TAG = NewEventsDetailsFragment::class.java.simpleName

		@JvmStatic
		private val DETAIL_THUMB_WIDTH = 254
		@JvmStatic
		private val DETAIL_THUMB_HEIGHT = 143
		@JvmStatic
		val DEFAULT_DRAWABLE = R.drawable.default_background


		@JvmStatic
		private val ACTION_PLAY: Long = 0
		@JvmStatic
		private val ACTION_ADD_WATCHLIST: Long = 1
		@JvmStatic
		private val ACTION_REMOVE_WATCHLIST: Long = 2
	}

	private inner class DetailActionClickedListener : OnActionClickedListener {
		override fun onActionClicked(action: Action) {
			Log.d(TAG, "OnActionClicked")
			if (action.id == ACTION_ADD_WATCHLIST) {
				event?.guid?.let { viewModel.createBookmark(it) }
				val preferences = requireActivity().getSharedPreferences(getString(R.string.watchlist_preferences_key), Context.MODE_PRIVATE)
				if (preferences.getBoolean(getString(R.string.watchlist_dialog_needed), true)) { // new item
					showWatchlistInfoDialog(preferences)
				}
			} else if (action.id == ACTION_REMOVE_WATCHLIST) {
				event?.guid?.let { viewModel.removeBookmark(it) }
			} else if (action.id == ACTION_PLAY) {
				play()
			}
		}

		fun showWatchlistInfoDialog(preferences: SharedPreferences) {
			val builder = AlertDialog.Builder(activity)
			builder.setTitle(R.string.watchlist_message)
			builder.setNegativeButton(R.string.return_to_homescreen) { dialog, which ->
				val i = Intent(activity, ConferencesActivity::class.java)
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
				startActivity(i)
				//						getActivity().finish();
			}
			builder.setPositiveButton("OK") { dialog, which -> }
			val edit = preferences.edit()
			edit.putBoolean(getString(R.string.watchlist_dialog_needed), false)
			edit.apply()

			builder.create().show()
		}
	}
}