package de.nicidienase.chaosflix.touch.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Event;
import de.nicidienase.chaosflix.touch.adapters.EventRecyclerViewAdapter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class EventsListFragment extends ChaosflixFragment {

	private static final String ARG_COLUMN_COUNT = "column-count";
	private static final String ARG_CONFERENCE = "conference";
	private static final String LAYOUTMANAGER_STATE = "layoutmanager-state";
	private static final String TAG = EventsListFragment.class.getSimpleName();

	private int mColumnCount = 1;
	private OnEventsListFragmentInteractionListener mListener;

	private Toolbar mToolbar;
	private Context mContext;
	private EventRecyclerViewAdapter mAdapter;
	private long mConferenceId;

	CompositeDisposable mDisposable = new CompositeDisposable();
	private LinearLayoutManager layoutManager;

	public static EventsListFragment newInstance(long conferenceId, int columnCount) {
		EventsListFragment fragment = new EventsListFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_COLUMN_COUNT, columnCount);
		args.putLong(ARG_CONFERENCE, conferenceId);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		setHasOptionsMenu(true);
		mContext = context;
		if (context instanceof OnEventsListFragmentInteractionListener) {
			mListener = (OnEventsListFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnListFragmentInteractionListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
			mConferenceId = getArguments().getLong(ARG_CONFERENCE);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getArguments().putParcelable(LAYOUTMANAGER_STATE, layoutManager.onSaveInstanceState());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.recycler_view_toolbar_layout, container, false);
		// Set the adapter
		Context context = view.getContext();
		RecyclerView recyclerView = view.findViewById(R.id.list);
		if (mColumnCount <= 1) {
			layoutManager = new LinearLayoutManager(context);
		} else {
			layoutManager = new GridLayoutManager(context, mColumnCount);
		}
		recyclerView.setLayoutManager(layoutManager);

		mAdapter = new EventRecyclerViewAdapter(mListener);
		recyclerView.setAdapter(mAdapter);

		mToolbar = view.findViewById(R.id.toolbar);
		((AppCompatActivity)mContext).setSupportActionBar(mToolbar);

		mDisposable.add(getViewModel().getConference(mConferenceId)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(conference -> {
					Parcelable layoutState = getArguments().getParcelable(LAYOUTMANAGER_STATE);
					if(layoutState != null){
						layoutManager.onRestoreInstanceState(layoutState);
					}
					mAdapter.setItems(conference);
					mToolbar.setTitle(conference.getTitle());
				}, throwable -> Snackbar.make(container,throwable.getMessage(),Snackbar.LENGTH_INDEFINITE).show()));
		return view;
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if(savedInstanceState != null){
			layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(LAYOUTMANAGER_STATE));
			Log.d(TAG,"Layout State restored");
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.events_menu,menu);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(LAYOUTMANAGER_STATE,layoutManager.onSaveInstanceState());
		Log.d(TAG, "Layout state saved");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onStop() {
		super.onStop();
		mDisposable.clear();
	}

	public interface OnEventsListFragmentInteractionListener{
		void onEventSelected(Event event, View v);
	}

}
