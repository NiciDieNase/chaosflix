package de.nicidienase.chaosflix.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.PlaybackFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v17.leanback.widget.Row;

import de.nicidienase.chaosflix.PlaybackHelper;
import de.nicidienase.chaosflix.activities.DetailsActivity;
import de.nicidienase.chaosflix.entities.recording.Event;
import de.nicidienase.chaosflix.entities.recording.Recording;

/**
 * Created by felix on 26.03.17.
 */

public class ExoOverlayFragment extends android.support.v17.leanback.app.PlaybackOverlayFragment {

	private Recording mSelectedRecording;
	private Event mSelectedEvent;
	private PlaybackHelper mHelper;
	private PlaybackControlListener mCallback;

	public interface PlaybackControlListener {
		void play();
		void pause();
		void playPause();
		void setVideoSource(String source);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getActivity()
				.getIntent();
		mSelectedEvent = intent.getParcelableExtra(DetailsActivity.EVENT);
		mSelectedRecording = intent.getParcelableExtra(DetailsActivity.RECORDING);

		setBackgroundType(PlaybackFragment.BG_LIGHT);
		setFadingEnabled(false);

		mHelper = new PlaybackHelper(getActivity(),this,mSelectedEvent,mSelectedRecording);

		ArrayObjectAdapter rowsAdapter = setupRows();
//		rowsAdapter.add(getRelatedItems());
		setAdapter(rowsAdapter);

		mCallback.setVideoSource(mSelectedRecording.getUrl());
	}

	private ArrayObjectAdapter setupRows() {
		ClassPresenterSelector ps = new ClassPresenterSelector();
		PlaybackControlsRowPresenter playbackControlsRowPresenter;
		playbackControlsRowPresenter = mHelper.getControlsRowPresenter();
		ps.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
		ps.addClassPresenter(ListRow.class, new ListRowPresenter());
		return new ArrayObjectAdapter(ps);
	}

	private Row getRelatedItems() {
//		ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
//		TODO Add related items
//		HeaderItem header = new HeaderItem(0, getString(R.string.related_movies));
//		return new ListRow(header, listRowAdapter);
		return null;
	}

	public boolean isMediaPlaying() {
		return false;
	}

	public int getCurrentPosition() {
		return 0;
	}

	@Override
	public void onAttach(Activity context) {
		super.onAttach(context);
		if(context instanceof PlaybackControlListener){
			mCallback = (PlaybackControlListener) context;
		} else {
			throw(new RuntimeException("Activity must implement PlaybackControlListener"));
		}
	}
}
