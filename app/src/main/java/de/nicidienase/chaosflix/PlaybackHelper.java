package de.nicidienase.chaosflix;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.PlaybackControlGlue;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ControlButtonPresenterSelector;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;

import de.nicidienase.chaosflix.activities.ExoPlayerActivity;
import de.nicidienase.chaosflix.entities.recording.Event;
import de.nicidienase.chaosflix.entities.recording.Recording;
import de.nicidienase.chaosflix.entities.streaming.Room;
import de.nicidienase.chaosflix.entities.streaming.StreamUrl;
import de.nicidienase.chaosflix.fragments.ExoOverlayFragment;

/**
 * Created by felix on 26.03.17.
 */

public class PlaybackHelper extends PlaybackControlGlue {

	private static final int[] SEEK_SPEEDS = {2, 4, 8, 16};
	private static final int DEFAULT_UPDATE_PERIOD = 500;
	private static final int UPDATE_PERIOD = 16;
	private static final String TAG = PlaybackHelper.class.getSimpleName();
	private ExoOverlayFragment.PlaybackControlListener controlListener;
	private BitmapDrawable mDrawable = null;
	private Room room;
	private StreamUrl stream;
	private ExoOverlayFragment fragment;
	private Event event;
	private Recording recording;
	private Runnable mUpdateProgressRunnable;
	private Handler mHandler = new Handler();

	PlaybackControlsRow.PlayPauseAction mPlayPauseAction;
	PlaybackControlsRow.FastForwardAction mFastForwardAction;
	PlaybackControlsRow.RewindAction mRewindAction;
	PlaybackControlsRow.SkipNextAction mSkipNextAction;
	PlaybackControlsRow.SkipPreviousAction mSkipPreviousAction;
	private ExoOverlayFragment.PlaybackControlListener mCallback;

	private MediaController mMediaController;
	private MediaController.TransportControls mTransportControls;
	private ArrayObjectAdapter adapter;

	public PlaybackHelper(Context context, ExoOverlayFragment fragment, Event event, Recording recording){
		super(context,SEEK_SPEEDS);
		controlListener = (ExoOverlayFragment.PlaybackControlListener) context;
		this.fragment = fragment;
		this.event = event;
		this.recording = recording;

		setup();

//		if(event != null){
//			Observable.fromCallable(() ->
//					new BitmapDrawable(
//							mContext.getResources(),
//							Glide.with(getContext())
//									.load(event.getThumbUrl())
//									.asBitmap()
//									.into(-1, -1)
//									.get()))
//					.subscribeOn(Schedulers.io())
//					.observeOn(AndroidSchedulers.mainThread())
//					.doOnError(Throwable::printStackTrace)
//					.subscribe(bitmapDrawable -> mDrawable = bitmapDrawable);
//		}
	}

	public PlaybackHelper(Context context, ExoOverlayFragment fragment, Room room, StreamUrl stream ){
		super(context, SEEK_SPEEDS);
		controlListener = (ExoOverlayFragment.PlaybackControlListener) context;
		this.fragment = fragment;
		this.room = room;
		this.stream = stream;

		setup();
	}

//	@Override
//	public PlaybackControlsRowPresenter createControlsRowAndPresenter() {
//		PlaybackControlsRowPresenter presenter = super.createControlsRowAndPresenter();
//		SparseArrayObjectAdapter primaryActionsAdapter = (SparseArrayObjectAdapter) getControlsRow().getPrimaryActionsAdapter();
//
//		ArrayObjectAdapter adapter = new ArrayObjectAdapter(new ControlButtonPresenterSelector());
//		getControlsRow().setSecondaryActionsAdapter(adapter);
//
//		mPlayPauseAction = (PlaybackControlsRow.PlayPauseAction) primaryActionsAdapter.lookup(ACTION_PLAY_PAUSE);
//		mFastForwardAction = (PlaybackControlsRow.FastForwardAction) primaryActionsAdapter.lookup(ACTION_FAST_FORWARD);
//		mRewindAction = (PlaybackControlsRow.RewindAction) primaryActionsAdapter.lookup(ACTION_REWIND);
//		mSkipNextAction = (PlaybackControlsRow.SkipNextAction) primaryActionsAdapter.lookup(ACTION_SKIP_TO_NEXT);
//		mSkipPreviousAction = (PlaybackControlsRow.SkipPreviousAction) primaryActionsAdapter.lookup(ACTION_SKIP_TO_PREVIOUS);
//
////		presenter.setOnActionClickedListener(action -> dispatch(action));
//		return presenter;
//	}

	private void setup() {
		mMediaController = fragment.getActivity().getMediaController();
		mTransportControls = mMediaController.getTransportControls();
	}

	@Override
	public PlaybackControlsRowPresenter createControlsRowAndPresenter() {
		PlaybackControlsRowPresenter presenter = super.createControlsRowAndPresenter();
		adapter = new ArrayObjectAdapter(new ControlButtonPresenterSelector());
		getControlsRow().setSecondaryActionsAdapter(adapter);

		presenter.setOnActionClickedListener(new OnActionClickedListener() {
			@Override
			public void onActionClicked(Action action) {
				dispatchAction(action);
			}
		});
		return presenter;
	}

