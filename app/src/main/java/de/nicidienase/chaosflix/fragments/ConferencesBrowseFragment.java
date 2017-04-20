package de.nicidienase.chaosflix.fragments;

import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.DividerRow;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.SectionRow;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.nicidienase.chaosflix.CardPresenter;
import de.nicidienase.chaosflix.ItemViewClickedListener;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.activities.AbstractServiceConnectedAcitivty;
import de.nicidienase.chaosflix.entities.WatchlistItem;
import de.nicidienase.chaosflix.entities.recording.Conference;
import de.nicidienase.chaosflix.entities.recording.ConferencesWrapper;
import de.nicidienase.chaosflix.entities.streaming.Group;
import de.nicidienase.chaosflix.entities.streaming.LiveConference;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by felix on 21.03.17.
 */

public class ConferencesBrowseFragment extends BrowseFragment {

	private static final String TAG = ConferencesBrowseFragment.class.getSimpleName();
	public static final int FRAGMENT = R.id.browse_fragment;
	private ArrayObjectAdapter mRowsAdapter;
	private Map<String, List<Conference>> mConferences;
	CompositeDisposable mDisposables = new CompositeDisposable();
	private ArrayObjectAdapter watchListAdapter;
	private ListRow mWatchlistRow;
	private boolean streamsAvailable = false;
	private SectionRow mRecomendationsSectionsRow;

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

		final BrowseErrorFragment errorFragment =
				BrowseErrorFragment.showErrorFragment(getFragmentManager(), FRAGMENT);
		CardPresenter cardPresenter = new CardPresenter();
		mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
		watchListAdapter = new ArrayObjectAdapter(cardPresenter);

		Disposable disposable = ((AbstractServiceConnectedAcitivty) getActivity()).getmApiServiceObservable()
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
								liveConferences.add(LiveConference.getDummyObject());
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

	private void addRecordings(CardPresenter cardPresenter, ConferencesWrapper conferences) {
		mRowsAdapter.add(new SectionRow(getString(R.string.conferences)));
		mConferences = conferences.getConferencesBySeries();
		Set<String> keySet = mConferences.keySet();
		for (String tag : getOrderedConferencesList()) {
			if (keySet.contains(tag)) {
				ListRow row = getRow(mConferences, cardPresenter, tag, "");
				mRowsAdapter.add(row);
			}
		}
		for (String tag : keySet) {
			if (!getOrderedConferencesList().contains(tag)) {
				mRowsAdapter.add(getRow(mConferences, cardPresenter, tag, ""));
			}
		}
	}

	private void addStreams(CardPresenter cardPresenter, List<LiveConference> liveConferences) {
		if (liveConferences.size() > 0) {
			for (LiveConference con : liveConferences) {
				HeaderItem streamingHeader = new HeaderItem(con.getConference()
						+ " " + getString(R.string.streaming_prefix));
				streamingHeader.setContentDescription(con.getDescription());
				mRowsAdapter.add(0, new SectionRow(streamingHeader));
				int i = -1;
				for (i = 0; i < con.getGroups().size(); i++) {
					Group g = con.getGroups().get(i);
					ArrayObjectAdapter listRowAdapter
							= new ArrayObjectAdapter(cardPresenter);
					listRowAdapter.addAll(listRowAdapter.size(), g.getRooms());
					HeaderItem header = new HeaderItem(g.getGroup());
					header.setDescription(con.getConference() + " - " + con.getDescription());
					//HeaderItem header = new HeaderItem(STREAM_PREFIX + con.getConference());
					header.setContentDescription(g.getGroup());
					mRowsAdapter.add(i + 1, new ListRow(header, listRowAdapter));
				}
//				mRowsAdapter.add(i + 1, new DividerRow());

			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		List<WatchlistItem> watchlistItems
				= Lists.newArrayList(WatchlistItem.findAll(WatchlistItem.class));
		// setup and list items
		if(mWatchlistRow == null && mRecomendationsSectionsRow == null){
			int offset = streamsAvailable ? 2 : 0;
			mRecomendationsSectionsRow = new SectionRow(getString(R.string.recomendations));
			mRowsAdapter.add(offset, mRecomendationsSectionsRow);
			HeaderItem header = new HeaderItem(getString(R.string.watchlist));
//								header.setDescription(description);
			mWatchlistRow = new ListRow(header, watchListAdapter);
			mRowsAdapter.add(offset + 1, mWatchlistRow);
		}
		updateWatchlist(watchlistItems);
	}

	private Disposable updateWatchlist(List<WatchlistItem> watchlistItems) {
		return ((AbstractServiceConnectedAcitivty) getActivity()).getmApiServiceObservable()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(mediaApiService -> {
					watchListAdapter.clear();
					if(watchlistItems.size() > 0){
						Observable.fromIterable(watchlistItems)
								.flatMap(watchlistItem -> mediaApiService.getEvent(watchlistItem.getEventId()))
								.observeOn(AndroidSchedulers.mainThread())
								.subscribe(event -> watchListAdapter.add(event));
					} else {
						watchListAdapter.add("Watchlist empty");
					}
				});
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public void onStop() {
		mDisposables.dispose();
		super.onStop();
	}

	private ListRow getRow(Map<String, List<Conference>> conferences, CardPresenter cardPresenter, String tag, String description) {
		ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
		listRowAdapter.addAll(0, conferences.get(tag));
		HeaderItem header = new HeaderItem(getStringForTag(tag));
		header.setDescription(description);
		return new ListRow(header, listRowAdapter);
	}

	private String getStringForTag(String tag) {
		switch (tag) {
			case "congress":
				return "Congress";
			case "sendezentrum":
				return "Sendezentrum";
			case "camp":
				return "Camp";
			case "broadcast/chaosradio":
				return "Chaosradio";
			case "eh":
				return "Easterhegg";
			case "gpn":
				return "GPN";
			case "froscon":
				return "FrOSCon";
			case "mrmcd":
				return "MRMCD";
			case "sigint":
				return "SIGINT";
			case "datenspuren":
				return "Datenspuren";
			case "fiffkon":
				return "FifFKon";
			case "blinkenlights":
				return "Blinkenlights";
			case "chaoscologne":
				return "1c2 Chaos Cologne";
			case "cryptocon":
				return "CryptoCon";
			case "other conferences":
				return "Other Conferences";
			case "denog":
				return "DENOG";
			case "vcfb":
				return "Vintage Computing Festival Berlin";
			case "hackover":
				return "Hackover";
			case "netzpolitik":
				return "Das ist Netzpolitik!";
			default:
				return tag;
		}
	}

	private List<String> getOrderedConferencesList() {
		return Arrays.asList("congress", "sendezentrum", "camp",
				"broadcast/chaosradio", "eh", "gpn",
				"froscon", "mrmcd", "sigint",
				"datenspuren", "fiffkon", "cryptocon");
	}
}
