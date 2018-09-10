package de.nicidienase.chaosflix.leanback.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.app.BrowseSupportFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.SectionRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.nicidienase.chaosflix.BuildConfig;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentConference;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Group;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference;
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem;
import de.nicidienase.chaosflix.common.util.ThreadHandler;
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel;
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory;
import de.nicidienase.chaosflix.leanback.CardPresenter;
import de.nicidienase.chaosflix.leanback.ItemViewClickedListener;
import de.nicidienase.chaosflix.R;

/**
 * Created by felix on 21.03.17.
 */

public class ConferencesBrowseFragment extends BrowseSupportFragment {

	private static final String TAG = ConferencesBrowseFragment.class.getSimpleName();
	public static final int FRAGMENT = R.id.browse_fragment;
	private ArrayObjectAdapter mRowsAdapter;
	private Map<String, List<PersistentConference>> mConferences;
	private ArrayObjectAdapter watchListAdapter;
	private ListRow mWatchlistRow;
	private boolean streamsAvailable = false;
	private SectionRow mStreamingSection;
	private SectionRow mRecomendationsSectionsRow;
	private SectionRow mConferencesSection;
	private boolean mWatchlistVisible = false;

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

		BrowseViewModel viewModel = ViewModelProviders.of(this).get(BrowseViewModel.class);

		final BrowseErrorFragment errorFragment =
				BrowseErrorFragment.showErrorFragment(getFragmentManager(), FRAGMENT);
		CardPresenter cardPresenter = new CardPresenter();
		mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
		watchListAdapter = new ArrayObjectAdapter(cardPresenter);

		viewModel.getConferenceGroups().observe(this, new Observer<List<ConferenceGroup>>() {
			@Override
			public void onChanged(@Nullable List<ConferenceGroup> conferenceGroups) {

			}
		});
		Disposable disposable = ((LeanbackBaseActivity) getActivity()).getApiServiceObservable()
				.subscribe(mediaApiService -> {
					mDisposables.add(Observable.zip(mediaApiService.getStreamingConferences(),
							mediaApiService.getConferences(),
							(liveConferences, conferencesWrapper) -> {
								ArrayList<Object> list = new ArrayList<>();
								list.add(liveConferences);
								list.add(conferencesWrapper);
								return list;
							})
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(objects -> {
								List<LiveConference> liveConferences = (List<LiveConference>) objects.get(0);
								ConferencesWrapper conferences = (ConferencesWrapper) objects.get(1);
								if (BuildConfig.DEBUG) {
									liveConferences.add(LiveConference.getDummyObject());
								}
								streamsAvailable = liveConferences.size() > 0;
								addStreams(cardPresenter, liveConferences);
								addRecordings(cardPresenter, conferences);
								errorFragment.dismiss();
								setOnItemViewClickedListener(new ItemViewClickedListener(this));
								setAdapter(mRowsAdapter);
								setSelectedPosition(1);
							}));
				});
		mDisposables.add(disposable);
	}

	private void addRecordings(CardPresenter cardPresenter, Map<String, List<PersistentConference>> conferences) {
		mConferencesSection = new SectionRow(getString(R.string.conferences));
		mRowsAdapter.add(mConferencesSection);

		Set<String> keySet = mConferences.keySet();
		for (String tag : ConferencesWrapper.getOrderedConferencesList()) {
			if (keySet.contains(tag)) {
				ListRow row = getRow(mConferences, cardPresenter, tag, "");
				mRowsAdapter.add(row);
			}
		}
		for (String tag : keySet) {
			if (!ConferencesWrapper.getOrderedConferencesList().contains(tag)) {
				mRowsAdapter.add(getRow(mConferences, cardPresenter, tag, ""));
			}
		}
	}

	private void addStreams(CardPresenter cardPresenter, List<LiveConference> liveConferences) {
		if (liveConferences.size() > 0) {
			HeaderItem streamingHeader = new HeaderItem(getString(R.string.livestreams));
			mStreamingSection = new SectionRow(streamingHeader);
			mRowsAdapter.add(0, mStreamingSection);
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
						mRowsAdapter.add(i + 1, new ListRow(header, listRowAdapter));
					}
				}
//				mRowsAdapter.add(i + 1, new DividerRow());

			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
//
//		List<WatchlistItem> watchlistItems = new ArrayList<>();
//		Iterator<WatchlistItem> all = WatchlistItem.findAll(WatchlistItem.class);
//		while (all.hasNext()){
//			watchlistItems.add(all.next());
//		}
		// setup and list items
//		updateWatchlist(watchlistItems);
	}

	private void showWatchlist() {
		if(mWatchlistRow == null && mRecomendationsSectionsRow == null){
			mRecomendationsSectionsRow = new SectionRow(getString(R.string.recomendations));
			HeaderItem header = new HeaderItem(getString(R.string.watchlist));
			mWatchlistRow = new ListRow(header, watchListAdapter);
		}
		int offset = getWatchlistOffset();
		if(!mWatchlistVisible){
			mRowsAdapter.add(offset, mRecomendationsSectionsRow);
			mRowsAdapter.add(offset + 1, mWatchlistRow);
			mWatchlistVisible = true;
		}
	}

	private Disposable updateWatchlist(List<WatchlistItem> watchlistItems) {
		return ((LeanbackBaseActivity) getActivity()).getApiServiceObservable()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(mediaApiService -> {
					showWatchlist();
					watchListAdapter.clear();
					if(watchlistItems.size() > 0){
//						int i = Math.max(0,mRowsAdapter.indexOf(mConferencesSection));
//						mRowsAdapter.add(i,mRecomendationsSectionsRow);
//						mRowsAdapter.add(i+1,watchListAdapter);
						Observable.fromIterable(watchlistItems)
								.flatMap(watchlistItem -> mediaApiService.getEvent(watchlistItem.getEventId()))
								.observeOn(AndroidSchedulers.mainThread())
								.subscribe(event -> watchListAdapter.add(event));
					} else {
//						watchListAdapter.add("Watchlist empty");
						hideWatchlist();
					}
				});
	}

	private void hideWatchlist() {
		if(mWatchlistVisible){
			int i = mRowsAdapter.indexOf(mRecomendationsSectionsRow);
			mRowsAdapter.removeItems(i,2);
			mWatchlistVisible = false;
		}
	}

	private int getWatchlistOffset() {
		if(mRowsAdapter.size()>0 || streamsAvailable){
			int i = mRowsAdapter.indexOf(mConferencesSection);
			return Math.max(i,0);
		} else {
			return 0;
		}
	}

	private ListRow getRow(Map<String, List<PersistentConference>> conferences, CardPresenter cardPresenter, String tag, String description) {
		ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
		listRowAdapter.addAll(0, conferences.get(tag));
		HeaderItem header = new HeaderItem(ConferencesWrapper.getStringForTag(tag));
		header.setDescription(description);
		return new ListRow(header, listRowAdapter);
	}

}
