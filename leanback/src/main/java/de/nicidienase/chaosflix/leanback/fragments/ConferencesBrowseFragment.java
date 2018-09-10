package de.nicidienase.chaosflix.leanback.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.BrowseSupportFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.DiffCallback;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.SectionRow;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.nicidienase.chaosflix.BuildConfig;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentConference;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Group;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference;
import de.nicidienase.chaosflix.common.util.ConferenceUtil;
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel;
import de.nicidienase.chaosflix.leanback.CardPresenter;
import de.nicidienase.chaosflix.leanback.ItemViewClickedListener;
import de.nicidienase.chaosflix.R;

/**
 * Created by felix on 21.03.17.
 */

public class ConferencesBrowseFragment extends BrowseSupportFragment {

	public static final int LIVESTREAMS_INDEX = 0;
	public static final int RECOMENDATIONS_INDEX = 100;
	public static final int CONFERENCES_INDEX = 200;
	private static final String TAG = ConferencesBrowseFragment.class.getSimpleName();
	public static final int FRAGMENT = R.id.browse_fragment;
	private ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
	private ArrayObjectAdapter watchListAdapter;
	private ListRow watchlistRow;

	private SectionRow streamingSection;
	private SectionRow recomendationsSectionsRow;
	private SectionRow conferencesSection;

	private boolean mWatchlistVisible = false;
	private BrowseViewModel viewModel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getResources().getString(R.string.app_name));

//		setHeaderPresenterSelector(new PresenterSelector() {
//			@Override
//			public Presenter getPresenter(Object item) {
//				return new HeaderItemPresenter();
//			}
//		});

		viewModel = ViewModelProviders.of(this).get(BrowseViewModel.class);

		final BrowseErrorFragment errorFragment =
				BrowseErrorFragment.showErrorFragment(getFragmentManager(), FRAGMENT);
		CardPresenter cardPresenter = new CardPresenter();

		watchListAdapter = new ArrayObjectAdapter(cardPresenter);

		viewModel.getConferenceGroups().observe(this, conferenceGroups -> {
			Map<String, List<PersistentConference>> conferences = new HashMap<>(); // TODO get map!

			List<Row> rows = addRecordings(cardPresenter, conferences);
			rowsAdapter.addAll(CONFERENCES_INDEX, rows);
		});

		viewModel.getBookmarkedEvents().observe(this, (bookmarks) -> {
			if(bookmarks != null){
				watchListAdapter.setItems(bookmarks, new DiffCallback<PersistentEvent>() {
					@Override
					public boolean areItemsTheSame(@NonNull PersistentEvent oldItem, @NonNull PersistentEvent newItem) {
						return oldItem.getGuid().equals(newItem.getGuid());
					}

					@Override
					public boolean areContentsTheSame(@NonNull PersistentEvent oldItem, @NonNull PersistentEvent newItem) {
						return oldItem.getGuid().equals(newItem.getGuid());
					}
				});
				if(bookmarks.size() > 0){
					showWatchlist();
				} else {
					hideWatchlist();
				}
			}
		});

		viewModel.getLivestreams().observe(this, liveConferences -> {
			if(liveConferences != null){
				if (BuildConfig.DEBUG) {
					liveConferences.add(LiveConference.getDummyObject());
				}
				addStreams(cardPresenter, liveConferences);
				errorFragment.dismiss();
			}
		});

		conferencesSection = new SectionRow(getString(R.string.conferences));
		rowsAdapter.add(CONFERENCES_INDEX, conferencesSection);
		setOnItemViewClickedListener(new ItemViewClickedListener(this));
		setAdapter(rowsAdapter);
		setSelectedPosition(1);
	}

	private List<Row> addRecordings(CardPresenter cardPresenter, Map<String, List<PersistentConference>> conferences) {
		List<Row> result = new LinkedList<>();
		Set<String> keySet = conferences.keySet();
		for (String tag : ConferenceUtil.getOrderedConferencesList()) {
			if (keySet.contains(tag)) {
				ListRow row = getRow(conferences.get(tag), cardPresenter, tag, ConferenceUtil.getStringForTag(tag));
				result.add(row);
			}
		}
		for (String tag : keySet) {
			if (!ConferenceUtil.getOrderedConferencesList().contains(tag)) {
				result.add(getRow(conferences.get(tag), cardPresenter, tag, tag));
			}
		}
		return result;
	}

	private void addStreams(CardPresenter cardPresenter, List<LiveConference> liveConferences) {
		if (liveConferences.size() > 0) {
			HeaderItem streamingHeader = new HeaderItem(getString(R.string.livestreams));
			streamingSection = new SectionRow(streamingHeader);
			rowsAdapter.add(LIVESTREAMS_INDEX, streamingSection);
			for (LiveConference con : liveConferences) {
				if(!con.getConference().equals("Sendeschleife") || BuildConfig.DEBUG){
					int i = -1;
					for (i = 0; i < con.getGroups().size(); i++) {
						Group g = con.getGroups().get(i);
						// setup header
						String group = g.getGroup().length() > 0 ? g.getGroup() : con.getConference();
						HeaderItem header = new HeaderItem(group);
						header.setDescription(con.getConference() + " - " + con.getDescription());
						header.setContentDescription(group);
						// setup list
						ArrayObjectAdapter listRowAdapter
								= new ArrayObjectAdapter(cardPresenter);
						listRowAdapter.addAll(listRowAdapter.size(), g.getRooms());
						rowsAdapter.add(i + 1, new ListRow(header, listRowAdapter));
					}
				}
//				rowsAdapter.add(i + 1, new DividerRow());

			}
		}
	}

	private void showWatchlist() {
		if(watchlistRow == null && recomendationsSectionsRow == null){
			recomendationsSectionsRow = new SectionRow(getString(R.string.recomendations));
			HeaderItem header = new HeaderItem(getString(R.string.watchlist));
			watchlistRow = new ListRow(header, watchListAdapter);
		}
		if(!mWatchlistVisible){
			rowsAdapter.add(RECOMENDATIONS_INDEX, recomendationsSectionsRow);
			rowsAdapter.add(RECOMENDATIONS_INDEX +1 , watchlistRow);
			mWatchlistVisible = true;
		}
	}

	private void hideWatchlist() {
		if(mWatchlistVisible){
			int i = rowsAdapter.indexOf(recomendationsSectionsRow);
			rowsAdapter.removeItems(i,2);
			mWatchlistVisible = false;
		}
	}

	private ListRow getRow(List<PersistentConference> conferences, CardPresenter cardPresenter, String tag, String description) {
		ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
		listRowAdapter.addAll(0, conferences);
		HeaderItem header = new HeaderItem(ConferenceUtil.getStringForTag(tag));
		header.setDescription(description);
		return new ListRow(header, listRowAdapter);
	}

}
