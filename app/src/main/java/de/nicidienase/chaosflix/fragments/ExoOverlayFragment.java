package de.nicidienase.chaosflix.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v17.leanback.app.PlaybackFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ControlButtonPresenterSelector;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.widget.Toast;

import de.nicidienase.chaosflix.activities.AbstractServiceConnectedAcitivty;
import de.nicidienase.chaosflix.activities.DetailsActivity;
import de.nicidienase.chaosflix.entities.recording.Event;
import de.nicidienase.chaosflix.entities.recording.Recording;

/**
 * Created by felix on 26.03.17.
 */

public class ExoOverlayFragment extends PlaybackFragment {

	private static final boolean HIDE_MORE_ACTIONS = false;

	private Recording mSelectedRecording;
	private Event mSelectedEvent;
	private ArrayObjectAdapter mRowsAdapter;
	private PlaybackControlsRow mPlaybackControlsRow;
	private ArrayObjectAdapter mPrimaryActionsAdapter;
	private ArrayObjectAdapter mSecondaryActionsAdapter;
	private PlaybackControlsRow.PlayPauseAction mPlayPauseAction;
	private PlaybackControlsRow.FastForwardAction mFastForwardAction;
	private PlaybackControlsRow.RewindAction mRewindAction;

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

		setBackgroundType(PlaybackFragment.BG_LIGHT);
		setFadingEnabled(false);

		setupRows();
		addPlaybackControlsRow();
		addOtherRows();

		setAdapter(mRowsAdapter);

	}

		private void setupRows() {

		ClassPresenterSelector ps = new ClassPresenterSelector();

		PlaybackControlsRowPresenter playbackControlsRowPresenter;
		playbackControlsRowPresenter = new PlaybackControlsRowPresenter(
					new PlaybackOverlayFragment.DescriptionPresenter());
		playbackControlsRowPresenter.setOnActionClickedListener(new PlaybackControlClickedListener());
		playbackControlsRowPresenter.setSecondaryActionsHidden(HIDE_MORE_ACTIONS);

		ps.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
		ps.addClassPresenter(ListRow.class, new ListRowPresenter());
		mRowsAdapter = new ArrayObjectAdapter(ps);




	}

	private void addOtherRows() {
//		ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
//		for (Movie movie : mItems) {
//			listRowAdapter.add(movie);
//		}
//		HeaderItem header = new HeaderItem(0, getString(R.string.related_movies));
//		mRowsAdapter.add(new ListRow(header, listRowAdapter));
	}

		private void addPlaybackControlsRow() {
		mPlaybackControlsRow = new PlaybackControlsRow(mSelectedEvent);
		mRowsAdapter.add(mPlaybackControlsRow);

//		updatePlaybackRow(mCurrentItem);

		ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
		mPrimaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
		mSecondaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
		mPlaybackControlsRow.setPrimaryActionsAdapter(mPrimaryActionsAdapter);
		mPlaybackControlsRow.setSecondaryActionsAdapter(mSecondaryActionsAdapter);

		mPlayPauseAction = new PlaybackControlsRow.PlayPauseAction(getActivity());
//		mRepeatAction = new RepeatAction(getActivity());
		mFastForwardAction = new PlaybackControlsRow.FastForwardAction(getActivity());
		mRewindAction = new PlaybackControlsRow.RewindAction(getActivity());

		mPrimaryActionsAdapter.add(new PlaybackControlsRow.RewindAction(getActivity()));
		mPrimaryActionsAdapter.add(mPlayPauseAction);
		mPrimaryActionsAdapter.add(new PlaybackControlsRow.FastForwardAction(getActivity()));

//		mSecondaryActionsAdapter.add(mRepeatAction);
		// The place to toggle audiotracks, subtitles, etc..
	}

		private class PlaybackControlClickedListener implements OnActionClickedListener {
		public void onActionClicked(Action action) {
			if (action.getId() == mPlayPauseAction.getId()) {
				// TODO play/pause
				//(mPlayPauseAction.getIndex() == PlaybackControlsRow.PlayPauseAction.PLAY)
			} else if (action.getId() == mFastForwardAction.getId()) {
				// TODO fast forward
			} else if (action.getId() == mRewindAction.getId()) {
				// TODO rewind
			}
		}
	}
}
