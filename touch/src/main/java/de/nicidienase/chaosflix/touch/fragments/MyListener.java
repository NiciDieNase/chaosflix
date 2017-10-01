package de.nicidienase.chaosflix.touch.fragments;

import android.support.design.widget.Snackbar;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

/**
 * Created by felix on 27.09.17.
 */
class MyListener implements Player.EventListener, SimpleExoPlayer.VideoListener {
	private static final String TAG = MyListener.class.getSimpleName();
	private SimpleExoPlayer player;
	private PlayerStateChangeListener listener;

	public MyListener(SimpleExoPlayer player, PlayerStateChangeListener listener) {
		this.player = player;
		this.listener = listener;
	}

	@Override
	public void onTimelineChanged(Timeline timeline, Object manifest) {

	}

	@Override
	public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

	}

	@Override
	public void onLoadingChanged(boolean isLoading) {
		if (isLoading && player.getPlaybackState() != Player.STATE_READY) {
			listener.notifyLoadingStart();
		} else {
			listener.notifyLoadingFinished();
		}

	}

	@Override
	public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
		switch (playbackState) {
			case Player.STATE_BUFFERING:
				if (player.isLoading()) {
					listener.notifyLoadingStart();
				}
				break;
			case Player.STATE_ENDED:
				Log.d(TAG, "Finished Playback");

				break;
			case Player.STATE_IDLE:
			case Player.STATE_READY:
			default:
				listener.notifyLoadingFinished();
		}
	}

	@Override
	public void onRepeatModeChanged(int repeatMode) {

	}

	@Override
	public void onPlayerError(ExoPlaybackException error) {
		String errorMessage = error.getCause().getMessage();
		listener.notifyError(errorMessage);
		Log.d(TAG, errorMessage, error);
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
		listener.notifyLoadingFinished();
	}

	public interface PlayerStateChangeListener{
		void notifyLoadingStart();
		void notifyLoadingFinished();
		void notifyError(String errorMessage);
	}
}
