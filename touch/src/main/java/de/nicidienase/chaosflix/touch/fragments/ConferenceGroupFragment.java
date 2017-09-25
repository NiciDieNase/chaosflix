package de.nicidienase.chaosflix.touch.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.touch.ChaosflixViewModel;
import de.nicidienase.chaosflix.touch.adapters.ConferenceRecyclerViewAdapter;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;

public class ConferenceGroupFragment extends ChaosflixFragment {

	private static final String TAG = ConferenceGroupFragment.class.getSimpleName();

	private static final String ARG_COLUMN_COUNT = "column-count";
	private static final String ARG_GROUP_NAME = "group-name";
	private static final String LAYOUTMANAGER_STATE = "layoutmanager-state";
	private ConferencesTabBrowseFragment.OnConferenceListFragmentInteractionListener mListener;

	private int mColumnCount = 1;
	private String mGroupName;

	private ConferenceRecyclerViewAdapter mAdapter;

	CompositeDisposable mDisposable = new CompositeDisposable();
	private RecyclerView.LayoutManager mLayoutManager;

	public ConferenceGroupFragment() {
	}

	public static ConferenceGroupFragment newInstance(String group,int columnCount) {
		ConferenceGroupFragment fragment = new ConferenceGroupFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_COLUMN_COUNT, columnCount);
		args.putString(ARG_GROUP_NAME, group);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
			mGroupName = getArguments().getString(ARG_GROUP_NAME);
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.recycler_view_pager_layout, container, false);

		// Set the adapter
		if (view instanceof RecyclerView) {
			Context context = view.getContext();
			RecyclerView mRecyclerView = (RecyclerView) view;
			if (mColumnCount <= 1) {
				mLayoutManager = new LinearLayoutManager(context);
			} else {
				mLayoutManager = new GridLayoutManager(context, mColumnCount);
			}
			mRecyclerView.setLayoutManager(mLayoutManager);

			mAdapter = new ConferenceRecyclerViewAdapter(mListener);
			mRecyclerView.setAdapter(mAdapter);
			mDisposable.add(getViewModel().getConferencesByGroup(mGroupName)
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(conferences -> {
						mAdapter.setItems(conferences);
						if(savedInstanceState != null){
							mLayoutManager.onRestoreInstanceState(
									savedInstanceState.getParcelable(LAYOUTMANAGER_STATE));
						}
					}));
		}
		return view;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof ConferencesTabBrowseFragment.OnConferenceListFragmentInteractionListener) {
			mListener = (ConferencesTabBrowseFragment.OnConferenceListFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnListFragmentInteractionListener");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(LAYOUTMANAGER_STATE,mLayoutManager.onSaveInstanceState());
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
}
