package de.nicidienase.chaosflix.leanback.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.v17.leanback.app.PlaybackFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v17.leanback.widget.Row;
import android.util.Log;

import java.util.List;
import java.util.Set;

import de.nicidienase.chaosflix.common.entities.PlaybackProgress;
import de.nicidienase.chaosflix.leanback.CardPresenter;
import de.nicidienase.chaosflix.leanback.ItemViewClickedListener;
import de.nicidienase.chaosflix.leanback.PlaybackHelper;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.leanback.activities.LeanbackBaseActivity;
import de.nicidienase.chaosflix.leanback.activities.DetailsActivity;
import de.nicidienase.chaosflix.common.entities.recording.Event;
import de.nicidienase.chaosflix.common.entities.recording.Recording;
import de.nicidienase.chaosflix.common.entities.streaming.Room;
import de.nicidienase.chaosflix.common.entities.streaming.StreamUrl;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS;
import static android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS;

/**
 * Created by felix on 26.03.17.
 */

public class OverlayFragment extends PlaybackFragment {

	private static final String TAG = OverlayFragment.class.getSimpleName();
	private static final long RESUME_SKIP = 5; // seconds to skip back on resume
	public static final int MAX_REMAINING = 30;

	private Recording mSelectedRecording;
	private Event mSelectedEvent;
	private PlaybackProgress mPlaybackProgress;

	private Room mSelectedRoom;
	private PlaybackHelper mHelper;
	private PlaybackControlListener mCallback;
	private ArrayObjectAdapter mRowsAdapter;
	private MediaSession mSession;
	private boolean mHasAudioFocus;
	private boolean mPauseTransient;
	private AudioManager mAudioManager;
	private MediaController mMediaControler;
	private int eventType;

	private StreamUrl mSelectedStream;

	private MediaController.Callback mMediaControllerCallback;
	private CompositeDisposable mDisposables = new CompositeDisposable();
	private final AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener =
			new AudioManager.OnAudioFocusChangeListener() {
				@Override
				public void onAudioFocusChange(int focusChange) {
					switch (focusChange) {
						case AudioManager.AUDIOFOCUS_LOSS:
							abandonAudioFocus();
							pause();
							break;
						case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
							if (mHelper.isMediaPlaying()) {
								pause();
								mPauseTransient = true;
							}
							break;
						case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
							mCallback.mute(true);
							break;
						case AudioManager.AUDIOFOCUS_GAIN:
						case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
						case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
							if (mPauseTransient) {
								play();
							}
							mCallback.mute(false);
							break;
					}
				}
			};

	public interface PlaybackControlListener {
		void play();

		void pause();

		void playPause();

		void setVideoSource(String source);

		void skipForward(int sec);

		void skipBackward(int sec);

		void seekTo(long sec);

		boolean isMediaPlaying();

		long getLength();

		long getCurrentPosition();

		long getBufferedPosition();

		void releasePlayer();

		void mute(boolean state);

		void nextAudioStream();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "OnCreate");

		Intent intent = getActivity()
				.getIntent();
		eventType = intent.getIntExtra(DetailsActivity.Companion.getTYPE(), -1);
		if (eventType == DetailsActivity.Companion.getTYPE_RECORDING()) {
			mSelectedEvent = intent.getParcelableExtra(DetailsActivity.Companion.getEVENT());
			mSelectedRecording = intent.getParcelableExtra(DetailsActivity.Companion.getRECORDING());
			mHelper = new PlaybackHelper(getActivity(), this, mSelectedEvent, mSelectedRecording);

			List<PlaybackProgress> progressList = PlaybackProgress.find(PlaybackProgress.class, "event_id = ?", String.valueOf(mSelectedEvent.getApiID()));
			if (progressList.size() > 0) {
				mPlaybackProgress = progressList.get(0);
			}
		} else if (eventType == DetailsActivity.Companion.getTYPE_STREAM()) {
			mSelectedRoom = intent.getParcelableExtra(DetailsActivity.Companion.getROOM());
			mSelectedStream = intent.getParcelableExtra(DetailsActivity.Companion.getSTREAM_URL());
			mHelper = new PlaybackHelper(getActivity(), this, mSelectedRoom, mSelectedStream);
		} else {
			Log.d(TAG, "No Media found, finishing");
			getActivity().finish();
		}
		mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

