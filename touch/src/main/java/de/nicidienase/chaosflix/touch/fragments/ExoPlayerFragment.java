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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
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

public class ExoPlayerFragment extends Fragment {
	private static final String TAG = ExoPlayerFragment.class.getSimpleName();
	public static final String PLAYBACK_STATE = "playback_state";
	private static final String ARG_EVENT = "event";
	private static final String ARG_RECORDING = "recording";

	private OnMediaPlayerInteractionListener mListener;
	private final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

	@BindView(R.id.video_view)
	SimpleExoPlayerView mVideoView;
	@BindView(R.id.progressBar)
	ProgressBar mProgressBar;

	@Nullable
	@BindView(R.id.title_text)
	TextView titleText;
	@Nullable
	@BindView(R.id.subtitle_text)
	TextView subtitleText;

	private SimpleExoPlayer mPlayer;
	private String mUserAgent;
	private Handler mainHandler = new Handler();
	private boolean mPlaybackState = true;
	private Event mEvent;
	private Recording mRecording;

	public ExoPlayerFragment() {
	}

	public static ExoPlayerFragment newInstance(Event event, Recording recording) {
		ExoPlayerFragment fragment = new ExoPlayerFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_EVENT,event);
		args.putParcelable(ARG_RECORDING,recording);
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
		if(savedInstanceState != null){
			mPlaybackState = savedInstanceState.getBoolean(PLAYBACK_STATE,true);
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
		ButterKnife.bind(this,view);
		if(titleText != null)
			titleText.setText(mEvent.getTitle());
		if(subtitleText != null)
			subtitleText.setText(mEvent.getSubtitle());

		if(mPlayer == null){
			setupPlayer();
		} else {
			mVideoView.setPlayer(mPlayer);
			Log.d(TAG,"Player already set up.");
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		getView().setSystemUiVisibility(View.INVISIBLE);
		if(mPlayer != null){
			mPlayer.setPlayWhenReady(mPlaybackState);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getView().setSystemUiVisibility(View.VISIBLE);
		if(mPlayer != null){
			mPlayer.setPlayWhenReady(false);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(mPlayer != null){
			outState.putBoolean(PLAYBACK_STATE, mPlayer.getPlayWhenReady());
		}
	}

	private void setupPlayer(){
		Log.d(TAG,"Setting up Player.");
		mVideoView.setKeepScreenOn(true);

		mUserAgent = Util.getUserAgent(getContext(), getResources().getString(R.string.app_name));

		AdaptiveTrackSelection.Factory trackSelectorFactory
				= new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
		DefaultTrackSelector trackSelector = new DefaultTrackSelector(trackSelectorFactory);
		LoadControl loadControl = new DefaultLoadControl();
		DefaultRenderersFactory renderersFactory
				= new DefaultRenderersFactory(getContext(), null, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);

		mPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
		mPlayer.addVideoListener(new MyListener());
		mPlayer.addListener(new MyListener());

		mVideoView.setPlayer(mPlayer);
		mPlayer.setPlayWhenReady(mPlaybackState);

		mPlayer.prepare(buildMediaSource(Uri.parse(mRecording.getRecordingUrl()),""));
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

	private void showLoadingSpinner(){
		if(mProgressBar != null){
			mProgressBar.setVisibility(View.VISIBLE);
		}
	}

	private void hideLoadingSpinner(){
		if(mProgressBar != null){
			mProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	public interface OnMediaPlayerInteractionListener {
	}

	private class MyListener implements Player.EventListener, SimpleExoPlayer.VideoListener {
		@Override
		public void onTimelineChanged(Timeline timeline, Object manifest) {

		}

		@Override
		public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

		}

		@Override
		public void onLoadingChanged(boolean isLoading) {
			if(isLoading && mPlayer.getPlaybackState() != Player.STATE_READY){
				showLoadingSpinner();
			} else{
				hideLoadingSpinner();
			}

		}

		@Override
		public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
			switch (playbackState){
					case Player.STATE_BUFFERING:
						if(mPlayer.isLoading()){
							showLoadingSpinner();
						}
						break;
					case Player.STATE_ENDED:
						Log.d(TAG,"Finished Playback");

						break;
					case Player.STATE_IDLE:
					case Player.STATE_READY:
					default:
						hideLoadingSpinner();
}
		}

		@Override
		public void onRepeatModeChanged(int repeatMode) {

		}

		@Override
		public void onPlayerError(ExoPlaybackException error) {
				String errorMessage = error.getCause().getMessage();
				Snackbar.make(mVideoView,errorMessage,Snackbar.LENGTH_LONG).show();
				Log.d(TAG,errorMessage,error);
		}

		@Override
		public void onPositionDiscontinuity() {

		}

		@Override
		public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

		}

		@Override
		public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

		}

		@Override
		public void onRenderedFirstFrame() {
			hideLoadingSpinner();
		}
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
