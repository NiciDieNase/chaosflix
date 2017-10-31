package de.nicidienase.chaosflix.touch.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Event;
import de.nicidienase.chaosflix.common.entities.recording.Recording;
import de.nicidienase.chaosflix.common.entities.userdata.PlaybackProgress;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;

public class ExoPlayerFragment extends Fragment implements MyListener.PlayerStateChangeListener {
	private static final String TAG = ExoPlayerFragment.class.getSimpleName();
	public static final String PLAYBACK_STATE = "playback_state";
	private static final String ARG_EVENT = "event";
	private static final String ARG_RECORDING = "recording";

	private OnMediaPlayerInteractionListener mListener;
	private final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
	private CompositeDisposable disposable = new CompositeDisposable();

	@BindView(R.id.video_view)
	SimpleExoPlayerView videoView;
	@BindView(R.id.progressBar)
	ProgressBar mProgressBar;

	@Nullable
	@BindView(R.id.title_text)
	TextView titleText;
	@Nullable
	@BindView(R.id.subtitle_text)
	TextView subtitleText;

	private String mUserAgent;
	private Handler mainHandler = new Handler();
	private boolean mPlaybackState = true;
	private Event mEvent;
	private Recording mRecording;
	private SimpleExoPlayer exoPlayer;

	public ExoPlayerFragment() {
	}

	public static ExoPlayerFragment newInstance(Event event, Recording recording) {
		ExoPlayerFragment fragment = new ExoPlayerFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_EVENT, event);
		args.putParcelable(ARG_RECORDING, recording);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mEvent = getArguments().getParcelable(ARG_EVENT);
			mRecording = getArguments().getParcelable(ARG_RECORDING);
		}
		if (savedInstanceState != null) {
			mPlaybackState = savedInstanceState.getBoolean(PLAYBACK_STATE, true);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_exo_player, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.bind(this, view);
		if (titleText != null)
			titleText.setText(mEvent.getTitle());
		if (subtitleText != null)
			subtitleText.setText(mEvent.getSubtitle());

		if (exoPlayer == null) {
			exoPlayer = setupPlayer();
		} else {
			exoPlayer = exoPlayer;
			Log.d(TAG, "Player already set up.");
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		if (exoPlayer != null) {
			exoPlayer.setPlayWhenReady(mPlaybackState);
			disposable.add(mListener.getPlaybackProgress(mEvent.getApiID())
					.subscribe(playbackProgress -> {
						if (playbackProgress != null) {
							exoPlayer.seekTo(playbackProgress.getProgress());
						}
					}));
			videoView.setPlayer(exoPlayer);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (exoPlayer != null) {
			mListener.setPlaybackProgress(mEvent.getApiID(), exoPlayer.getCurrentPosition());
			exoPlayer.setPlayWhenReady(false);
		}
		disposable.clear();
	}

	@Override
	public void onStart() {
		super.onStart();
		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (exoPlayer != null) {
			outState.putBoolean(PLAYBACK_STATE, exoPlayer.getPlayWhenReady());
		}
	}

	private SimpleExoPlayer setupPlayer() {
		Log.d(TAG, "Setting up Player.");
		videoView.setKeepScreenOn(true);

		mUserAgent = Util.getUserAgent(getContext(), getResources().getString(R.string.app_name));

		AdaptiveTrackSelection.Factory trackSelectorFactory
				= new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
		DefaultTrackSelector trackSelector = new DefaultTrackSelector(trackSelectorFactory);
		LoadControl loadControl = new DefaultLoadControl();
		DefaultRenderersFactory renderersFactory
				= new DefaultRenderersFactory(getContext(), null, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);


		exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
		MyListener listener = new MyListener(exoPlayer, this);
		exoPlayer.addVideoListener(listener);
		exoPlayer.addListener(listener);

		exoPlayer.setPlayWhenReady(mPlaybackState);

		exoPlayer.prepare(buildMediaSource(Uri.parse(mRecording.getRecordingUrl()), ""));
		return exoPlayer;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnMediaPlayerInteractionListener) {
			mListener = (OnMediaPlayerInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void notifyLoadingStart() {
		if (mProgressBar != null) {
			mProgressBar.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void notifyLoadingFinished() {
		if (mProgressBar != null) {
			mProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void notifyError(String errorMessage) {
		Snackbar.make(videoView, errorMessage, Snackbar.LENGTH_LONG).show();
	}

	public interface OnMediaPlayerInteractionListener {
		Flowable<PlaybackProgress> getPlaybackProgress(long apiId);

		void setPlaybackProgress(long apiId, long progress);
	}

	private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
		DataSource.Factory mediaDataSourceFactory = buildDataSourceFactory(true);
		int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
				: Util.inferContentType("." + overrideExtension);
		switch (type) {
			case C.TYPE_SS:
				return new SsMediaSource(uri, buildDataSourceFactory(false),
						new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, null);
			case C.TYPE_DASH:
				return new DashMediaSource(uri, buildDataSourceFactory(false),
						new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, null);
			case C.TYPE_HLS:
				return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, null);
			case C.TYPE_OTHER:
				return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
						mainHandler, null);
			default: {
				throw new IllegalStateException("Unsupported type: " + type);
			}
		}
	}

	private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
		return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
	}

	private DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
		return new DefaultDataSourceFactory(getContext(), bandwidthMeter,
				buildHttpDataSourceFactory(bandwidthMeter));
	}

	private HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
		return new DefaultHttpDataSourceFactory(mUserAgent, bandwidthMeter);
	}
}
