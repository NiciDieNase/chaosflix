package de.nicidienase.chaosflix.activities;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.PlaybackFragment;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
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
import com.google.android.exoplayer2.util.UriUtil;
import com.google.android.exoplayer2.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.entities.recording.Event;
import de.nicidienase.chaosflix.entities.recording.Recording;
import de.nicidienase.chaosflix.fragments.ExoOverlayFragment;
import de.nicidienase.chaosflix.fragments.PlaybackOverlayFragment;

/**
 * Created by felix on 26.03.17.
 */

public class ExoPlayerActivity extends AbstractServiceConnectedAcitivty
		implements ExoOverlayFragment.PlaybackControlListener{

	@BindView(R.id.videoView)
	TextureView mTextureView;
	@BindView(R.id.playback_controls_fragment)
	View mPlaybackControllFragment;
	private DefaultBandwidthMeter bandwidthMeter;
	private SimpleExoPlayer player;
	private String mUserAgent;

	private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exoplayback_activity);
		ButterKnife.bind(this);

		mUserAgent = Util.getUserAgent(this, getResources().getString(R.string.app_name));
		synchronized (this){
			if(player == null){
				setupPlayer();
			}
		}
	}

	private void setupPlayer(){
		mUserAgent = Util.getUserAgent(this, getResources().getString(R.string.app_name));

		Handler mainHander = new Handler();
		bandwidthMeter = new DefaultBandwidthMeter();
		TrackSelection.Factory videoTrackSelectionFactory
				= new AdaptiveTrackSelection.Factory(bandwidthMeter);
		TrackSelector trackSelector
				= new DefaultTrackSelector(videoTrackSelectionFactory);

		LoadControl loadControl = new DefaultLoadControl();

		player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

		player.setVideoTextureView(mTextureView);
	}

	@Override
	public void setVideoSource(String source) {
		synchronized (this){
			if(player == null){
				setupPlayer();
			}
		}
//		source = "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8";
		MediaSource mediaSource = buildMediaSource(Uri.parse(source), "");
		player.setPlayWhenReady(true);
		player.prepare(mediaSource);
	}

	@Override
	public void play() {
	}

	@Override
	public void pause() {

	}

	@Override
	public void playPause() {

	}

	private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
		DataSource.Factory mediaDataSourceFactory = buildDataSourceFactory(true);
		int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
				: Util.inferContentType("." + overrideExtension);
		switch (type) {
			case C.TYPE_SS:
				return new SsMediaSource(uri, buildDataSourceFactory(false),
						new DefaultSsChunkSource.Factory(mediaDataSourceFactory), null, null);
			case C.TYPE_DASH:
				return new DashMediaSource(uri, buildDataSourceFactory(false),
						new DefaultDashChunkSource.Factory(mediaDataSourceFactory), null, null);
			case C.TYPE_HLS:
				return new HlsMediaSource(uri, mediaDataSourceFactory, null, null);
			case C.TYPE_OTHER:
				return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
						null, null);
			default: {
				throw new IllegalStateException("Unsupported type: " + type);
			}
		}
	}
	private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
		return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
	}

	public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
		return new DefaultDataSourceFactory(this, bandwidthMeter,
				buildHttpDataSourceFactory(bandwidthMeter));
	}

	public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
		return new DefaultHttpDataSourceFactory(mUserAgent, bandwidthMeter);
	}
}
