package de.nicidienase.chaosflix.touch.activities;

import android.app.SearchManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent;
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentRecording;
import de.nicidienase.chaosflix.touch.ViewModelFactory;
import de.nicidienase.chaosflix.touch.fragments.ConferencesTabBrowseFragment;
import de.nicidienase.chaosflix.touch.fragments.EventDetailsFragment;
import de.nicidienase.chaosflix.touch.fragments.EventsListFragment;
import de.nicidienase.chaosflix.touch.viewmodels.BrowseViewModel;

public class BrowseActivity extends AppCompatActivity implements
		ConferencesTabBrowseFragment.OnConferenceListFragmentInteractionListener,
		EventsListFragment.OnEventsListFragmentInteractionListener,
		EventDetailsFragment.OnEventDetailsFragmentInteractionListener {

	private static final String TAG = BrowseActivity.class.getSimpleName();
	private BrowseViewModel viewModel;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_container_layout);

		if (savedInstanceState == null) {
			ConferencesTabBrowseFragment browseFragment
					= ConferencesTabBrowseFragment.newInstance(getNumColumns());
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.fragment_container, browseFragment);
//			ft.setReorderingAllowed(true);
			ft.commit();
		}

		viewModel = ViewModelProviders.of(this, ViewModelFactory.INSTANCE).get(BrowseViewModel.class);
	}

	private int getNumColumns() {
		return getResources().getInteger(R.integer.num_columns);
	}

	private void showBookmarksFragment() {
		EventsListFragment bookmarksFragment = EventsListFragment.newInstance(EventsListFragment.BOOKMARKS_LIST_ID, getNumColumns());
		showEventsFragment(bookmarksFragment);
	}

	@Override
	public void onConferenceSelected(long conferenceId) {
		EventsListFragment eventsListFragment = EventsListFragment.newInstance(conferenceId, getNumColumns());
		showEventsFragment(eventsListFragment);
	}

	private void showEventsFragment(EventsListFragment eventsListFragment) {
		FragmentManager fm = getSupportFragmentManager();
		Fragment oldFragment = fm.findFragmentById(R.id.fragment_container);

		TransitionInflater transitionInflater = TransitionInflater.from(this);
		oldFragment.setExitTransition(
				transitionInflater.inflateTransition(android.R.transition.fade));
		eventsListFragment.setEnterTransition(
				transitionInflater.inflateTransition(android.R.transition.slide_right));

		Slide slideTransition = new Slide(Gravity.RIGHT);
		eventsListFragment.setEnterTransition(slideTransition);

		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.fragment_container, eventsListFragment);
		ft.setReorderingAllowed(true);
		ft.addToBackStack(null);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();
	}

	@Override
	public void onEventSelected(PersistentEvent event, View v) {
		EventDetailsFragment detailsFragment = EventDetailsFragment.newInstance(event.getEventId());
		FragmentManager fm = getSupportFragmentManager();

		detailsFragment.setAllowEnterTransitionOverlap(true);
		detailsFragment.setAllowReturnTransitionOverlap(true);

		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.fragment_container, detailsFragment);
		ft.addToBackStack(null);
		ft.setReorderingAllowed(true);

		View thumb = v.findViewById(R.id.imageView);
		ft.addSharedElement(thumb, ViewCompat.getTransitionName(thumb));

		ft.commit();
	}

	@Override
	public void onToolbarStateChange() {
		invalidateOptionsMenu();
	}

	@Override
	public void setActionbar(Toolbar toolbar) {
		setSupportActionBar(toolbar);
		toolbar.setTitle("");
	}

	@Override
	public void playItem(PersistentEvent event, PersistentRecording recording) {
		Intent i = new Intent(this, PlayerActivity.class);
		i.putExtra(PlayerActivity.EVENT_KEY, event);
		i.putExtra(PlayerActivity.RECORDING_KEY, recording);
		startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.browse_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_show_bookmarks:
				showBookmarksFragment();
				return true;
			case R.id.action_show_inprogress:
				showInProgressFragment();
				return true;
			case R.id.action_about:
				showAboutPage();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void showInProgressFragment() {
		EventsListFragment bookmarksFragment = EventsListFragment.newInstance(EventsListFragment.IN_PROGRESS_LIST_ID, getNumColumns());
		showEventsFragment(bookmarksFragment);
	}

	private void showAboutPage() {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}
}
