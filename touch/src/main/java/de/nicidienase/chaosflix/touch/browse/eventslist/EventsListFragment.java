package de.nicidienase.chaosflix.touch.browse.eventslist;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import de.nicidienase.chaosflix.common.mediadata.MediaRepository;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event;
import de.nicidienase.chaosflix.touch.OnEventSelectedListener;
import de.nicidienase.chaosflix.touch.R;
import de.nicidienase.chaosflix.touch.browse.BrowseFragment;
import de.nicidienase.chaosflix.touch.browse.adapters.EventRecyclerViewAdapter;
import de.nicidienase.chaosflix.touch.databinding.FragmentEventsListBinding;
import kotlin.Unit;

public class EventsListFragment extends BrowseFragment implements SearchView.OnQueryTextListener {

	private static final String ARG_COLUMN_COUNT    = "column-count";
	private static final String ARG_TYPE            = "type";
	private static final String ARG_CONFERENCE      = "conference";
	private static final String LAYOUTMANAGER_STATE = "layoutmanager-state";
	private static final String TAG                 = EventsListFragment.class.getSimpleName();

	public static final int TYPE_EVENTS      = 0;
	public static final int TYPE_BOOKMARKS   = 1;
	public static final int TYPE_IN_PROGRESS = 2;

	private int                     columnCount = 1;
	private OnEventSelectedListener listener;

	private EventRecyclerViewAdapter eventAdapter;
	private Conference               conference;

	private RecyclerView.LayoutManager layoutManager;
	private Snackbar            snackbar;
	private int                 type;

	public static EventsListFragment newInstance(int type, Conference conference, int columnCount) {
		EventsListFragment fragment = new EventsListFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_TYPE, type);
		args.putInt(ARG_COLUMN_COUNT, columnCount);
		args.putParcelable(ARG_CONFERENCE, conference);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		setHasOptionsMenu(true);
		if (context instanceof OnEventSelectedListener) {
			listener = (OnEventSelectedListener) context;
		} else {
			throw new RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			columnCount = getArguments().getInt(ARG_COLUMN_COUNT);
			type = getArguments().getInt(ARG_TYPE);
			conference = getArguments().getParcelable(ARG_CONFERENCE);
		}
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		FragmentEventsListBinding binding = FragmentEventsListBinding.inflate(inflater, container, false);

		AppCompatActivity activity = (AppCompatActivity) requireActivity();
		activity.setSupportActionBar(binding.incToolbar.toolbar);
		setOverlay(binding.incOverlay.loadingOverlay);

		if (columnCount <= 1) {
			layoutManager = new LinearLayoutManager(getContext());
		} else {
			layoutManager = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
		}
		binding.list.setLayoutManager(layoutManager);

		eventAdapter = new EventRecyclerViewAdapter(event -> {
			listener.onEventSelected(event);
			return Unit.INSTANCE;
		});
		eventAdapter.setHasStableIds(true);
		binding.list.setAdapter(eventAdapter);

		Observer<List<Event>> listObserver = persistentEvents -> {
			setLoadingOverlayVisibility(false);
			if (persistentEvents != null) {
				setEvents(persistentEvents);
			}
		};

		if (type == TYPE_BOOKMARKS) {
			setupToolbar(binding.incToolbar.toolbar, R.string.bookmarks);
			eventAdapter.setShowConferenceName(true);
			getViewModel().getBookmarkedEvents().observe(this, listObserver);
		} else if (type == TYPE_IN_PROGRESS) {
			setupToolbar(binding.incToolbar.toolbar, R.string.continue_watching);
			eventAdapter.setShowConferenceName(true);
			getViewModel().getInProgressEvents().observe(this, listObserver);
		} else if (type == TYPE_EVENTS) {
			{
				setupToolbar(binding.incToolbar.toolbar, conference.getTitle(), false);
				getViewModel().getEventsforConference(conference).observe(this, events -> {
					if(events != null){
						setEvents(events);
						setLoadingOverlayVisibility(false);
					}
				});
				getViewModel().updateEventsForConference(conference).observe(this, state -> {
					MediaRepository.State downloaderState = state.getState();
					switch (downloaderState) {
						case RUNNING:
							setLoadingOverlayVisibility(true);
							break;
						case DONE:
							setLoadingOverlayVisibility(false);
							break;
					}
					if (state.getError() != null) {
						showSnackbar(state.getError());
					}
				});
			}
		}
		return binding.getRoot();
	}

	private void showSnackbar(String message) {
		if (snackbar != null) {
			snackbar.dismiss();
		}
		snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_LONG);
		snackbar.setAction("Okay", view -> snackbar.dismiss());
		snackbar.show();
	}

	private void setEvents(List<Event> events) {
		eventAdapter.setItems(events);

		Parcelable layoutState = getArguments().getParcelable(LAYOUTMANAGER_STATE);
		if (layoutState != null) { layoutManager.onRestoreInstanceState(layoutState); }
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
		SearchView searchView = (SearchView) searchMenuItem.getActionView();
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
}