	@TargetApi(Build.VERSION_CODES.N)
	public void dispatchAction(Action action) {
		if(action instanceof PlaybackControlsRow.MultiAction){
			PlaybackControlsRow.MultiAction multiAction = (PlaybackControlsRow.MultiAction) action;
			multiAction.nextIndex();
			notifyActionChanged(multiAction);
		}
		super.onActionClicked(action);
	}

	private void notifyActionChanged(PlaybackControlsRow.MultiAction multiAction) {
		int index;
		index = getPrimaryActionsAdapter().indexOf(multiAction);
		if (index >= 0) {
			getPrimaryActionsAdapter().notifyArrayItemRangeChanged(index, 1);
		} else {
			index = getSecondaryActionsAdapter().indexOf(multiAction);
			if (index >= 0) {
				getSecondaryActionsAdapter().notifyArrayItemRangeChanged(index, 1);
			}
		}
	}

	@Override
	public void enableProgressUpdating(boolean enable) {
		mHandler.removeCallbacks(mUpdateProgressRunnable);
		if(enable){
			mHandler.post(mUpdateProgressRunnable);
		}
	}

	@Override
	public void updateProgress() {
		if(mUpdateProgressRunnable == null){
			mUpdateProgressRunnable = new Runnable() {
				@Override
				public void run() {
					int totalTime = getControlsRow().getTotalTime();
					long currentPosition = controlListener.getCurrentPosition();
					getControlsRow().setCurrentTimeLong(currentPosition);

					long bufferedPosition = controlListener.getBufferedPosition();
					getControlsRow().setBufferedProgressLong(bufferedPosition);

					if (totalTime > 0 && totalTime <= currentPosition) {
						stopProgressAnimation();
					} else {
						updateProgress();
					}
				}
			};
		}
		mHandler.postDelayed(mUpdateProgressRunnable, getUpdatePeriod());
	}

	@Override
	public boolean hasValidMedia() {
		return mediaIsRecording()||mediaIsStream();
	}

	@Override
	public boolean isMediaPlaying() {
		return controlListener.isMediaPlaying();
	}

	@Override
	public CharSequence getMediaTitle() {
		if(mediaIsRecording()){
			return event.getTitle();
		}
		if(mediaIsStream()){
			return room.getDisplay();
		}
		return null;
	}

	@Override
	public CharSequence getMediaSubtitle() {
		if(mediaIsRecording()){
			return event.getSubtitle();
		}
		if(mediaIsStream()){
			return stream.getDisplay();
		}
		return null;
	}

	@Override
	public int getMediaDuration() {
		if(mediaIsRecording()){
			return event.getLength() * 1000;
		}
		return 0;
	}

	@Override
	public Drawable getMediaArt() {
		return mDrawable;
	}

	@Override
	protected void startPlayback(int speed) {
		if(getCurrentSpeedId() == speed){
			return;
		}
		mTransportControls.play();
	}

	@Override
	protected void pausePlayback() {
		mTransportControls.pause();
	}


	private void stopProgressAnimation() {
		if (mHandler != null && mUpdateProgressRunnable != null) {
			mHandler.removeCallbacks(mUpdateProgressRunnable);
			mUpdateProgressRunnable = null;
		}
	}

	@Override
	public long getSupportedActions() {
		return ACTION_PLAY_PAUSE | ACTION_FAST_FORWARD | ACTION_REWIND;
	}

	@Override
	public int getCurrentSpeedId() {
		return isMediaPlaying() ? PLAYBACK_SPEED_NORMAL : PLAYBACK_SPEED_PAUSED;
	}

	@Override
	public int getCurrentPosition() {
		return (int) controlListener.getCurrentPosition();
	}

	public long getCurrentPositionLong(){
		return controlListener.getCurrentPosition();
	}

	private boolean mediaIsStream() {
		return (room != null && stream != null);
	}
	private boolean mediaIsRecording(){
		return (event != null && recording != null);
	}

	public MediaController.Callback createMediaControllerCallback() {
		return new ChaosflixMediaControllerCallback();
	}

	private SparseArrayObjectAdapter getPrimaryActionsAdapter() {
		return (SparseArrayObjectAdapter) getControlsRow().getPrimaryActionsAdapter();
	}

	private ArrayObjectAdapter getSecondaryActionsAdapter(){
		return (ArrayObjectAdapter) getControlsRow().getSecondaryActionsAdapter();
	}

	private class ChaosflixMediaControllerCallback extends MediaController.Callback {

		@Override
		public void onPlaybackStateChanged(@NonNull PlaybackState state) {
			if(state.getState() != PlaybackState.STATE_NONE){
				updateProgress();
			}
			onStateChanged();
		}

		@Override
		public void onMetadataChanged(@Nullable MediaMetadata metadata) {
			PlaybackHelper.this.onMetadataChanged();
			PlaybackHelper.this.adapter.notifyArrayItemRangeChanged(0,1);
		}
	}
}
