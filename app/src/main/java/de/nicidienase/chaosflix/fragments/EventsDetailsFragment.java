package de.nicidienase.chaosflix.fragments;

import android.os.Bundle;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;

import java.util.List;

import de.nicidienase.chaosflix.CardPresenter;
import de.nicidienase.chaosflix.EventDetailsDescriptionPresenter;
import de.nicidienase.chaosflix.activities.DetailsActivity;
import de.nicidienase.chaosflix.entities.Conference;
import de.nicidienase.chaosflix.entities.Event;
import de.nicidienase.chaosflix.entities.Recording;
import de.nicidienase.chaosflix.network.MediaCCCClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by felix on 18.03.17.
 */

public class EventsDetailsFragment extends DetailsFragment {
	private static final String TAG = EventsDetailsFragment.class.getSimpleName();
	private Event mSelectedEvent;
	private ArrayObjectAdapter mRowsAdapter;

	private MediaCCCClient client = new MediaCCCClient();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSelectedEvent = getActivity().getIntent()
				.getParcelableExtra(DetailsActivity.EVENT);
		client.getEvent(mSelectedEvent.getApiID()).enqueue(new Callback<Event>() {
			@Override
			public void onResponse(Call<Event> call, Response<Event> response) {
				mSelectedEvent = response.body();
			}

			@Override
			public void onFailure(Call<Event> call, Throwable t) {

			}
		});

		// Create Adapter
		ClassPresenterSelector selector = new ClassPresenterSelector();

		FullWidthDetailsOverviewRowPresenter rowPresenter =
				new FullWidthDetailsOverviewRowPresenter(
						new EventDetailsDescriptionPresenter());
		// Todo add transition-animation
		selector.addClassPresenter(DetailsOverviewRow.class, rowPresenter);
		selector.addClassPresenter(ListRow.class, new ListRowPresenter());
		mRowsAdapter = new ArrayObjectAdapter(selector);

		// Add playback options
		DetailsOverviewRow row = new DetailsOverviewRow(mSelectedEvent);
		List<Recording> recordings = mSelectedEvent.getRecordings();
		ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter();
		for(int i = 0; i < recordings.size(); i++){
			if(recordings.get(i).getMimeType().startsWith("video/") && !recordings.get(i).getLanguage().contains("-")){
				String quality = recordings.get(i).isHighQuality() ? "HD" : "SD";
//				int id = recordings.get(i).getLanguage().equals(mSelectedEvent.getOriginalLanguage()) ? 0 : 1;
				arrayObjectAdapter.add(new Action(i,quality,recordings.get(i).getLanguage()));
//				row.addAction(new Action(i,quality,recordings.get(i).getLanguage()));
			}
		}
		row.setActionsAdapter(arrayObjectAdapter);
		mRowsAdapter.add(row);

		// Add related media
		client.getConference(mSelectedEvent.getApiID()).enqueue(new Callback<Conference>() {
			@Override
			public void onResponse(Call<Conference> call, Response<Conference> response) {
				Conference conference = response.body();
				List<Event> events = conference.getEventsByTags().get(mSelectedEvent.getTags().get(0));
				CardPresenter cardPresenter = new CardPresenter();

				ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
				listRowAdapter.addAll(0, events);
				HeaderItem header = new HeaderItem("Related Talks");
				mRowsAdapter.add(new ListRow(header, listRowAdapter));
			}

			@Override
			public void onFailure(Call<Conference> call, Throwable t) {

			}
		});

		setAdapter(mRowsAdapter);
	}
}
