package de.nicidienase.chaosflix.leanback.detail

import android.app.AlertDialog
import android.content.DialogInterface
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
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnActionClickedListener
import androidx.lifecycle.ViewModelProvider
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
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Stream
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamUrl
import de.nicidienase.chaosflix.common.mediadata.network.ApiFactory
import de.nicidienase.chaosflix.common.viewmodel.DetailsViewModel
import de.nicidienase.chaosflix.common.viewmodel.PlayerViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.leanback.ItemViewClickedListener
import de.nicidienase.chaosflix.leanback.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class StreamDetailsFragment : DetailsSupportFragment() {

    private var selectDialog: AlertDialog? = null

    private lateinit var detailsViewModel: DetailsViewModel
    private lateinit var playerViewModel: PlayerViewModel

    private lateinit var room: Room

    private lateinit var rowsAdapter: ArrayObjectAdapter

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

        room = arguments?.getParcelable(DetailsActivity.ROOM) ?: error("Missing Argument Room")

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
        playerGlue.title = room.display
        detailsBackgroundController.setupVideoPlayback(playerGlue)

        onItemViewClickedListener = ItemViewClickedListener(this)

        onCreateStream(room, rowsAdapter)
        adapter = this.rowsAdapter

        startEntranceTransition()
    }

    private fun showError(errorMessage: String?) {
        if (errorMessage != null && errorMessage.isNotBlank()) {
            view?.let {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectStreamFromList(room: Room, streams: List<Stream>, resultHandler: (StreamUrl) -> Unit) {
        if (selectDialog != null) {
            selectDialog?.dismiss()
        }
        val streamUrls: Map<String, StreamUrl> = streams.flatMap { stream -> stream.urls.map { "${stream.display} ${it.key}" to it.value } }.toMap()
        val keys = streamUrls.keys.toTypedArray()
        val onClickListener = DialogInterface.OnClickListener { _, which ->
            val selectedKey = keys[which]
            streamUrls[selectedKey]?.let { resultHandler.invoke(it) } ?: Log.e(TAG, "Selected Stream somehow does not exist")
        }
        selectDialog = AlertDialog.Builder(requireContext())
                .setItems(keys, onClickListener)
                .setNegativeButton("Autoselect") { _, _ ->
                    getAutoselectedStream(room)?.let { resultHandler.invoke(it) }
                }
                .setPositiveButton("Always select automatically") { _, _ ->
                    detailsViewModel.autoselectStream = true
                    getAutoselectedStream(room)?.let { resultHandler.invoke(it) }
                }
                .create()

        selectDialog?.show()
    }

    private fun onCreateStream(room: Room, rowsAdapter: ArrayObjectAdapter) {
        val detailsOverview = DetailsOverviewRow(room)

        setThumb(room.thumb, detailsOverview)
        initializeBackgroundWithImage(room.thumb)

        val streamUrl = getAutoselectedStream(room)
        if (detailsViewModel.autoselectStream && streamUrl != null) {
            detailsBackgroundController.switchToVideo()
            preparePlayer(streamUrl.url, "")
        } else {
            selectStreamFromList(room, room.streams) {
                detailsBackgroundController.switchToVideo()
                preparePlayer(it.url)
            }
        }

        val actionAdapter = ArrayObjectAdapter()

        val playAction = Action(ACTION_PLAY, "Play")
        actionAdapter.add(playAction)

        detailsOverview.actionsAdapter = actionAdapter
        rowsAdapter.add(detailsOverview)
    }

    private fun getAutoselectedStream(room: Room): StreamUrl? {
        val dashStreams = room.streams.filter { it.slug == "dash-native" }
        return dashStreams.first().urls["dash"]
    }

    override fun onPause() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || activity?.isInPictureInPictureMode == false) {
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
        val TAG = StreamDetailsFragment::class.java.simpleName

        @JvmStatic
        private val DETAIL_THUMB_WIDTH = 254
        @JvmStatic
        private val DETAIL_THUMB_HEIGHT = 143
        @JvmStatic
        val DEFAULT_DRAWABLE = R.drawable.default_background

        @JvmStatic
        private val ACTION_PLAY: Long = 0L
    }

    private inner class DetailActionClickedListener : OnActionClickedListener {
        override fun onActionClicked(action: Action) {
            Log.d(TAG, "OnActionClicked")
            when (action.id) {
                ACTION_PLAY -> {
                    detailsBackgroundController.switchToVideo()
                }
            }
        }

    }
}
