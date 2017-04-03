package de.nicidienase.chaosflix.fragments;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.v17.leanback.app.PlaybackFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import de.nicidienase.chaosflix.CardPresenter;
import de.nicidienase.chaosflix.PlaybackHelper;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.activities.DetailsActivity;
import de.nicidienase.chaosflix.entities.recording.Event;
import de.nicidienase.chaosflix.entities.recording.Recording;
import de.nicidienase.chaosflix.entities.streaming.Room;
import de.nicidienase.chaosflix.entities.streaming.StreamUrl;

import static android.support.v17.leanback.app.PlaybackControlSupportGlue.ACTION_FAST_FORWARD;
import static android.support.v17.leanback.app.PlaybackControlSupportGlue.ACTION_PLAY_PAUSE;
import static android.support.v17.leanback.app.PlaybackControlSupportGlue.ACTION_REWIND;
import static android.support.v17.leanback.app.PlaybackControlSupportGlue.ACTION_SKIP_TO_NEXT;
import static android.support.v17.leanback.app.PlaybackControlSupportGlue.ACTION_SKIP_TO_PREVIOUS;
import static android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS;
import static android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS;

/**
 * Created by felix on 26.03.17.
 */

public class ExoOverlayFragment extends PlaybackFragment{

	private static final String TAG = ExoOverlayFragment.class.getSimpleName();

	private Recording mSelectedRecording;
	private Event mSelectedEvent;

	private Room mSelectedRoom;
	private PlaybackHelper mHelper;
	private PlaybackControlListener mCallback;
	private ArrayObjectAdapter mRowsAdapter;
	private MediaSession mSession;
	private boolean mHasAudioFocus;
	private AudioManager mAudioManager;
	private MediaController mMediaControler;
	private int eventType;
	private StreamUrl mSelectedStream;

	private PlaybackControlsRow.PlayPauseAction mPlayPauseAction;
	private PlaybackControlsRow.FastForwardAction mFastForwardAction;
	private PlaybackControlsRow.RewindAction mRewindAction;
	private PlaybackControlsRow.SkipNextAction mSkipNextAction;
	private PlaybackControlsRow.SkipPreviousAction mSkipPreviousAction;
	private MediaController.Callback mMediaControllerCallback;

	public interface PlaybackControlListener {
		void play();
		void pause();
		void playPause();
		void setVideoSource(String source);
		void skipForward(int sec);
		void skipBackward(int sec);
		void seekTo(long sec);
		boolean isMediaPlaying();
		long getCurrentPosition();
		void releasePlayer();
		long getPosition();
		long getBufferedPosition();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"OnCreate");

		Intent intent = getActivity()
				.getIntent();
		eventType = intent.getIntExtra(DetailsActivity.TYPE, -1);
		if(eventType == DetailsActivity.TYPE_RECORDING){
			mSelectedEvent = intent.getParcelableExtra(DetailsActivity.EVENT);
			mSelectedRecording = intent.getParcelableExtra(DetailsActivity.RECORDING);
			mHelper = new PlaybackHelper(getActivity(),this,mSelectedEvent,mSelectedRecording);
		} else if(eventType == DetailsActivity.TYPE_STREAM){
			mSelectedRoom = intent.getParcelableExtra(DetailsActivity.ROOM);
			mSelectedStream = intent.getParcelableExtra(DetailsActivity.STREAM_URL);
			mHelper = new PlaybackHelper(getActivity(),this,mSelectedRoom,mSelectedStream);
		} else {
			Log.d(TAG,"No Media found, finishing");
			getActivity().finish();
		}
		mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

