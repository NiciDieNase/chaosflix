package de.nicidienase.chaosflix.touch.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.touch.ConferenceGroupsFragmentPager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by felix on 19.09.17.
 */

public class ConferencesTabBrowseFragment extends ChaosflixFragment {

	private static final String TAG = ConferencesTabBrowseFragment.class.getSimpleName();

	private static final String ARG_COLUMN_COUNT = "column-count";
	private static final String CURRENTTAB_KEY = "current_tab";
	private int mColumnCount = 1;
	private OnConferenceListFragmentInteractionListener mListener;
	private Toolbar mToolbar;
	private Context mContext;
	private int mCurrentTab = -1;
	private ViewPager mViewPager;

	private final CompositeDisposable mDisposable = new CompositeDisposable();


	public static ConferencesTabBrowseFragment newInstance(int columnCount) {
		ConferencesTabBrowseFragment fragment = new ConferencesTabBrowseFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_COLUMN_COUNT, columnCount);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mContext = context;
		if (context instanceof OnConferenceListFragmentInteractionListener) {
			mListener = (OnConferenceListFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnListFragmentInteractionListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG,"onCreate");
		if(savedInstanceState != null){
			mCurrentTab = savedInstanceState.getInt(CURRENTTAB_KEY);
		}
		if (getArguments() != null) {
			mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tab_pager_layout, container, false);
		ConferenceGroupsFragmentPager fragmentPager
				= new ConferenceGroupsFragmentPager(this.getContext(),getChildFragmentManager());

		getViewModel().getConferencesWrapper()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(conferencesWrapper -> {
					fragmentPager.setContent(conferencesWrapper.getConferencesBySeries());
					mViewPager.setCurrentItem(mCurrentTab);
						});

		mViewPager = view.findViewById(R.id.viewpager);
		mViewPager.setAdapter(fragmentPager);

		TabLayout tabLayout = view.findViewById(R.id.sliding_tabs);
		tabLayout.setupWithViewPager(mViewPager);

		if(mCurrentTab != -1){
			mViewPager.setCurrentItem(mCurrentTab);
		}

		mToolbar = view.findViewById(R.id.toolbar);
		((AppCompatActivity)mContext).setSupportActionBar(mToolbar);
//		mToolbar.setLogo(R.drawable.toolbar_icon);

		return view;
	}

	@Override
	public void onStop() {
		super.onStop();
		mDisposable.clear();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(CURRENTTAB_KEY, mViewPager.getCurrentItem());
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
