package de.nicidienase.chaosflix.leanback.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.BrowseSupportFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.DiffCallback;
import android.support.v17.leanback.widget.DividerRow;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.SectionRow;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.nicidienase.chaosflix.BuildConfig;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentConference;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Group;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference;
import de.nicidienase.chaosflix.common.util.ConferenceUtil;
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel;
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory;
import de.nicidienase.chaosflix.leanback.CardPresenter;
import de.nicidienase.chaosflix.leanback.ItemViewClickedListener;

public class ConferencesBrowseFragment extends BrowseSupportFragment {

	private static final String TAG = ConferencesBrowseFragment.class.getSimpleName();
	public static final int FRAGMENT = R.id.browse_fragment;
	private ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
	private ArrayObjectAdapter watchListAdapter;
	private ListRow watchlistRow;

	private SectionRow streamingSection;
	private SectionRow recomendationsSectionsRow;
	private SectionRow conferencesSection;

	private boolean watchlistVisible = false;
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

		viewModel = ViewModelProviders.of(this, new ViewModelFactory(requireContext()))
				.get(BrowseViewModel.class);

//		final BrowseErrorFragment errorFragment =
//				BrowseErrorFragment.showErrorFragment(getFragmentManager(), FRAGMENT);
		CardPresenter cardPresenter = new CardPresenter();

		watchListAdapter = new ArrayObjectAdapter(cardPresenter);

		// Streams
		streamingSection = new SectionRow(new HeaderItem(getString(R.string.livestreams)));
		rowsAdapter.add(0, streamingSection);
		rowsAdapter.add(new DividerRow());
		// Recomendations
		recomendationsSectionsRow = new SectionRow(getString(R.string.recomendations));
		rowsAdapter.add(recomendationsSectionsRow);
		watchlistRow = new ListRow(new HeaderItem(getString(R.string.watchlist)), watchListAdapter);
		rowsAdapter.add(watchlistRow);
		rowsAdapter.add(new DividerRow());
		// Conferences
		conferencesSection = new SectionRow(getString(R.string.conferences));
		rowsAdapter.add(conferencesSection);

		setOnItemViewClickedListener(new ItemViewClickedListener(this));
		setAdapter(rowsAdapter);


		viewModel.getConferenceGroups().observe(this, conferenceGroups -> {

			for (ConferenceGroup group : conferenceGroups) {
				rowsAdapter.add(buildRowForConferencesGroup(cardPresenter, group));
			}
//			errorFragment.dismiss();
		});

		viewModel.getBookmarkedEvents().observe(this, (bookmarks) -> {
			if (bookmarks != null) {
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
			}
		});

		viewModel.getLivestreams().observe(this, liveConferences -> {
			if (liveConferences != null) {
//				if (BuildConfig.DEBUG) {
//					liveConferences.add(LiveConference.getDummyObject());
//				}
				addStreams(cardPresenter, liveConferences);
//				errorFragment.dismiss();
			}
		});
	}

	private Row buildRowForConferencesGroup(CardPresenter cardPresenter, ConferenceGroup group) {
		ListRow row = getRow(Collections.EMPTY_LIST, cardPresenter, group.getName(), ConferenceUtil.getStringForTag(group.getName()));
		viewModel.getConferencesByGroup(group.getId()).observe(this, conferences -> {
			((ArrayObjectAdapter) row.getAdapter()).setItems(conferences, new DiffCallback<PersistentConference>() {
				@Override
				public boolean areItemsTheSame(@NonNull PersistentConference oldItem, @NonNull PersistentConference newItem) {
					return oldItem.getUrl().equals(newItem.getUrl());
				}

				@Override
				public boolean areContentsTheSame(@NonNull PersistentConference oldItem, @NonNull PersistentConference newItem) {
					return oldItem.getUpdatedAt().equals(newItem.getUpdatedAt());
				}
			});
		});
		return row;
	}

	private void addStreams(CardPresenter cardPresenter, List<LiveConference> liveConferences) {
		if (liveConferences.size() > 0) {
			for (LiveConference con : liveConferences) {
				if (!con.getConference().equals("Sendeschleife") || BuildConfig.DEBUG) {
					for (Group g: con.getGroups()) {
						// setup header
						String group = g.getGroup().length() > 0 ? g.getGroup() : con.getConference();
						HeaderItem header = new HeaderItem(group);
						header.setDescription(con.getConference() + " - " + con.getDescription());
						header.setContentDescription(group);
						// setup list
						ArrayObjectAdapter listRowAdapter
								= new ArrayObjectAdapter(cardPresenter);
						listRowAdapter.addAll(listRowAdapter.size(), g.getRooms());
						int index = getSectionIndex(recomendationsSectionsRow);
						if(index >= 0){
							rowsAdapter.add(index, new ListRow(header, listRowAdapter));
						} else {
							rowsAdapter.add(new ListRow(header, listRowAdapter));
						}
					}
				}

			}
		}
	}

	private ListRow getRow(List<PersistentConference> conferences, CardPresenter cardPresenter, String tag, String description) {
		ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
		listRowAdapter.addAll(0, conferences);
		HeaderItem header = new HeaderItem(ConferenceUtil.getStringForTag(tag));
		header.setDescription(description);
		return new ListRow(header, listRowAdapter);
	}

	private int getSectionIndex(SectionRow section) {
		if (rowsAdapter != null && section != null) {
			return rowsAdapter.indexOf(recomendationsSectionsRow);
		} else {
			return -1;
		}
	}
}
