package de.nicidienase.chaosflix.touch;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.nicidienase.chaosflix.shared.entities.recording.Conference;
import de.nicidienase.chaosflix.shared.entities.recording.ConferencesWrapper;
import de.nicidienase.chaosflix.touch.fragments.ConferenceFragment;

/**
 * Created by felix on 18.09.17.
 */
public class ConferenceGroupsFragmentPager extends FragmentPagerAdapter {

	private static final String TAG = ConferenceGroupsFragmentPager.class.getSimpleName();
	private List<String> orderedConferencesList = new ArrayList<>();
	private Map<String, List<Conference>> mConferenceMap;

	public ConferenceGroupsFragmentPager(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		ConferenceFragment conferenceFragment = new ConferenceFragment();
		List<Conference> conferences = mConferenceMap.get(orderedConferencesList.get(position));
		conferenceFragment.setContent(conferences);
		return conferenceFragment;
	}

	@Override
	public int getCount() {
		return orderedConferencesList.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		String string = ConferencesWrapper.getStringForTag(orderedConferencesList.get(position));
		Log.d(TAG,string);
		return string;
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
}
