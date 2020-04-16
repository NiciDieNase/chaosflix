package de.nicidienase.chaosflix.touch.playback

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
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
import com.google.android.material.snackbar.Snackbar
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgress
import de.nicidienase.chaosflix.common.viewmodel.PlayerViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.databinding.FragmentExoPlayerBinding
import de.nicidienase.chaosflix.touch.playback.PlayerEventListener.PlayerStateChangeListener

class ExoPlayerFragment : Fragment(), PlayerStateChangeListener {
    private val BANDWIDTH_METER = DefaultBandwidthMeter()
    private var userAgent: String? = null
    private val mainHandler = Handler()
    private var playbackState = true
    private var exoPlayer: SimpleExoPlayer? = null
    private lateinit var viewModel: PlayerViewModel

    private var binding: FragmentExoPlayerBinding? = null

    private val args: ExoPlayerFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            playbackState = savedInstanceState.getBoolean(PLAYBACK_STATE, true)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentExoPlayerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_exo_player, container, false)
        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(requireContext())).get(PlayerViewModel::class.java)
        viewModel.setEvent(args.playbackItem.eventGuid)
// 		val toolbar: Toolbar = binding.getRoot().findViewById(R.id.toolbar)
// 		toolbar.title = args.playbackItem.title
// 		toolbar.subtitle = args.playbackItem.subtitle
// 		val activity = activity as AppCompatActivity?
// 		if (activity != null) {
// 			activity.setSupportActionBar(toolbar)
// 			val actionBar = activity.supportActionBar
// 			actionBar?.setDisplayHomeAsUpEnabled(true)
// 		}
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (exoPlayer == null) {
            exoPlayer = setupPlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.apply {
            viewModel.setPlaybackProgress(currentPosition)
            playWhenReady = false
        }
        val activity = activity
        if (activity != null) {
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    override fun onStart() {
        super.onStart()
        val activity = activity
        if (activity != null) {
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        }
        if (exoPlayer != null) {
            exoPlayer?.playWhenReady = playbackState
            viewModel.getPlaybackProgressLiveData().observe(this, Observer { playbackProgress: PlaybackProgress? ->
                if (playbackProgress != null) {
                    exoPlayer?.seekTo(playbackProgress.progress)
                }
            })
            binding?.videoView?.player = exoPlayer
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (exoPlayer != null) {
            outState.putBoolean(PLAYBACK_STATE, exoPlayer?.playWhenReady ?: false)
        }
    }

    private fun setupPlayer(): SimpleExoPlayer? {
        Log.d(TAG, "Setting up Player.")
        binding?.videoView?.keepScreenOn = true
        userAgent = Util.getUserAgent(context, resources.getString(R.string.app_name))
        val trackSelectorFactory = AdaptiveTrackSelection.Factory(BANDWIDTH_METER)
        val trackSelector = DefaultTrackSelector(trackSelectorFactory)
        val renderersFactory = DefaultRenderersFactory(
                context,
                null,
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
        exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector).apply {
            val listener = PlayerEventListener(this, this@ExoPlayerFragment)
            this.addVideoListener(listener)
            this.addListener(listener)

            this.playWhenReady = this@ExoPlayerFragment.playbackState
            this.prepare(buildMediaSource(Uri.parse(args.playbackItem.uri), ""))
        }
        return exoPlayer
    }

    override fun notifyLoadingStart() {
        binding?.progressBar?.visibility = View.VISIBLE
    }

    override fun notifyLoadingFinished() {
        binding?.progressBar?.visibility = View.INVISIBLE
    }

    override fun notifyError(errorMessage: String?) {
        binding?.root?.let {
            Snackbar.make(it, errorMessage ?: "Error", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun notifyEnd() {
        viewModel.deletePlaybackProgress()
    }

    private fun buildMediaSource(uri: Uri, overrideExtension: String): MediaSource {
        val mediaDataSourceFactory = buildDataSourceFactory(true)
        val type = if (TextUtils.isEmpty(overrideExtension)) Util.inferContentType(uri) else Util.inferContentType(".$overrideExtension")
        return when (type) {
            C.TYPE_SS -> SsMediaSource(uri, buildDataSourceFactory(false), DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, null)
            C.TYPE_DASH -> DashMediaSource(uri, buildDataSourceFactory(false), DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, null)
            C.TYPE_HLS -> HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, null)
            C.TYPE_OTHER -> ExtractorMediaSource(uri, mediaDataSourceFactory, DefaultExtractorsFactory(), mainHandler, null)
            else -> {
                throw IllegalStateException("Unsupported type: $type")
            }
        }
    }

    private fun buildDataSourceFactory(useBandwidthMeter: Boolean): DataSource.Factory {
        return buildDataSourceFactory(if (useBandwidthMeter) BANDWIDTH_METER else null)
    }

    private fun buildDataSourceFactory(bandwidthMeter: DefaultBandwidthMeter?): DataSource.Factory {
        return DefaultDataSourceFactory(context, bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter))
    }

    private fun buildHttpDataSourceFactory(bandwidthMeter: DefaultBandwidthMeter?): HttpDataSource.Factory {
        return DefaultHttpDataSourceFactory(userAgent,
                bandwidthMeter,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true)
    }

    companion object {
        private val TAG = ExoPlayerFragment::class.java.simpleName
        private const val PLAYBACK_STATE = "playback_state"
        private const val ARG_item = "item"
        fun newInstance(item: PlaybackItem): ExoPlayerFragment {
            return ExoPlayerFragment().apply {
               arguments = ExoPlayerFragmentArgs(item).toBundle()
           }
        }
    }
}
