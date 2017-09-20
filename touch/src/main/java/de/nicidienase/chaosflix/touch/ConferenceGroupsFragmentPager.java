package de.nicidienase.chaosflix.touch;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.common.entities.recording.ConferencesWrapper;
import de.nicidienase.chaosflix.touch.fragments.ConferenceGroupFragment;

/**
 * Created by felix on 18.09.17.
 */
public class ConferenceGroupsFragmentPager extends FragmentPagerAdapter {

	private static final String TAG = ConferenceGroupsFragmentPager.class.getSimpleName();
	private final Context mContext;
	private List<String> orderedConferencesList = new ArrayList<>();
	private Map<String, List<Conference>> mConferenceMap;

	public ConferenceGroupsFragmentPager(Context context, FragmentManager fm) {
		super(fm);
		this.mContext = context;
	}

	@Override
	public Fragment getItem(int position) {
//		ConferenceGroupFragment conferenceFragment = ConferenceGroupFragment.newInstance(1);
		ConferenceGroupFragment conferenceFragment = ConferenceGroupFragment.newInstance(getNumColumns());
		String confKey = orderedConferencesList.get(position);
		List<Conference> conferences = mConferenceMap.get(confKey);
		conferenceFragment.setContent(conferences);
		conferenceFragment.setRetainInstance(true);
		Log.d(TAG,"Created Fragment for: " + confKey);
		return conferenceFragment;
	}

	@Override
	public int getCount() {
		return orderedConferencesList.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return ConferencesWrapper.getStringForTag(orderedConferencesList.get(position));
	}

	public void setContent(Map<String, List<Conference>> conferenceMap) {
		mConferenceMap = conferenceMap;
//			orderedConferencesList = new ArrayList<>(conferenceMap.keySet());
		orderedConferencesList = new ArrayList<>();
		for (String tag : ConferencesWrapper.getOrderedConferencesList()) {
			if (conferenceMap.keySet().contains(tag)) {
				orderedConferencesList.add(tag);
			}
		}
		for (String tag : conferenceMap.keySet()) {
			if (!orderedConferencesList.contains(tag)) {
				orderedConferencesList.add(tag);
			}
		}
	}

	@Override
	public float getPageWidth(int position) {
		int integer = getNumColumns();
		return 1f/integer;
	}

	private int getNumColumns() {
		return mContext.getResources().getInteger(R.integer.num_columns);
	}
}
