package de.nicidienase.chaosflix.fragments;

import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.nicidienase.chaosflix.CardPresenter;
import de.nicidienase.chaosflix.ItemViewClickedListener;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.activities.AbstractServiceConnectedAcitivty;
import de.nicidienase.chaosflix.entities.recording.Conference;
import de.nicidienase.chaosflix.entities.streaming.Group;
import de.nicidienase.chaosflix.entities.streaming.LiveConference;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by felix on 21.03.17.
 */

public class ConferencesBrowseFragment extends BrowseFragment {

	private static final String TAG = ConferencesBrowseFragment.class.getSimpleName();
	public static final int FRAGMENT = R.id.browse_fragment;
	private ArrayObjectAdapter mRowsAdapter;
	private Map<String, List<Conference>> mConferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getResources().getString(R.string.app_name));

		final BrowseErrorFragment errorFragment =
				BrowseErrorFragment.showErrorFragment(getFragmentManager(),FRAGMENT);
		CardPresenter cardPresenter = new CardPresenter();
		mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
		((AbstractServiceConnectedAcitivty)getActivity()).getmApiServiceObservable()
				.subscribe(mediaApiService -> {
					mediaApiService.getStreamingConferences()
							.subscribe(liveConferences -> {
								if(liveConferences.size() > 0){
									for(LiveConference con : liveConferences){
										ArrayObjectAdapter listRowAdapter
												= new ArrayObjectAdapter(cardPresenter);
										for(Group g: con.getGroups()){
											listRowAdapter.addAll(0,g.getRooms());
										}
										HeaderItem header = new HeaderItem(con.getConference());
										header.setDescription(con.getDescription());
										header.setContentDescription(con.getAuthor());
										mRowsAdapter.add(new ListRow(header,listRowAdapter));
									}
								}
								mediaApiService.getConferences()
										.doOnError(t -> {errorFragment.setErrorContent(t.getMessage());})
										.observeOn(AndroidSchedulers.mainThread())
										.subscribe(conferences -> {
											mConferences = conferences.getConferencesBySeries();
											Set<String> keySet = mConferences.keySet();
											for(String tag: getOrderedConferencesList()){
												if(keySet.contains(tag)){
													ListRow row = getRow(mConferences, cardPresenter, tag,"");
													mRowsAdapter.add(row);
												}
											}
											for(String tag: keySet){
												if(!getOrderedConferencesList().contains(tag)){
													mRowsAdapter.add(getRow(mConferences, cardPresenter, tag,""));
												}
											}
											errorFragment.dismiss();
											setAdapter(mRowsAdapter);
										});
							});
					setOnItemViewClickedListener(new ItemViewClickedListener(this));
//					setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
//						@Override
//						public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
//							if(item instanceof Conference){
//								Conference con = (Conference) item;
//								row.getHeaderItem().setDescription(con.getTitle());
//							}
//							if(item instanceof Room)
//						}
//					});

				});
	}

	private ListRow getRow(Map<String, List<Conference>> conferences, CardPresenter cardPresenter, String tag, String description) {
		ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
		listRowAdapter.addAll(0,conferences.get(tag));
		HeaderItem header = new HeaderItem(getStringForTag(tag));
		header.setDescription(description);
		return new ListRow(header, listRowAdapter);
	}

	private String getStringForTag(String tag) {
		switch (tag){
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
			case "other mConferences":
				return "other ConferencesWrapper";
			case "blinkenlights":
				return "Blinkenlights";
			case "chaoscologne":
				return "1c2	Chaos Cologne";
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

	private List<String> getOrderedConferencesList(){
		return Arrays.asList(new String[]{"congress", "sendezentrum", "camp", "broadcast/chaosradio", "eh", "gpn", "froscon", "mrmcd",
				"sigint", "datenspuren", "fiffkon", "cryptocon"});
	}
}
