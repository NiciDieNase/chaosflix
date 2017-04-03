/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package de.nicidienase.chaosflix.fragments;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ControlButtonPresenterSelector;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRow.FastForwardAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.PlayPauseAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.RewindAction;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.HashMap;

import de.nicidienase.chaosflix.activities.AbstractServiceConnectedAcitivty;
import de.nicidienase.chaosflix.activities.DetailsActivity;
import de.nicidienase.chaosflix.entities.recording.Event;
import de.nicidienase.chaosflix.entities.recording.Recording;

/*
 * Class for video playback with media control
 */
public class PlaybackOverlayFragment extends android.support.v17.leanback.app.PlaybackOverlayFragment {
	private static final String TAG = "PlaybackControlsFragmnt";

	private static final boolean SHOW_DETAIL = true;
	private static final boolean HIDE_MORE_ACTIONS = false;
	private static final boolean SHOW_IMAGE = true;
	private static final int BACKGROUND_TYPE = PlaybackOverlayFragment.BG_LIGHT;
	private static final int CARD_WIDTH = 200;
	private static final int CARD_HEIGHT = 240;
	private static final int DEFAULT_UPDATE_PERIOD = 1000;
	private static final int UPDATE_PERIOD = 16;
	private static final int SIMULATED_BUFFERED_TIME = 10000;

	private ArrayObjectAdapter mRowsAdapter;
	private ArrayObjectAdapter mPrimaryActionsAdapter;
	private ArrayObjectAdapter mSecondaryActionsAdapter;

	private PlayPauseAction mPlayPauseAction;
	private FastForwardAction mFastForwardAction;
	private RewindAction mRewindAction;

	private PlaybackControlsRow mPlaybackControlsRow;
	private Handler mHandler;
	private Runnable mRunnable;
	private Recording mSelectedRecording;
	private Event mSelectedEvent;

	private OnPlayPauseClickedListener mCallback;
//	private MediaApiService mMediaApiService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getActivity()
				.getIntent();
		mSelectedEvent = intent.getParcelableExtra(DetailsActivity.EVENT);
		final int mRecordingID = (int) intent.getLongExtra(DetailsActivity.RECORDING, 0);

		((AbstractServiceConnectedAcitivty)getActivity()).getmApiServiceObservable()
			.subscribe(mediaApiService -> {
//				mMediaApiService = mediaApiService;

				mediaApiService.getEvent(mSelectedEvent.getApiID())
					.subscribe(event -> {
						for(Recording r : event.getRecordings()){
							if(r.getApiID() == mRecordingID){
								mSelectedRecording = r;
							}
						}
					});
			});

		mHandler = new Handler();

		setBackgroundType(BACKGROUND_TYPE);
		setFadingEnabled(false);

