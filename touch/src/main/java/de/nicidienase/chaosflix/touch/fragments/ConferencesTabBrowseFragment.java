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
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentConference;
import de.nicidienase.chaosflix.touch.ConferenceGroupsFragmentPager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class ConferencesTabBrowseFragment extends ChaosflixFragment {

	private static final String TAG = ConferencesTabBrowseFragment.class.getSimpleName();

	private static final String ARG_COLUMN_COUNT = "column-count";
	private static final String CURRENTTAB_KEY = "current_tab";
	private static final String VIEWPAGER_STATE = "viewpager_state";
	private int mColumnCount = 1;
	private OnConferenceListFragmentInteractionListener mListener;
	private Toolbar mToolbar;
	private Context mContext;
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

		Log.d(TAG, "onCreate");
		if (getArguments() != null) {
			mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tab_pager_layout, container, false);
		ConferenceGroupsFragmentPager fragmentPager
				= new ConferenceGroupsFragmentPager(this.getContext(), getChildFragmentManager());

		getViewModel().getConferencesMap()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(conferencesWrapper -> {
					fragmentPager.setContent(conferencesWrapper.getConferenceMap());
					mViewPager.onRestoreInstanceState(getArguments().getParcelable(VIEWPAGER_STATE));
				});

		mViewPager = view.findViewById(R.id.viewpager);
		mViewPager.setAdapter(fragmentPager);

		TabLayout tabLayout = view.findViewById(R.id.sliding_tabs);
		tabLayout.setupWithViewPager(mViewPager);

		mToolbar = view.findViewById(R.id.toolbar);
		((AppCompatActivity) mContext).setSupportActionBar(mToolbar);
//		mToolbar.setLogo(R.drawable.toolbar_icon);

		return view;
	}

	@Override
	public void onPause() {
		super.onPause();
		getArguments().putParcelable(VIEWPAGER_STATE, mViewPager.onSaveInstanceState());
	}

	@Override
	public void onStop() {
		super.onStop();
		mDisposable.clear();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public interface OnConferenceListFragmentInteractionListener {
		void onConferenceSelected(PersistentConference conference);
	}
}