		setBackgroundType(PlaybackFragment.BG_LIGHT);
		setFadingEnabled(false);
	}

	@Override
	public synchronized void onStart() {
		super.onStart();
		Log.d(TAG,"OnStart");

		PlaybackControlsRowPresenter playbackControlsRowPresenter = mHelper.createControlsRowAndPresenter();
		PlaybackControlsRow controlsRow = mHelper.getControlsRow();
		mMediaControllerCallback = mHelper.createMediaControllerCallback();
		mMediaControler = getActivity().getMediaController();
		mMediaControler.registerCallback(mMediaControllerCallback);
		ClassPresenterSelector ps = new ClassPresenterSelector();
		ps.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
		ps.addClassPresenter(ListRow.class, new ListRowPresenter());
		mRowsAdapter = new ArrayObjectAdapter(ps);
		mRowsAdapter.add(controlsRow);
//		mRowsAdapter.add(getRelatedItems());
		setAdapter(mRowsAdapter);

		if(mCallback != null && eventType == DetailsActivity.TYPE_STREAM){
			mCallback.setVideoSource(mSelectedStream.getUrl());
		} else if(mCallback != null && eventType == DetailsActivity.TYPE_RECORDING){
			mCallback.setVideoSource(mSelectedRecording.getRecordingUrl());
		} else {
			Log.d(TAG,"Callback not set or not event/stream");
		}
	}

	private Row getRelatedItems() {
		ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
//		TODO Add related items
		HeaderItem header = new HeaderItem(0, getString(R.string.related_talks));
		return new ListRow(header, listRowAdapter);
	}

	public boolean isMediaPlaying() {
		if(mCallback != null){
			return mCallback.isMediaPlaying();
		}
		return false;
	}

	public int getCurrentPosition() {
		if(mCallback != null){
			return (int) mCallback.getCurrentPosition();
		}
		return 0;
	}

	public long getCurrentBufferedPosition(){
		if(mCallback != null){
			return mCallback.getBufferedPosition();
		}
		return 0;
	}

	@Override
	public void onStop() {
		super.onStop();
		mSession.release();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mSession.release();
		mCallback.releasePlayer();
	}

	@SuppressWarnings("WrongConstant")
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		Log.d(TAG,"OnAttach");
		if(context instanceof PlaybackControlListener){
			mCallback = (PlaybackControlListener) context;
		} else {
			throw(new RuntimeException("Activity must implement PlaybackControlListener"));
		}

		if(mSession == null){
			mSession = new MediaSession(getActivity(),"chaosflix");
			mSession.setCallback(new ChaosflixSessionCallback());
			mSession.setFlags(FLAG_HANDLES_MEDIA_BUTTONS| FLAG_HANDLES_TRANSPORT_CONTROLS);
			mSession.setActive(true);

			PlaybackState state = new PlaybackState.Builder()
					.setActions(getAvailableActions())
					.setState(PlaybackState.STATE_STOPPED, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0)
					.build();
			mSession.setPlaybackState(state);

			getActivity().setMediaController(
					new MediaController(getActivity(),mSession.getSessionToken()));
		}
	}

	private void setPlaybackState(int state){
		int currentPosition = getCurrentPosition();

		PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
				.setActions(getAvailableActions());
		stateBuilder.setState(state,currentPosition,1.0f);
		mSession.setPlaybackState(stateBuilder.build());
	}

	private long getAvailableActions() {
		long actions = PlaybackState.ACTION_PLAY |
				PlaybackState.ACTION_SKIP_TO_NEXT |
				PlaybackState.ACTION_SKIP_TO_PREVIOUS |
				PlaybackState.ACTION_FAST_FORWARD |
				PlaybackState.ACTION_REWIND |
				PlaybackState.ACTION_PAUSE;
		return actions;
	}

	private class ChaosflixSessionCallback extends MediaSession.Callback {
		@Override
		public void onPlay() {
			mCallback.play();
		}

		@Override
		public void onPause() {
			mCallback.pause();
		}

		@Override
		public void onFastForward() {
			mCallback.skipForward(30);
		}

		@Override
		public void onRewind() {
			mCallback.skipBackward(30);
		}

		@Override
		public void onSkipToNext() {
			mCallback.skipForward(5*60);
		}

		@Override
		public void onSkipToPrevious() {
			mCallback.skipBackward(5*60);
		}

		@Override
		public void onSeekTo(long pos) {
			mCallback.seekTo(pos);
		}
	}

	private class ChaosflixActionClickListener implements OnActionClickedListener {

		@Override
		public void onActionClicked(Action action) {
			if(action.getId() == mPlayPauseAction.getId()){
				if(mPlayPauseAction.getIndex() == PlaybackControlsRow.PlayPauseAction.PLAY){
					mCallback.play();
				} else if(mPlayPauseAction.getIndex() == PlaybackControlsRow.PlayPauseAction.PAUSE){
					mCallback.pause();
				}
			}
		}
	}
}
