package de.nicidienase.chaosflix.touch.playback;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import de.nicidienase.chaosflix.touch.R;
import de.nicidienase.chaosflix.common.viewmodel.PlayerViewModel;
import de.nicidienase.chaosflix.touch.databinding.FragmentExoPlayerBinding;
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory;

public class ExoPlayerFragment extends Fragment implements PlayerEventListener.PlayerStateChangeListener {
	private static final String TAG = ExoPlayerFragment.class.getSimpleName();
	private static final String PLAYBACK_STATE = "playback_state";
	private static final String ARG_item = "item";

	private final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

	private String userAgent;
	private Handler mainHandler = new Handler();
	private boolean playbackState = true;
	private SimpleExoPlayer exoPlayer;
	private PlayerViewModel viewModel;
	private PlaybackItem item;

	FragmentExoPlayerBinding binding;

	public ExoPlayerFragment() {
	}

	public static ExoPlayerFragment newInstance(PlaybackItem item) {
		ExoPlayerFragment fragment = new ExoPlayerFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_item, item);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			item = getArguments().getParcelable(ARG_item);
		}
		if (savedInstanceState != null) {
			playbackState = savedInstanceState.getBoolean(PLAYBACK_STATE, true);
		}

		viewModel = ViewModelProviders.of(this, new ViewModelFactory(requireContext())).get(PlayerViewModel.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = DataBindingUtil.inflate(inflater, R.layout.fragment_exo_player, container, false);

		Toolbar toolbar = binding.getRoot().findViewById(R.id.toolbar);
		toolbar.setTitle(item.getTitle());
		toolbar.setSubtitle(item.getSubtitle());
		AppCompatActivity activity = (AppCompatActivity) getActivity();
		if(activity != null){
			activity.setSupportActionBar(toolbar);
			ActionBar actionBar = activity.getSupportActionBar();
			if (actionBar != null) {
				actionBar.setDisplayHomeAsUpEnabled(true);
			}
		}

		return binding.getRoot();
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (exoPlayer == null) {
			exoPlayer = setupPlayer();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (exoPlayer != null) {
			viewModel.setPlaybackProgress(item.getEventGuid() , exoPlayer.getCurrentPosition());
			exoPlayer.setPlayWhenReady(false);
		}
		FragmentActivity activity = getActivity();
		if(activity != null){
			Window window = activity.getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}


	@Override
	public void onStart() {
		super.onStart();
		FragmentActivity activity = getActivity();
		if(activity != null){
			Window window = activity.getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}
		if (exoPlayer != null) {
			exoPlayer.setPlayWhenReady(playbackState);
			viewModel.getPlaybackProgress(item.getEventGuid()).observe(this, playbackProgress -> {
				if (playbackProgress != null) {
					exoPlayer.seekTo(playbackProgress.getProgress());
				}
			});
			binding.videoView.setPlayer(exoPlayer);
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		if (exoPlayer != null) {
			outState.putBoolean(PLAYBACK_STATE, exoPlayer.getPlayWhenReady());
		}
	}

	private SimpleExoPlayer setupPlayer() {
		Log.d(TAG, "Setting up Player.");
		binding.videoView.setKeepScreenOn(true);

		userAgent = Util.getUserAgent(getContext(), getResources().getString(R.string.app_name));

		AdaptiveTrackSelection.Factory trackSelectorFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
		DefaultTrackSelector trackSelector = new DefaultTrackSelector(trackSelectorFactory);
		LoadControl loadControl = new DefaultLoadControl();
		DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(getContext(), null, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);


		exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
		PlayerEventListener listener = new PlayerEventListener(exoPlayer, this);
		exoPlayer.addVideoListener(listener);
		exoPlayer.addListener(listener);

		exoPlayer.setPlayWhenReady(playbackState);

		exoPlayer.prepare(buildMediaSource(Uri.parse(item.getUri()), ""));
		return exoPlayer;
	}

	@Override
	public void notifyLoadingStart() {
		if (binding.progressBar != null) {
			binding.progressBar.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void notifyLoadingFinished() {
		if (binding.progressBar != null) {
			binding.progressBar.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void notifyError(String errorMessage) {
		Snackbar.make(binding.videoView, errorMessage, Snackbar.LENGTH_LONG).show();
	}

	@Override
	public void notifyEnd() {
		viewModel.deletePlaybackProgress(item.getEventGuid());
	}

	private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
		DataSource.Factory mediaDataSourceFactory = buildDataSourceFactory(true);
		int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri) : Util.inferContentType("." + overrideExtension);
		switch (type) {
			case C.TYPE_SS:
				return new SsMediaSource(uri, buildDataSourceFactory(false), new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, null);
			case C.TYPE_DASH:
				return new DashMediaSource(uri, buildDataSourceFactory(false), new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, null);
			case C.TYPE_HLS:
				return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, null);
			case C.TYPE_OTHER:
				return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(), mainHandler, null);
			default: {
				throw new IllegalStateException("Unsupported type: " + type);
			}
		}
	}

	private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
		return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
	}

	private DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
		return new DefaultDataSourceFactory(getContext(), bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
	}

	private HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
		return new DefaultHttpDataSourceFactory(userAgent,
				bandwidthMeter,
				DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
				DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
				true);
	}
}
