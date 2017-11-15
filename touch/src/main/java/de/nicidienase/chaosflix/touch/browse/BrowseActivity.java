package de.nicidienase.chaosflix.touch.browse;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent;
import de.nicidienase.chaosflix.touch.OnEventSelectedListener;
import de.nicidienase.chaosflix.touch.activities.AboutActivity;
import de.nicidienase.chaosflix.touch.eventdetails.EventDetailsActivity;

public class BrowseActivity extends AppCompatActivity implements
		ConferencesTabBrowseFragment.OnConferenceListFragmentInteractionListener,
		EventsListFragment.OnEventsListFragmentInteractionListener,
		OnEventSelectedListener {

	private static final String TAG = BrowseActivity.class.getSimpleName();
	private Toolbar toolbar;
	private ActionBarDrawerToggle drawerToggle;
	private DrawerLayout drawerLayout;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse);

		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		drawerLayout = findViewById(R.id.drawer_layout);
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
		drawerLayout.addDrawerListener(drawerToggle);
		drawerToggle.syncState();

		resetToolbar();

		NavigationView navigationView = findViewById(R.id.navigation_view);
		navigationView.setNavigationItemSelectedListener(item -> {
			switch (item.getItemId()) {
				case R.id.nav_recordings:
					showConferencesFragment();
					break;
				case R.id.nav_bookmarks:
					showBookmarksFragment();
					break;
				case R.id.nav_inprogress:
					showInProgressFragment();
					break;
				case R.id.nav_about:
					showAboutPage();
					break;
				case R.id.nav_streams:
				case R.id.nav_preferences:
				default:
					Snackbar.make(drawerLayout, "Not implemented yet", Snackbar.LENGTH_SHORT).show();
					break;
			}
			drawerLayout.closeDrawers();
			return true;
		});

		if (savedInstanceState == null) {
			showConferencesFragment();
		}
	}

	@Override
	public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
		super.onPostCreate(savedInstanceState, persistentState);
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.browse_menu, menu);
//		return true;
//	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
			case android.R.id.home:
				drawerLayout.openDrawer(GravityCompat.START);
				return true;
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

	private int getNumColumns() {
		return getResources().getInteger(R.integer.num_columns);
	}

	private int showConferencesFragment() {
		resetToolbar();
		return getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.fragment_container, ConferencesTabBrowseFragment.newInstance(getNumColumns()))
				.commit();
	}

	private void resetToolbar() {
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setTitle(R.string.app_name);
//		getSupportActionBar().setLogo(R.drawable.icon_notext);
//		getSupportActionBar().setDisplayUseLogoEnabled(true);

	}

	@Override
	public void onConferenceSelected(long conferenceId) {
		EventsListFragment eventsListFragment = EventsListFragment.newInstance(conferenceId, getNumColumns());
		showEventsFragment(eventsListFragment);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private void showBookmarksFragment() {
		EventsListFragment bookmarksFragment = EventsListFragment.newInstance(EventsListFragment.BOOKMARKS_LIST_ID, getNumColumns());
		showEventsFragment(bookmarksFragment);
	}

	private void showInProgressFragment() {
		EventsListFragment progressEventsFragment = EventsListFragment.newInstance(EventsListFragment.IN_PROGRESS_LIST_ID, getNumColumns());
		showEventsFragment(progressEventsFragment);
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
		resetToolbar();
	}

	@Override
	public void onEventSelected(PersistentEvent event, View v) {
		EventDetailsActivity.Companion.launch(this, event.getEventId());
//		EventDetailsFragment detailsFragment = EventDetailsFragment.Companion.newInstance(event.getEventId());
//		FragmentManager fm = getSupportFragmentManager();
//
//		detailsFragment.setAllowEnterTransitionOverlap(true);
//		detailsFragment.setAllowReturnTransitionOverlap(true);
//
//		FragmentTransaction ft = fm.beginTransaction();
//		ft.replace(R.id.fragment_container, detailsFragment);
//		ft.addToBackStack(null);
//		ft.setReorderingAllowed(true);
//
//		View thumb = v.findViewById(R.id.imageView);
//		ft.addSharedElement(thumb, ViewCompat.getTransitionName(thumb));
//
//		ft.commit();
	}

	@Override
	public void setToolbarTitle(String title) {
		toolbar.setTitle(title);
	}

	private void showAboutPage() {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		resetToolbar();
		super.onBackPressed();
	}
}
