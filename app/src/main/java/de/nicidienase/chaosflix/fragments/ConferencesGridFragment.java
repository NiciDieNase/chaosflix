package de.nicidienase.chaosflix.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.util.Log;

import java.util.Collections;
import java.util.List;

import de.nicidienase.chaosflix.CardPresenter;
import de.nicidienase.chaosflix.ItemViewClickedListener;
import de.nicidienase.chaosflix.entities.Conference;
import de.nicidienase.chaosflix.entities.Conferences;
import de.nicidienase.chaosflix.network.MediaCCCClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by felix on 20.03.17.
 */

public class ConferencesGridFragment extends VerticalGridFragment {

	private static final int NUM_COLUMNS = 5;
	private static final String TAG = ConferencesGridFragment.class.getSimpleName();
	private final ArrayObjectAdapter mConferenceAdapter =
            new ArrayObjectAdapter(new CardPresenter());
	private MediaCCCClient mMediaCCCClient;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mMediaCCCClient = new MediaCCCClient();

		mMediaCCCClient.listConferences().enqueue(new Callback<Conferences>() {
			@Override
			public void onResponse(Call<Conferences> call, Response<Conferences> response) {
				List<Conference> conferences = response.body().getConferences();
				Collections.sort(conferences);
				Collections.reverse(conferences);
				mConferenceAdapter.addAll(0, conferences);
				setAdapter(mConferenceAdapter);
			}

			@Override
			public void onFailure(Call<Conferences> call, Throwable t) {
				Log.d(TAG,"Error loading conferences",t);
				t.printStackTrace();

			}
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
