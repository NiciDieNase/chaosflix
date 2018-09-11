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
import android.support.v17.leanback.app.PlaybackSupportFragment;
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

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentRecording;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamUrl;
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgress;
import de.nicidienase.chaosflix.leanback.CardPresenter;
import de.nicidienase.chaosflix.leanback.PlaybackHelper;
import de.nicidienase.chaosflix.leanback.activities.DetailsActivity;

import static android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS;
import static android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS;

/**
 * Created by felix on 26.03.17.
 */

public class OverlayFragment extends PlaybackSupportFragment {

	private static final String TAG = OverlayFragment.class.getSimpleName();
	private static final long RESUME_SKIP = 5; // seconds to skip back on resume
	public static final int MAX_REMAINING = 30;

	private PersistentRecording recording;
	private PersistentEvent event;
	private PlaybackProgress playbackProgress;

	private Room room;
	private PlaybackHelper helper;
	private PlaybackControlListener callback;
	private ArrayObjectAdapter rowsAdapter;
	private MediaSession mediaSession;
	private boolean hasAudioFocus;
	private boolean pauseTransient;
	private AudioManager audioManager;
	private MediaController mediaController;
	private int eventType;

	private StreamUrl mSelectedStream;

	private MediaController.Callback mediaControllerCallback;
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
							if (helper.isMediaPlaying()) {
								pause();
								pauseTransient = true;
							}
							break;
						case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
							callback.mute(true);
							break;
						case AudioManager.AUDIOFOCUS_GAIN:
						case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
						case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
							if (pauseTransient) {
								play();
							}
							callback.mute(false);
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
			event = intent.getParcelableExtra(DetailsActivity.Companion.getEVENT());
			recording = intent.getParcelableExtra(DetailsActivity.Companion.getRECORDING());
			helper = new PlaybackHelper(getActivity(), this, event, recording);

