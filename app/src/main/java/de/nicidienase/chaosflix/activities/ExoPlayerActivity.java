package de.nicidienase.chaosflix.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.PlaybackFragment;
import android.view.TextureView;
import android.view.View;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.entities.recording.Event;
import de.nicidienase.chaosflix.entities.recording.Recording;
import de.nicidienase.chaosflix.fragments.PlaybackOverlayFragment;

/**
 * Created by felix on 26.03.17.
 */

public class ExoPlayerActivity extends AbstractServiceConnectedAcitivty
		implements PlaybackOverlayFragment.OnPlayPauseClickedListener{

	@BindView(R.id.videoView)
	TextureView mTextureView;
	@BindView(R.id.playback_controls_fragment)
	View mPlaybackControllFragment;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exoplayback_activity);
		ButterKnife.bind(this);

		Handler mainHander = new Handler();
		DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
		TrackSelection.Factory videoTrackSelectionFactory
				= new AdaptiveTrackSelection.Factory(bandwidthMeter);
		TrackSelector trackSelector
				= new DefaultTrackSelector(videoTrackSelectionFactory);

		LoadControl loadControl = new DefaultLoadControl();

		SimpleExoPlayer player =
				ExoPlayerFactory.newSimpleInstance(this,trackSelector,loadControl);

		player.setVideoTextureView(mTextureView);
	}

	@Override
	public void onFragmentPlayPause(Event event, Recording recording, int position, Boolean playPause) {

	}
}
