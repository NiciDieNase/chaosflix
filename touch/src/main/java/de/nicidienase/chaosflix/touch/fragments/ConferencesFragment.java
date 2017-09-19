package de.nicidienase.chaosflix.touch.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.touch.adapters.ConferenceRecyclerViewAdapter;
import de.nicidienase.chaosflix.touch.adapters.ItemRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class ConferencesFragment extends Fragment {


	private static final String ARG_COLUMN_COUNT = "column-count";
	private int mColumnCount = 1;
	private ItemRecyclerViewAdapter.OnListFragmentInteractionListener<Conference> mListener;
	private List<Conference> mItmes = new ArrayList<>();

	public ConferencesFragment() {
	}

	public void setContent(List<Conference> itmes){
		mItmes = itmes;
	}

	public static ConferencesFragment newInstance(int columnCount) {
		ConferencesFragment fragment = new ConferencesFragment();
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

			recyclerView.setAdapter(new ConferenceRecyclerViewAdapter(mItmes, mListener) {
			});
		}
		return view;
	}


	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof ItemRecyclerViewAdapter.OnListFragmentInteractionListener) {
			mListener = (ItemRecyclerViewAdapter.OnListFragmentInteractionListener<Conference>) context;
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


}
