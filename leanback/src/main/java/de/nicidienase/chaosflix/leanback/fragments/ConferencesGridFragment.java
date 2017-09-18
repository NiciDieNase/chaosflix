package de.nicidienase.chaosflix.leanback.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.VerticalGridPresenter;

import java.util.Collections;
import java.util.List;

import de.nicidienase.chaosflix.leanback.CardPresenter;
import de.nicidienase.chaosflix.leanback.ItemViewClickedListener;
import de.nicidienase.chaosflix.leanback.activities.LeanbackBaseActivity;
import de.nicidienase.chaosflix.common.entities.recording.Conference;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by felix on 20.03.17.
 */

public class ConferencesGridFragment extends VerticalGridFragment {

	private static final int NUM_COLUMNS = 5;
	private static final String TAG = ConferencesGridFragment.class.getSimpleName();
	private final ArrayObjectAdapter mConferenceAdapter =
			new ArrayObjectAdapter(new CardPresenter());

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((LeanbackBaseActivity) getActivity()).getApiServiceObservable()
				.subscribe(mediaApiService -> {
					mediaApiService.getConferences()
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(conferences -> {
								List<Conference> conferenceList = conferences.getConferences();
								Collections.sort(conferenceList);
								Collections.reverse(conferenceList);
								mConferenceAdapter.addAll(0, conferenceList);
								setAdapter(mConferenceAdapter);
							});
				});
		setTitle("Chaosflix");

		VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
		gridPresenter.setNumberOfColumns(NUM_COLUMNS);
		setGridPresenter(gridPresenter);

		// After 500ms, start the animation to transition the cards into view.
		new Handler().postDelayed(new Runnable() {
			public void run() {
				startEntranceTransition();
			}
		}, 500);

		setOnItemViewClickedListener(new ItemViewClickedListener(this));
	}

}