		setupRows();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnPlayPauseClickedListener) {
			mCallback = (OnPlayPauseClickedListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnPlayPauseClickedListener");
		}
	}

	private void setupRows() {

		ClassPresenterSelector ps = new ClassPresenterSelector();

		PlaybackControlsRowPresenter playbackControlsRowPresenter;
		if (SHOW_DETAIL) {
			playbackControlsRowPresenter = new PlaybackControlsRowPresenter(
					new DescriptionPresenter());
		} else {
			playbackControlsRowPresenter = new PlaybackControlsRowPresenter();
		}
		playbackControlsRowPresenter.setOnActionClickedListener(new PlaybackControlClickedListener());
		playbackControlsRowPresenter.setSecondaryActionsHidden(HIDE_MORE_ACTIONS);

		ps.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
		ps.addClassPresenter(ListRow.class, new ListRowPresenter());
		mRowsAdapter = new ArrayObjectAdapter(ps);

		addPlaybackControlsRow();
		addOtherRows();

		setAdapter(mRowsAdapter);
	}

	public void togglePlayback(boolean playPause) {
		if (playPause) {
			startProgressAutomation();
			setFadingEnabled(true);
			mCallback.onFragmentPlayPause(mSelectedEvent, mSelectedRecording,
					mPlaybackControlsRow.getCurrentTime(), true);
			mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlayPauseAction.PAUSE));
		} else {
			stopProgressAutomation();
			setFadingEnabled(false);
			mCallback.onFragmentPlayPause(mSelectedEvent, mSelectedRecording,
					mPlaybackControlsRow.getCurrentTime(), false);
			mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlayPauseAction.PLAY));
		}
		notifyChanged(mPlayPauseAction);
	}

	private int getDuration() {
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		mmr.setDataSource(mSelectedRecording.getRecordingUrl(), new HashMap<String, String>());
		String time = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		long duration = Long.parseLong(time);
		return (int) duration;
	}

	private void addPlaybackControlsRow() {
		if (SHOW_DETAIL) {
			mPlaybackControlsRow = new PlaybackControlsRow(mSelectedEvent);
		} else {
			mPlaybackControlsRow = new PlaybackControlsRow();
		}
		mRowsAdapter.add(mPlaybackControlsRow);

//		updatePlaybackRow(mCurrentItem);
		updateVideoImage(mSelectedEvent.getThumbUrl());

		ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
		mPrimaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
		mSecondaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
		mPlaybackControlsRow.setPrimaryActionsAdapter(mPrimaryActionsAdapter);
		mPlaybackControlsRow.setSecondaryActionsAdapter(mSecondaryActionsAdapter);

		mPlayPauseAction = new PlayPauseAction(getActivity());
//		mRepeatAction = new RepeatAction(getActivity());
		mFastForwardAction = new PlaybackControlsRow.FastForwardAction(getActivity());
		mRewindAction = new PlaybackControlsRow.RewindAction(getActivity());

		mPrimaryActionsAdapter.add(new PlaybackControlsRow.RewindAction(getActivity()));
		mPrimaryActionsAdapter.add(mPlayPauseAction);
		mPrimaryActionsAdapter.add(new PlaybackControlsRow.FastForwardAction(getActivity()));

//		mSecondaryActionsAdapter.add(mRepeatAction);
	}

	private void notifyChanged(Action action) {
		ArrayObjectAdapter adapter = mPrimaryActionsAdapter;
		if (adapter.indexOf(action) >= 0) {
			adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
			return;
		}
		adapter = mSecondaryActionsAdapter;
		if (adapter.indexOf(action) >= 0) {
			adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
			return;
		}
	}

	private void updatePlaybackRow(Event event) {
		if (mPlaybackControlsRow.getItem() != null) {
			Event item = (Event) mPlaybackControlsRow.getItem();
			item.setTitle(mSelectedEvent.getTitle());
			item.setPersons(mSelectedEvent.getPersons());
		}
		if (SHOW_IMAGE) {
			updateVideoImage(mSelectedEvent.getThumbUrl());
		}
		mRowsAdapter.notifyArrayItemRangeChanged(0, 1);
		mPlaybackControlsRow.setTotalTime(getDuration());
		mPlaybackControlsRow.setCurrentTime(0);
		mPlaybackControlsRow.setBufferedProgress(0);
	}

	private void addOtherRows() {
//		ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
//		for (Movie movie : mItems) {
//			listRowAdapter.add(movie);
//		}
//		HeaderItem header = new HeaderItem(0, getString(R.string.related_movies));
//		mRowsAdapter.add(new ListRow(header, listRowAdapter));
	}

	private int getUpdatePeriod() {
		if (getView() == null || mPlaybackControlsRow.getTotalTime() <= 0) {
			return DEFAULT_UPDATE_PERIOD;
		}
		return Math.max(UPDATE_PERIOD, mPlaybackControlsRow.getTotalTime() / getView().getWidth());
	}

	private void startProgressAutomation() {
		mRunnable = new Runnable() {
			@Override
			public void run() {
				int updatePeriod = getUpdatePeriod();
				int currentTime = mPlaybackControlsRow.getCurrentTime() + updatePeriod;
				int totalTime = mPlaybackControlsRow.getTotalTime();
				mPlaybackControlsRow.setCurrentTime(currentTime);
				mPlaybackControlsRow.setBufferedProgress(currentTime + SIMULATED_BUFFERED_TIME);

				if (totalTime > 0 && totalTime <= currentTime) {
					next();
				}
				mHandler.postDelayed(this, updatePeriod);
			}
		};
		mHandler.postDelayed(mRunnable, getUpdatePeriod());
	}

	private void next() {
//		if (++mCurrentItem >= mItems.size()) {
//			mCurrentItem = 0;
//		}
//
//		if (mPlayPauseAction.getIndex() == PlayPauseAction.PLAY) {
//			mCallback.onFragmentPlayPause(mItems.get(mCurrentItem), 0, false);
//		} else {
//			mCallback.onFragmentPlayPause(mItems.get(mCurrentItem), 0, true);
//		}
//		updatePlaybackRow(mCurrentItem);
	}

	private void prev() {
//		if (--mCurrentItem < 0) {
//			mCurrentItem = mItems.size() - 1;
//		}
//		if (mPlayPauseAction.getIndex() == PlayPauseAction.PLAY) {
//			mCallback.onFragmentPlayPause(mItems.get(mCurrentItem), 0, false);
//		} else {
//			mCallback.onFragmentPlayPause(mItems.get(mCurrentItem), 0, true);
//		}
//		updatePlaybackRow(mCurrentItem);
	}

	private void stopProgressAutomation() {
		if (mHandler != null && mRunnable != null) {
			mHandler.removeCallbacks(mRunnable);
		}
	}

	@Override
	public void onStop() {
		stopProgressAutomation();
		super.onStop();
	}

	protected void updateVideoImage(String uri) {
		Glide.with(getActivity())
				.load(uri)
				.centerCrop()
				.into(new SimpleTarget<GlideDrawable>(CARD_WIDTH, CARD_HEIGHT) {
					@Override
					public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
						mPlaybackControlsRow.setImageDrawable(resource);
						mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());
					}
				});
	}

	// Container Activity must implement this interface
	public interface OnPlayPauseClickedListener {
		void onFragmentPlayPause(Event event, Recording recording, int position, Boolean playPause);
	}

	static class DescriptionPresenter extends AbstractDetailsDescriptionPresenter {
		@Override
		protected void onBindDescription(ViewHolder viewHolder, Object item) {
			viewHolder.getTitle().setText(((Event) item).getTitle());
			viewHolder.getSubtitle().setText(android.text.TextUtils.join(", ",((Event) item).getPersons()));
		}
	}

	private class PlaybackControlClickedListener implements OnActionClickedListener {
		public void onActionClicked(Action action) {
			if (action.getId() == mPlayPauseAction.getId()) {
				togglePlayback(mPlayPauseAction.getIndex() == PlayPauseAction.PLAY);
			} else if (action.getId() == mFastForwardAction.getId()) {
				Toast.makeText(getActivity(), "TODO: Fast Forward", Toast.LENGTH_SHORT).show();
			} else if (action.getId() == mRewindAction.getId()) {
				Toast.makeText(getActivity(), "TODO: Rewind", Toast.LENGTH_SHORT).show();
			}
			if (action instanceof PlaybackControlsRow.MultiAction) {
				((PlaybackControlsRow.MultiAction) action).nextIndex();
				notifyChanged(action);
			}
		}
	}
}
