package de.nicidienase.chaosflix.activities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.os.BuildCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
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
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.fragments.ExoOverlayFragment;

/**
 * Created by felix on 26.03.17.
 */

public class ExoPlayerActivity extends AbstractServiceConnectedAcitivty
		implements ExoOverlayFragment.PlaybackControlListener{

	private static final String TAG = ExoPlayerActivity.class.getSimpleName();
	@BindView(R.id.videoView)
	SurfaceView mSurfaceView;
	ExoOverlayFragment mPlaybackControllFragment;
	private DefaultBandwidthMeter bandwidthMeter;
	private SimpleExoPlayer player;
	private String mUserAgent;

	private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
	private Handler mainHandler;
	private DefaultTrackSelector trackSelector;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exoplayback_activity);
		ButterKnife.bind(this);

		mPlaybackControllFragment = (ExoOverlayFragment) getFragmentManager().findFragmentById(R.id.playback_controls_fragment);

		mUserAgent = Util.getUserAgent(this, getResources().getString(R.string.app_name));
		synchronized (this){
			if(player == null){
				setupPlayer();
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
//		mPlaybackControllFragment.startEntranceTransition();
	}

	@Override
	protected void onPause() {
		super.onPause();
		pause();
		// TODO persist playback progress
	}

	private void setupPlayer(){
		mUserAgent = Util.getUserAgent(this, getResources().getString(R.string.app_name));

		mainHandler = new Handler();
		bandwidthMeter = new DefaultBandwidthMeter();
		TrackSelection.Factory videoTrackSelectionFactory
				= new AdaptiveTrackSelection.Factory(bandwidthMeter);
		trackSelector
				= new DefaultTrackSelector(videoTrackSelectionFactory);

		LoadControl loadControl = new DefaultLoadControl();

		player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

		player.setVideoSurfaceView(mSurfaceView);
	}

	@Override
	public void setVideoSource(String source) {
		Log.d(TAG,"Source: " + source);
		synchronized (this){
			if(player == null){
				setupPlayer();
			}
		}
		MediaSource mediaSource = buildMediaSource(Uri.parse(source), "");
		player.prepare(mediaSource);
	}

	@Override
	public void play() {
		player.setPlayWhenReady(true);
	}

	@Override
	public void pause() {
		player.setPlayWhenReady(false);

	}

	@Override
	public void playPause() {
		player.setPlayWhenReady(!player.getPlayWhenReady());
	}

	@Override
	public void seekTo(long sec) {
		player.seekTo(sec * 1000);
	}

	@Override
	public boolean isMediaPlaying() {
		if(player != null){
			return player.getPlayWhenReady();
		} else {
			return false;
		}
	}

	@Override
	public long getCurrentPosition() {
		if(player != null){
			return player.getCurrentPosition();
		} else {
			return 0;
		}
	}

	@Override
	public void releasePlayer() {
		if(player != null){
			player.release();
		}
	}

	@Override
	public long getPosition() {
		if(player != null){
			return player.getCurrentPosition();
		}
		return 0;
	}

	@Override
	public long getBufferedPosition() {
		if(player != null){
			return player.getBufferedPosition();
		}
		return 0;
	}

	@Override
	public void skipForward(int sec){
		player.seekTo(player.getCurrentPosition()+(sec*1000));
	}

	@Override
	public void skipBackward(int sec){
		player.seekTo(player.getCurrentPosition()-(sec*1000));
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
		return new DefaultDataSourceFactory(this, bandwidthMeter,
				buildHttpDataSourceFactory(bandwidthMeter));
	}

	private HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
		return new DefaultHttpDataSourceFactory(mUserAgent, bandwidthMeter);
	}
}
