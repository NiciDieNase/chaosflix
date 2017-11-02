package de.nicidienase.chaosflix.touch;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.common.entities.recording.ConferencesWrapper;
import de.nicidienase.chaosflix.common.entities.recording.persistence.ConferenceGroup;
import de.nicidienase.chaosflix.touch.fragments.ConferenceGroupFragment;

/**
 * Created by felix on 18.09.17.
 */
public class ConferenceGroupsFragmentPager extends FragmentPagerAdapter {

	private static final String TAG = ConferenceGroupsFragmentPager.class.getSimpleName();
	private final Context mContext;
	private List<ConferenceGroup> conferenceGroupList = new ArrayList<>();

	public ConferenceGroupsFragmentPager(Context context, FragmentManager fm) {
		super(fm);
		this.mContext = context;
	}

	@Override
	public Fragment getItem(int position) {
		String confKey = conferenceGroupList.get(position);
		ConferenceGroupFragment conferenceFragment = ConferenceGroupFragment.newInstance(confKey,1);
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
		return ConferencesWrapper.Companion.getStringForTag(orderedConferencesList.get(position));
	}

	public void setContent(Map<String, List<Conference>> conferenceMap) {
		orderedConferencesList = new ArrayList<>();
		for (String tag : ConferencesWrapper.Companion.getOrderedConferencesList()) {
			if (conferenceMap.keySet().contains(tag)) {
				orderedConferencesList.add(tag);
			}
		}
		for (String tag : conferenceMap.keySet()) {
			if (!orderedConferencesList.contains(tag)) {
				orderedConferencesList.add(tag);
			}
		}
		notifyDataSetChanged();
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
