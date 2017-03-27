package de.nicidienase.chaosflix;


import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.session.MediaController;
import android.os.Handler;
import android.support.v17.leanback.app.PlaybackControlGlue;
import android.view.View;

import com.bumptech.glide.Glide;

import de.nicidienase.chaosflix.entities.recording.Event;
import de.nicidienase.chaosflix.entities.recording.Recording;
import de.nicidienase.chaosflix.entities.streaming.Room;
import de.nicidienase.chaosflix.entities.streaming.Stream;
import de.nicidienase.chaosflix.fragments.ExoOverlayFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by felix on 26.03.17.
 */

public class PlaybackHelper extends PlaybackControlGlue {

	private static final int[] SEEK_SPEEDS = {2, 4, 8, 16};
	private static final int DEFAULT_UPDATE_PERIOD = 500;
	private static final int UPDATE_PERIOD = 16;
	private static final String TAG = PlaybackHelper.class.getSimpleName();
	private final Context mContext;
	private final ExoOverlayFragment mFragment;
	private BitmapDrawable mDrawable = null;
	private Room room;
	private Stream stream;
	private Event event;
	private Recording recording;
	private Runnable mUpdateProgressRunnable;
	private Handler mHandler = new Handler();

	public PlaybackHelper(Context context, ExoOverlayFragment fragment, Event event, Recording recording){
		super(context, fragment,SEEK_SPEEDS);
		this.mContext = context;
		this.mFragment = fragment;
		this.event = event;
		this.recording = recording;

		Observable.fromCallable(() ->
			new BitmapDrawable(
					mContext.getResources(),
					Glide.with(getContext())
							.load(event.getThumbUrl())
							.asBitmap()
							.into(-1, -1)
							.get()))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.doOnError(Throwable::printStackTrace)
				.subscribe(bitmapDrawable -> mDrawable = bitmapDrawable);
	}

	public PlaybackHelper(Context context, ExoOverlayFragment fragment, Room room, Stream stream ){
		super(context, fragment,SEEK_SPEEDS);
		this.mContext = context;
		this.mFragment = fragment;
		this.room = room;
		this.stream = stream;
	}

	@Override
	public boolean hasValidMedia() {
		return mediaIsRecording()||mediaIsStream();
	}

	@Override
	public boolean isMediaPlaying() {
		return mFragment.isMediaPlaying();
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
	public int getUpdatePeriod() {
		View view = mFragment.getView();
		int totalTime = getControlsRow().getTotalTime();
		if (view == null || totalTime <= 0 || view.getWidth() == 0) {
			return DEFAULT_UPDATE_PERIOD;
		}
		return Math.max(UPDATE_PERIOD, totalTime / view.getWidth());
	}

	@Override
	public void updateProgress() {
		if (mUpdateProgressRunnable == null) {
			mUpdateProgressRunnable = new Runnable() {
				@Override
				public void run() {
					int totalTime = getControlsRow().getTotalTime();
					int currentTime = getCurrentPosition();
					getControlsRow().setCurrentTime(currentTime);

					int progress = mFragment.getCurrentPosition();
					getControlsRow().setBufferedProgress(progress);

					if (totalTime > 0 && totalTime <= currentTime) {
						stopProgressAnimation();
					} else {
						updateProgress();
					}
				}
			};
		}

		mHandler.postDelayed(mUpdateProgressRunnable, getUpdatePeriod());
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
		return mFragment.getCurrentPosition();
	}

	private boolean mediaIsStream() {
		return (room != null && stream != null);
	}
	private boolean mediaIsRecording(){
		return (event != null && recording != null);
	}


}
