package de.nicidienase.chaosflix.touch.browse.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.nicidienase.chaosflix.touch.R;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup;
import de.nicidienase.chaosflix.touch.browse.ConferenceGroupFragment;

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
		ConferenceGroup conferenceGroup = conferenceGroupList.get(position);
		ConferenceGroupFragment conferenceFragment = ConferenceGroupFragment.newInstance(conferenceGroup, getNumColumns());
		Log.d(TAG, "Created Fragment for: " + conferenceGroup.getName());
		return conferenceFragment;
	}

	@Override
	public long getItemId(int position) {
		return conferenceGroupList.get(position).getId();
	}

	@Override
	public int getCount() {
		return conferenceGroupList.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return conferenceGroupList.get(position).getName();
	}

	private int getNumColumns() {
		return mContext.getResources().getInteger(R.integer.num_columns);
	}

	public void addGroup(ConferenceGroup conferenceGroup) {
		conferenceGroupList.add(conferenceGroup);
		//		Collections.sort(conferenceGroupList,(g1, g2) -> g1.getIndex()-g2.getIndex());
		notifyDataSetChanged();
	}

	public void setContent(List<ConferenceGroup> conferenceGroups) {
		conferenceGroupList = conferenceGroups;
		notifyDataSetChanged();
	}
}
