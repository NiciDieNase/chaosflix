package de.nicidienase.chaosflix.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.CursorObjectAdapter;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.support.v4.app.ActivityOptionsCompat;

import java.util.Collections;
import java.util.List;

import de.nicidienase.chaosflix.CardPresenter;
import de.nicidienase.chaosflix.activities.EventsActivity;
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

		setOnItemViewClickedListener(new ItemViewClickedListener());
	}

	private final class ItemViewClickedListener implements OnItemViewClickedListener{

		@Override
		public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
								  RowPresenter.ViewHolder rowViewHolder, Row row) {
			if(item instanceof Conference){
				Conference conference = (Conference) item;
				// Start EventsActivity for this conference
				Intent i = new Intent(getActivity(), EventsActivity.class);
				i.putExtra(EventsActivity.CONFERENCE,conference);
				i.putExtra(EventsActivity.CONFERENCE_ID,conference.getApiID());
//				Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
//						getActivity(),
//						((ImageCardView) itemViewHolder.view).getMainImageView(),
//						EventsActivity.SHARED_ELEMENT_NAME).toBundle();
				startActivity(i);
			}
		}
	}
}