		setBackgroundType(PlaybackFragment.BG_LIGHT);
		setFadingEnabled(false);
	}

	@Override
	public synchronized void onStart() {
		super.onStart();
		Log.d(TAG, "OnStart");

		PlaybackControlsRowPresenter playbackControlsRowPresenter = mHelper.createControlsRowAndPresenter();
		PlaybackControlsRow controlsRow = mHelper.getControlsRow();
		mMediaControllerCallback = mHelper.createMediaControllerCallback();
		requestAudioFocus();
		mMediaControler = getActivity().getMediaController();
		if (mMediaControler != null) {
			mMediaControler.registerCallback(mMediaControllerCallback);
		} else {
			Log.d(TAG, "MediaController is null");
			getActivity().finish();
		}
		ClassPresenterSelector ps = new ClassPresenterSelector();
		ps.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
		ps.addClassPresenter(ListRow.class, new ListRowPresenter());
		mRowsAdapter = new ArrayObjectAdapter(ps);
		mRowsAdapter.add(controlsRow);
		if (eventType == DetailsActivity.Companion.getTYPE_RECORDING()) {
			if (mSelectedEvent.getMetadata() != null && mSelectedEvent.getMetadata().getRelated() != null) {
				mRowsAdapter.add(getRelatedItems());
				setOnItemViewClickedListener(new ItemViewClickedListener(this));
			}
		} else if (eventType == DetailsActivity.Companion.getTYPE_STREAM()) {
			// TODO add other streams as related events
		}
		setAdapter(mRowsAdapter);

		if (mCallback != null && eventType == DetailsActivity.Companion.getTYPE_STREAM()) {
			mCallback.setVideoSource(mSelectedStream.getUrl());
		} else if (mCallback != null && eventType == DetailsActivity.Companion.getTYPE_RECORDING()) {
			mCallback.setVideoSource(mSelectedRecording.getRecordingUrl());
		} else {
			Log.d(TAG, "Callback not set or not event/stream");
		}
		requestAudioFocus();
		if (mPlaybackProgress != null) {
			showContinueOrRestartDialog();
		} else {
			play();
		}
	}

	private void showContinueOrRestartDialog() {
		AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
				.setMessage(R.string.resume_question)
				.setPositiveButton(R.string.start_again, (dialog, which) -> play())
				.setNegativeButton(R.string.resume, (dialog, which) -> {
					mCallback.seekTo(getProgress());
					play();
				})
				.create();
		alertDialog.show();
	}

	private long getProgress() {
		long progress = mPlaybackProgress.getProgress() / 1000;
		if (progress >= mSelectedEvent.getLength()) {
			return 0;
		} else {
			return Math.max(0, progress - RESUME_SKIP);
		}
	}

	private Row getRelatedItems() {
		ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
		final Set<Long> related = mSelectedEvent.getMetadata().getRelated().keySet();
		mDisposables.add(((LeanbackBaseActivity) getActivity()).getApiServiceObservable()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						mediaApiService -> {
							for (long id : related) {
								mDisposables.add(mediaApiService.getEvent(id)
										.observeOn(AndroidSchedulers.mainThread())
										.subscribe(event -> listRowAdapter.add(event)));
							}
							listRowAdapter.notifyArrayItemRangeChanged(0, listRowAdapter.size());
						}
				)
		);
		HeaderItem header = new HeaderItem(0, getString(R.string.related_talks));
		return new ListRow(header, listRowAdapter);
	}

	public boolean isMediaPlaying() {
		if (mCallback != null) {
			return mCallback.isMediaPlaying();
		}
		return false;
	}

	public int getCurrentPosition() {
		if (eventType == DetailsActivity.Companion.getTYPE_RECORDING()) {
			if (mCallback != null) {
				return (int) mCallback.getCurrentPosition();
			}
		}
		return 0;
	}

	private long getCurrentPositionLong() {
		if (eventType == DetailsActivity.Companion.getTYPE_RECORDING()) {
			if (mCallback != null) {
				return mCallback.getCurrentPosition();
			}
		}
		return 0;
	}

	public long getCurrentBufferedPosition() {
		if (mCallback != null) {
			return mCallback.getBufferedPosition();
		}
		return 0;
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mSession != null) {
			mSession.release();
		}
		mDisposables.dispose();
		abandonAudioFocus();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mSelectedEvent != null) {
			if (mPlaybackProgress != null) {
				if ((mSelectedEvent.getLength() - mCallback.getCurrentPosition() / 1000) > MAX_REMAINING) {
					mPlaybackProgress.setProgress(mCallback.getCurrentPosition());
					mPlaybackProgress.save();
				} else {
					mPlaybackProgress.delete();
				}
			} else if ((mSelectedEvent.getLength() - mCallback.getCurrentPosition() / 1000) > MAX_REMAINING) {
				mPlaybackProgress = new PlaybackProgress(mSelectedEvent.getApiID(),
						mCallback.getCurrentPosition(), mSelectedRecording.getApiID());
				mPlaybackProgress.save();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mSession != null) {
			mSession.release();
		}
		mCallback.releasePlayer();
		mHelper.onStop();
	}


	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		setupMediaSession(context);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		setupMediaSession(activity);
	}

	@SuppressWarnings("WrongConstant")
	private void setupMediaSession(Context context) {
		Log.d(TAG, "OnAttach");
		if (context instanceof PlaybackControlListener) {
			mCallback = (PlaybackControlListener) context;
		} else {
			throw (new RuntimeException("Activity must implement PlaybackControlListener"));
		}

		if (mSession == null) {
			mSession = new MediaSession(getActivity(), "chaosflix");
			mSession.setCallback(new ChaosflixSessionCallback());
			mSession.setFlags(FLAG_HANDLES_MEDIA_BUTTONS | FLAG_HANDLES_TRANSPORT_CONTROLS);
			mSession.setActive(true);

			setPlaybackState(PlaybackState.STATE_NONE);

			getActivity().setMediaController(
					new MediaController(getActivity(), mSession.getSessionToken()));
		}
	}

	@SuppressWarnings("WrongConstant")
	private void setPlaybackState(int state) {
		long currentPosition = getCurrentPositionLong();
		PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
				.setActions(getAvailableActions(state))
				.setState(PlaybackState.STATE_PLAYING, currentPosition, 0);

		if (mSession != null) {
			mSession.setPlaybackState(stateBuilder.build());
		}
	}

	private int getPlaybackState() {
		Activity activity = getActivity();

		if (activity != null) {
			PlaybackState state = activity.getMediaController().getPlaybackState();
			if (state != null) {
				return state.getState();
			} else {
				return PlaybackState.STATE_NONE;
			}
		}
		return PlaybackState.STATE_NONE;
	}

	private long getAvailableActions(int nextState) {
		long actions = PlaybackState.ACTION_PLAY |
				PlaybackState.ACTION_SKIP_TO_NEXT |
				PlaybackState.ACTION_SKIP_TO_PREVIOUS |
				PlaybackState.ACTION_FAST_FORWARD |
				PlaybackState.ACTION_REWIND |
				PlaybackState.ACTION_PAUSE;

		if (nextState == PlaybackState.STATE_PLAYING) {
			actions |= PlaybackState.ACTION_PAUSE;
		}

		return actions;
	}

	private void requestAudioFocus() {
		if (mHasAudioFocus) {
			return;
		}
		int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			mHasAudioFocus = true;
		} else {
			pause();
		}
	}

	private void abandonAudioFocus() {
		mHasAudioFocus = false;
		mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
	}

	private void play() {
		if (mCallback != null) {
			setPlaybackState(PlaybackState.STATE_PLAYING);
			setFadingEnabled(true);
			mCallback.play();
		}
	}

	private void pause() {
		if (mCallback != null) {
			setPlaybackState(PlaybackState.STATE_PAUSED);
			setFadingEnabled(false);
			mCallback.pause();
		}
	}

	private void rewind() {
		if (mCallback != null) {
			int prevState = getPlaybackState();
			setPlaybackState(PlaybackState.STATE_FAST_FORWARDING);
			mCallback.skipBackward(30);
			setPlaybackState(prevState);
		}
	}

	private void fastForward() {
		if (mCallback != null) {
			int prevState = getPlaybackState();
			setPlaybackState(PlaybackState.STATE_FAST_FORWARDING);
			mCallback.skipForward(30);
			setPlaybackState(prevState);
		}
	}

	private class ChaosflixSessionCallback extends MediaSession.Callback {
		@Override
		public void onPlay() {
			play();
		}

		@Override
		public void onPause() {
			pause();
		}

		@Override
		public void onFastForward() {
			fastForward();
		}

		@Override
		public void onRewind() {
			rewind();
		}

		@Override
		public void onSkipToNext() {
			mCallback.skipForward(5 * 60);
		}

		@Override
		public void onSkipToPrevious() {
			mCallback.skipBackward(5 * 60);
		}

		@Override
		public void onSeekTo(long pos) {
			mCallback.seekTo(pos);
		}
	}
}
