package de.nicidienase.chaosflix.touch.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.common.entities.recording.Event;
import de.nicidienase.chaosflix.touch.adapters.EventRecyclerViewAdapter;
import de.nicidienase.chaosflix.touch.adapters.ItemRecyclerViewAdapter;

public class EventsFragment extends Fragment {


	private static final String ARG_COLUMN_COUNT = "column-count";
	private int mColumnCount = 1;
	private ItemRecyclerViewAdapter.OnListFragmentInteractionListener mListener;
	private Conference mConference;
	private CharSequence mPreviousTitle;
	private ActionBar mActionBar;

	public EventsFragment() {
	}

	public void setContent(Conference conference){
		mConference = conference;
	}

	public static EventsFragment newInstance(int columnCount) {
		EventsFragment fragment = new EventsFragment();
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
		View view = inflater.inflate(R.layout.recycler_view_layout, container, false);

		// Set the adapter
		if (view instanceof RecyclerView) {
			Context context = view.getContext();
			RecyclerView recyclerView = (RecyclerView) view;
			if (mColumnCount <= 1) {
				recyclerView.setLayoutManager(new LinearLayoutManager(context));
			} else {
				recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
			}

			recyclerView.setAdapter(new EventRecyclerViewAdapter(mConference, mListener) {
			});
		}
		return view;
	}


	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mActionBar = ((AppCompatActivity) context).getSupportActionBar();
		mPreviousTitle = mActionBar.getTitle();
		mActionBar.setTitle(mConference.getTitle());
		if (context instanceof ItemRecyclerViewAdapter.OnListFragmentInteractionListener) {
			mListener = (ItemRecyclerViewAdapter.OnListFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnListFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
		mActionBar.setTitle(mPreviousTitle);
	}


}