			List<PlaybackProgress> progressList = null;
//					= PlaybackProgress.find(PlaybackProgress.class, "event_id = ?", String.valueOf(event.getApiID()));
			if (progressList.size() > 0) {
				playbackProgress = progressList.get(0);
			}
		} else if (eventType == DetailsActivity.Companion.getTYPE_STREAM()) {
			room = intent.getParcelableExtra(DetailsActivity.Companion.getROOM());
			mSelectedStream = intent.getParcelableExtra(DetailsActivity.Companion.getSTREAM_URL());
			helper = new PlaybackHelper(getActivity(), this, room, mSelectedStream);
		} else {
			Log.d(TAG, "No Media found, finishing");
			getActivity().finish();
		}
		audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

		setBackgroundType(PlaybackFragment.BG_LIGHT);
		setFadingEnabled(false);
	}

	@Override
	public synchronized void onStart() {
		super.onStart();
		Log.d(TAG, "OnStart");

		PlaybackControlsRowPresenter playbackControlsRowPresenter = helper.createControlsRowAndPresenter();
		PlaybackControlsRow controlsRow = helper.getControlsRow();
		mediaControllerCallback = helper.createMediaControllerCallback();
		requestAudioFocus();
		mediaController = getActivity().getMediaController();
		if (mediaController != null) {
			mediaController.registerCallback(mediaControllerCallback);
		} else {
			Log.d(TAG, "MediaController is null");
			getActivity().finish();
		}
		ClassPresenterSelector ps = new ClassPresenterSelector();
		ps.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
		ps.addClassPresenter(ListRow.class, new ListRowPresenter());
		rowsAdapter = new ArrayObjectAdapter(ps);
		rowsAdapter.add(controlsRow);
		if (eventType == DetailsActivity.Companion.getTYPE_RECORDING()) {
//			if (event.getMetadata() != null && event.getMetadata().getRelated() != null) {
//				rowsAdapter.add(getRelatedItems());
//				setOnItemViewClickedListener(new ItemViewClickedListener(this));
//			}
		} else if (eventType == DetailsActivity.Companion.getTYPE_STREAM()) {
			// TODO add other streams as related events
		}
		setAdapter(rowsAdapter);

		if (callback != null && eventType == DetailsActivity.Companion.getTYPE_STREAM()) {
			callback.setVideoSource(mSelectedStream.getUrl());
		} else if (callback != null && eventType == DetailsActivity.Companion.getTYPE_RECORDING()) {
			callback.setVideoSource(recording.getRecordingUrl());
		} else {
			Log.d(TAG, "Callback not set or not event/stream");
		}
		requestAudioFocus();
		if (playbackProgress != null) {
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
					callback.seekTo(getProgress());
					play();
				})
				.create();
		alertDialog.show();
	}

	private long getProgress() {
		long progress = playbackProgress.getProgress() / 1000;
		if (progress >= event.getLength()) {
			return 0;
		} else {
			return Math.max(0, progress - RESUME_SKIP);
		}
	}

	private Row getRelatedItems() {
		ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
//		final Set<Long> related = event.getMetadata().getRelated().keySet();
//		mDisposables.add(((LeanbackBaseActivity) getActivity()).getApiServiceObservable()
//				.observeOn(AndroidSchedulers.mainThread())
//				.subscribe(
//						mediaApiService -> {
//							for (long id : related) {
//								mDisposables.add(mediaApiService.getEvent(id)
//										.observeOn(AndroidSchedulers.mainThread())
//										.subscribe(event -> listRowAdapter.add(event)));
//							}
//							listRowAdapter.notifyArrayItemRangeChanged(0, listRowAdapter.size());
//						}
//				)
//		);
		HeaderItem header = new HeaderItem(0, getString(R.string.related_talks));
		return new ListRow(header, listRowAdapter);
	}

	public boolean isMediaPlaying() {
		if (callback != null) {
			return callback.isMediaPlaying();
		}
		return false;
	}

	public int getCurrentPosition() {
		if (eventType == DetailsActivity.Companion.getTYPE_RECORDING()) {
			if (callback != null) {
				return (int) callback.getCurrentPosition();
			}
		}
		return 0;
	}

	private long getCurrentPositionLong() {
		if (eventType == DetailsActivity.Companion.getTYPE_RECORDING()) {
			if (callback != null) {
				return callback.getCurrentPosition();
			}
		}
		return 0;
	}

	public long getCurrentBufferedPosition() {
		if (callback != null) {
			return callback.getBufferedPosition();
		}
		return 0;
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mediaSession != null) {
			mediaSession.release();
		}
		abandonAudioFocus();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (event != null) {
			if (playbackProgress != null) {
				if ((event.getLength() - callback.getCurrentPosition() / 1000) > MAX_REMAINING) {
					playbackProgress.setProgress(callback.getCurrentPosition());
//					playbackProgress.save();
				} else {
//					playbackProgress.delete();
				}
			} else if ((event.getLength() - callback.getCurrentPosition() / 1000) > MAX_REMAINING) {
//				playbackProgress = new PlaybackProgress(event.getApiID(),
//						callback.getCurrentPosition(), recording.getApiID());
//				playbackProgress.save();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mediaSession != null) {
			mediaSession.release();
		}
		callback.releasePlayer();
//		helper.onStop();
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
			callback = (PlaybackControlListener) context;
		} else {
			throw (new RuntimeException("Activity must implement PlaybackControlListener"));
		}

		if (mediaSession == null) {
			mediaSession = new MediaSession(getActivity(), "chaosflix");
			mediaSession.setCallback(new ChaosflixSessionCallback());
			mediaSession.setFlags(FLAG_HANDLES_MEDIA_BUTTONS | FLAG_HANDLES_TRANSPORT_CONTROLS);
			mediaSession.setActive(true);

			setPlaybackState(PlaybackState.STATE_NONE);

			getActivity().setMediaController(
					new MediaController(getActivity(), mediaSession.getSessionToken()));
		}
	}

	@SuppressWarnings("WrongConstant")
	private void setPlaybackState(int state) {
		long currentPosition = getCurrentPositionLong();
		PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
				.setActions(getAvailableActions(state))
				.setState(PlaybackState.STATE_PLAYING, currentPosition, 0);

		if (mediaSession != null) {
			mediaSession.setPlaybackState(stateBuilder.build());
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
		if (hasAudioFocus) {
			return;
		}
		int result = audioManager.requestAudioFocus(mOnAudioFocusChangeListener,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			hasAudioFocus = true;
		} else {
			pause();
		}
	}

	private void abandonAudioFocus() {
		hasAudioFocus = false;
		audioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
	}

	private void play() {
		if (callback != null) {
			setPlaybackState(PlaybackState.STATE_PLAYING);
			setFadingEnabled(true);
			callback.play();
		}
	}

	private void pause() {
		if (callback != null) {
			setPlaybackState(PlaybackState.STATE_PAUSED);
			setFadingEnabled(false);
			callback.pause();
		}
	}

	private void rewind() {
		if (callback != null) {
			int prevState = getPlaybackState();
			setPlaybackState(PlaybackState.STATE_FAST_FORWARDING);
			callback.skipBackward(30);
			setPlaybackState(prevState);
		}
	}

	private void fastForward() {
		if (callback != null) {
			int prevState = getPlaybackState();
			setPlaybackState(PlaybackState.STATE_FAST_FORWARDING);
			callback.skipForward(30);
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
			callback.skipForward(5 * 60);
		}

		@Override
		public void onSkipToPrevious() {
			callback.skipBackward(5 * 60);
		}

		@Override
		public void onSeekTo(long pos) {
			callback.seekTo(pos);
		}
	}
}
