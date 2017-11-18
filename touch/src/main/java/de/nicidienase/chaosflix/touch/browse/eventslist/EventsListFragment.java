package de.nicidienase.chaosflix.touch.browse.eventslist;

import android.app.Activity;
import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import java.util.List;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent;
import de.nicidienase.chaosflix.touch.OnEventSelectedListener;
import de.nicidienase.chaosflix.touch.browse.BrowseFragment;
import de.nicidienase.chaosflix.touch.browse.adapters.EventRecyclerViewAdapter;

public class EventsListFragment extends BrowseFragment implements SearchView.OnQueryTextListener {

	private static final String ARG_COLUMN_COUNT = "column-count";
	private static final String ARG_CONFERENCE = "conference";
	private static final String LAYOUTMANAGER_STATE = "layoutmanager-state";
	private static final String TAG = EventsListFragment.class.getSimpleName();
	public static final long BOOKMARKS_LIST_ID = -1;
	public static final long IN_PROGRESS_LIST_ID = -2;

	private int columnCount = 1;
	private OnInteractionListener listener;

	private EventRecyclerViewAdapter eventAdapter;
	private long conferenceId;

	private LinearLayoutManager layoutManager;
	private SearchView searchView;

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
		if (context instanceof OnInteractionListener) {
			listener = (OnInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			columnCount = getArguments().getInt(ARG_COLUMN_COUNT);
			conferenceId = getArguments().getLong(ARG_CONFERENCE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_events_list, container, false);
		Context context = view.getContext();
		RecyclerView recyclerView = view.findViewById(R.id.list);
		if (columnCount <= 1) {
			layoutManager = new LinearLayoutManager(context);
		} else {
			layoutManager = new GridLayoutManager(context, columnCount);
		}
		recyclerView.setLayoutManager(layoutManager);

		eventAdapter = new EventRecyclerViewAdapter(listener);
		recyclerView.setAdapter(eventAdapter);

		Observer<List<PersistentEvent>> listObserver = persistentEvents -> {
			setEvents(persistentEvents);
		};

		Resources resources = getResources();
		if (conferenceId == BOOKMARKS_LIST_ID) {
			listener.setToolbarTitle(resources.getString(R.string.bookmarks));
			getViewModel().getBookmarkedEvents().observe(this, listObserver);
		} else if (conferenceId == IN_PROGRESS_LIST_ID) {
			listener.setToolbarTitle(resources.getString(R.string.continue_watching));
			getViewModel().getInProgressEvents().observe(this, listObserver);
		} else {
			{
				getViewModel().getConference(conferenceId).observe(this, conference -> {
					if (conference != null) {
						listener.setToolbarTitle(conference.getTitle());
						eventAdapter.setShowTags(conference.getTagsUsefull());
					}
				});

				getViewModel().getEventsforConference(conferenceId).observe(this, listObserver);
			}
		}
		return view;
	}

	private void setEvents(List<PersistentEvent> persistentEvents) {
		eventAdapter.setItems(persistentEvents);

		Parcelable layoutState = getArguments().getParcelable(LAYOUTMANAGER_STATE);
		if (layoutState != null)
			layoutManager.onRestoreInstanceState(layoutState);
	}

	@Override
	public void onPause() {
		super.onPause();
		getArguments().putParcelable(LAYOUTMANAGER_STATE, layoutManager.onSaveInstanceState());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.events_menu, menu);

		MenuItem searchMenuItem = menu.findItem(R.id.search);
		searchView = (SearchView) searchMenuItem.getActionView();
		SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

		searchView.setSearchableInfo(searchManager.
				getSearchableInfo(getActivity().getComponentName()));
		searchView.setSubmitButtonEnabled(true);
		searchView.setIconified(false);
		searchView.setOnQueryTextListener(this);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		eventAdapter.getFilter().filter(newText);
		return true;
	}

	public interface OnInteractionListener extends OnEventSelectedListener {
		void setToolbarTitle(String title);
	}

}
