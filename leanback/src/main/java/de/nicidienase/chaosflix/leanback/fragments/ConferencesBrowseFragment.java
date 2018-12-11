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
import android.support.v17.leanback.widget.SectionRow;
import android.support.v4.app.FragmentManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.nicidienase.chaosflix.BuildConfig;
import de.nicidienase.chaosflix.leanback.ChaosflixEventAdapter;
import de.nicidienase.chaosflix.leanback.R;
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

	public static final  int                   FRAGMENT    = R.id.browse_fragment;
	private static final String                TAG         = ConferencesBrowseFragment.class.getSimpleName();
	private              ArrayObjectAdapter    rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
	private              ChaosflixEventAdapter watchListAdapter;
	private              ChaosflixEventAdapter inProgressAdapter;
	private              ChaosflixEventAdapter promotedAdapter;
	private              ListRow               watchlistRow;
	private              ListRow               inProgressRow;

	private SectionRow streamingSection;
	private DividerRow streamsDivider;
	private SectionRow recomendationsSectionsRow;
	private DividerRow recomendationsDivider;
	private SectionRow conferencesSection;


	private BrowseViewModel viewModel;

	private Map<String, ListRow>          conferencesGroupRows = new HashMap<>();
	private DiffCallback<PersistentEvent> eventDiffCallback    = new DiffCallback<PersistentEvent>() {
		@Override
		public boolean areItemsTheSame(@NonNull PersistentEvent oldItem, @NonNull PersistentEvent newItem) {
			return oldItem.getGuid().equals(newItem.getGuid());
		}

		@Override
		public boolean areContentsTheSame(@NonNull PersistentEvent oldItem, @NonNull PersistentEvent newItem) {
			return oldItem.getGuid().equals(newItem.getGuid());
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getResources().getString(R.string.app_name));
		setBadgeDrawable(getResources().getDrawable(R.drawable.chaosflix_icon));

		//		setHeaderPresenterSelector(new PresenterSelector() {
		//			@Override
		//			public Presenter getPresenter(Object item) {
		//				return new HeaderItemPresenter();
		//			}
		//		});

		viewModel = ViewModelProviders.of(this, new ViewModelFactory(requireContext())).get(BrowseViewModel.class);

		FragmentManager fragmentManager = getFragmentManager();
		final BrowseErrorFragment errorFragment;
		if(fragmentManager != null){
				errorFragment = BrowseErrorFragment.showErrorFragment(fragmentManager, FRAGMENT);
		}else {
			errorFragment = null;
		}
		CardPresenter conferencePresenter = new CardPresenter(R.style.ConferenceCardStyle);
		CardPresenter eventPresenter = new CardPresenter(R.style.EventCardStyle);

		watchListAdapter = new ChaosflixEventAdapter(eventPresenter);
		inProgressAdapter = new ChaosflixEventAdapter(eventPresenter);
		promotedAdapter = new ChaosflixEventAdapter(eventPresenter);

		streamingSection = new SectionRow(new HeaderItem(getString(R.string.livestreams)));
		streamsDivider = new DividerRow();
		recomendationsSectionsRow = new SectionRow(new HeaderItem(getString(R.string.recomendations)));
		recomendationsDivider = new DividerRow();
		conferencesSection = new SectionRow(new HeaderItem(getString(R.string.conferences)));

		// Streams
		rowsAdapter.add(0, streamingSection);
		rowsAdapter.add(streamsDivider);

		// Recomendations
		Row promotedRow = new ListRow(new HeaderItem("Promoted"), promotedAdapter);
		watchlistRow = new ListRow(new HeaderItem(getString(R.string.watchlist)), watchListAdapter);
		inProgressRow = new ListRow(new HeaderItem("Continue Watching"), inProgressAdapter);

		rowsAdapter.add(recomendationsSectionsRow);
		rowsAdapter.add(promotedRow);
		rowsAdapter.add(watchlistRow);
		rowsAdapter.add(inProgressRow);
		rowsAdapter.add(recomendationsDivider);

		// Conferences
		rowsAdapter.add(conferencesSection);

		setOnItemViewClickedListener(new ItemViewClickedListener(this));
		setAdapter(rowsAdapter);

		viewModel.updateConferences().observe(this, downloaderEvent -> {
			if (downloaderEvent.getData() != null) {
			}
			if (downloaderEvent.getError() != null) {
				if (errorFragment != null && !errorFragment.isDetached()) {
					errorFragment.setErrorContent(downloaderEvent.getError());
				}
			}
			switch (downloaderEvent.getState()) {
				case RUNNING:
					break;
				case DONE:
					if(errorFragment != null){
						errorFragment.dismiss();
					}
					break;
			}
		});

		viewModel.getConferenceGroups().observe(this, conferenceGroups -> {
			if (conferenceGroups != null && conferenceGroups.size() > 0) {
				if(errorFragment != null){
					errorFragment.dismiss();
				}
				Collections.sort(conferenceGroups);
				for (ConferenceGroup group : conferenceGroups) {
					ListRow row;

					if (conferencesGroupRows.containsKey(group.getName())) {
						row = conferencesGroupRows.get(group.getName());
					} else {
						row = buildRow(new ArrayList<>(), conferencePresenter, group.getName(), ConferenceUtil.getStringForTag(group.getName()));
						rowsAdapter.add(row);
						conferencesGroupRows.put(group.getName(), row);
					}
					bindConferencesToRow(conferencePresenter, group, row);

				}
			}
		});

		viewModel.getBookmarkedEvents().observe(this, (bookmarks) -> {
			if (bookmarks != null) {
				watchListAdapter.setItems(bookmarks, eventDiffCallback);
			}
		});
		viewModel.getInProgressEvents().observe(this, (inProgress) -> {
			if (inProgress != null) {
				inProgressAdapter.setItems(inProgress, eventDiffCallback);
			}
		});

		viewModel.getPromotedEvents().observe(this, (promoted) -> {
			if(promoted != null){
				promotedAdapter.setItems(promoted, eventDiffCallback);
			}
		});

		viewModel.getLivestreams().observe(this, liveConferences -> {
			if (liveConferences != null) {
				//				if (BuildConfig.DEBUG) {
				//					liveConferences.add(LiveConference.getDummyObject());
				//				}
				addStreams(conferencePresenter, liveConferences);
				//				errorFragment.dismiss();
			}
		});
	}

	private void bindConferencesToRow(CardPresenter cardPresenter, ConferenceGroup group, ListRow row) {
		viewModel.getConferencesByGroup(group.getId()).observe(this, conferences -> {
			if(conferences != null){
				Collections.sort(conferences);
			}
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
	}

	private void addStreams(CardPresenter cardPresenter, List<LiveConference> liveConferences) {
		if (liveConferences.size() > 0) {
			for (LiveConference con : liveConferences) {
				if (!con.getConference().equals("Sendeschleife") || BuildConfig.DEBUG) {
					for (Group g : con.getGroups()) {
						// setup header
						rowsAdapter.add(new SectionRow(new HeaderItem(con.getConference())));

						String group = g.getGroup().length() > 0 ? g.getGroup() : con.getConference();
						HeaderItem header = new HeaderItem(group);
						header.setDescription(con.getConference() + " - " + con.getDescription());
						header.setContentDescription(group);
						// setup list
						ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
						listRowAdapter.addAll(listRowAdapter.size(), g.getRooms());
						int index = getSectionIndex(recomendationsSectionsRow);
						if (index >= 0) {
							rowsAdapter.add(index, new ListRow(header, listRowAdapter));
						} else {
							rowsAdapter.add(new ListRow(header, listRowAdapter));
						}
					}
				}

			}
		}
	}

	private ListRow buildRow(List<PersistentConference> conferences, CardPresenter cardPresenter, String tag, String description) {
		ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
		listRowAdapter.addAll(0, conferences);
		HeaderItem header = new HeaderItem(ConferenceUtil.getStringForTag(tag));
		//		header.setDescription(description);
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
