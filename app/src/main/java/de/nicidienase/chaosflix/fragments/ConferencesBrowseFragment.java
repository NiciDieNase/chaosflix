package de.nicidienase.chaosflix.fragments;

import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.nicidienase.chaosflix.CardPresenter;
import de.nicidienase.chaosflix.entities.Conference;
import de.nicidienase.chaosflix.entities.Conferences;
import de.nicidienase.chaosflix.network.MediaCCCClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by felix on 21.03.17.
 */

public class ConferencesBrowseFragment extends BrowseFragment {

	private static final String TAG = ConferencesBrowseFragment.class.getSimpleName();
	private MediaCCCClient mMediaCCCClient;
	private ArrayObjectAdapter mRowsAdapter;
	private Map<String, List<Conference>> mConferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMediaCCCClient = new MediaCCCClient();

		mMediaCCCClient.listConferences().enqueue(new Callback<Conferences>() {
			@Override
			public void onResponse(Call<Conferences> call, Response<Conferences> response) {
				Conferences body = response.body();
				mConferences = body.getConferencesBySeries();
				mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
				CardPresenter cardPresenter = new CardPresenter();
				Set<String> keySet = mConferences.keySet();
				for(String tag: getOrderedConferencesList()){
					if(keySet.contains(tag)){
						addRow(mConferences, cardPresenter,tag);
					}
				}
				for(String tag: keySet){
					if(!getOrderedConferencesList().contains(tag)){
						addRow(mConferences, cardPresenter, tag);
					}
				}
				setAdapter(mRowsAdapter);
			}

			@Override
			public void onFailure(Call<Conferences> call, Throwable t) {
				Log.d(TAG,"Error loading conferences",t);
				t.printStackTrace();
			}
		});

		setOnItemViewClickedListener(new ItemViewClickedListener(this));
	}

	private void addRow(Map<String, List<Conference>> conferences, CardPresenter cardPresenter, String tag) {
		ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
		listRowAdapter.addAll(0,conferences.get(tag));
		HeaderItem header = new HeaderItem(getStringForTag(tag));
		mRowsAdapter.add(new ListRow(header, listRowAdapter));
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
				return "other Conferences";
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
