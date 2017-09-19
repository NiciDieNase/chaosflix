package de.nicidienase.chaosflix.touch.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.common.entities.recording.ConferencesWrapper;
import de.nicidienase.chaosflix.touch.ConferenceGroupsFragmentPager;
import de.nicidienase.chaosflix.touch.adapters.ItemRecyclerViewAdapter;

/**
 * Created by felix on 19.09.17.
 */

public class ConferencesBrowseFragment extends Fragment {

	private static final String ARG_COLUMN_COUNT = "column-count";
	private int mColumnCount = 1;
	private OnConferenceListFragmentInteractionListener mListener;
	private ConferencesWrapper conferencesWrapper;

	public ConferencesBrowseFragment() {
	}

	public void setContent(ConferencesWrapper conferencesWrapper){
		this.conferencesWrapper = conferencesWrapper;
	}

	public static ConferencesBrowseFragment newInstance(int columnCount) {
		ConferencesBrowseFragment fragment = new ConferencesBrowseFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_COLUMN_COUNT, columnCount);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.tab_layout, container, false);
		ConferenceGroupsFragmentPager fragmentPager
				= new ConferenceGroupsFragmentPager(this.getContext(),getChildFragmentManager());
		fragmentPager.setContent(conferencesWrapper.getConferencesBySeries());

		ViewPager pager = (ViewPager) view.findViewById(R.id.viewpager);
		pager.setAdapter(fragmentPager);

		TabLayout tabLayout = (TabLayout) view.findViewById(R.id.sliding_tabs);
		tabLayout.setupWithViewPager(pager);
		return view;
	}


	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnConferenceListFragmentInteractionListener) {
			mListener = (OnConferenceListFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnListFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public interface OnConferenceListFragmentInteractionListener{
		void onConferenceSelected(Conference conference);
	}
}
