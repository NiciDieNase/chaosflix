package de.nicidienase.chaosflix.touch.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.transition.TransitionInflater;
import android.util.Log;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.common.entities.recording.Event;
import de.nicidienase.chaosflix.touch.adapters.ItemRecyclerViewAdapter;
import de.nicidienase.chaosflix.touch.ConferenceGroupsFragmentPager;
import de.nicidienase.chaosflix.touch.fragments.ConferencesBrowseFragment;
import de.nicidienase.chaosflix.touch.fragments.EventDetailsFragment;
import de.nicidienase.chaosflix.touch.fragments.EventsFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by felix on 17.09.17.
 */

public class BrowseActivity extends TouchBaseActivity implements
		ConferencesBrowseFragment.OnConferenceListFragmentInteractionListener,
		EventsFragment.OnEventsListFragmentInteractionListener,
		EventDetailsFragment.OnEventDetailsFragmentInteractionListener{

	private static final String TAG = BrowseActivity.class.getSimpleName();
	CompositeDisposable mDisposables = new CompositeDisposable();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_container_layout);
		Disposable disposable = getApiServiceObservable()
				.subscribe(mediaApiService -> {
					mediaApiService.getConferences()
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(conferencesWrapper -> {
								ConferencesBrowseFragment browseFragment
										= ConferencesBrowseFragment.newInstance(getNumColumns());
								browseFragment.setContent(conferencesWrapper);
								FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
								ft.replace(R.id.fragment_container,browseFragment);
								ft.commit();
							});
				});
		mDisposables.add(disposable);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mDisposables.dispose();
	}

	private int getNumColumns() {
		return getResources().getInteger(R.integer.num_columns);
	}

	@Override
	public void onConferenceSelected(Conference con) {
		Disposable disposable = getApiServiceObservable()
				.subscribe(mediaApiService -> {
					mediaApiService.getConference(con.getApiID())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(conference -> {
								EventsFragment eventsFragment = EventsFragment.newInstance(conference,getNumColumns());
								updateFragment(eventsFragment);
							});
				});
		mDisposables.add(disposable);
	}

	@Override
	public void onEventSelected(Event e) {
		Disposable disposable = getApiServiceObservable()
				.subscribe(mediaApiService -> {
					mediaApiService.getEvent(e.getApiID())
							.subscribe(event -> {
								EventDetailsFragment detailsFragment = EventDetailsFragment.newInstance(event);
								updateFragment(detailsFragment);
							});
				});
		mDisposables.add(disposable);
	}

	private void updateFragment(Fragment fragment) {
		FragmentManager fm = getSupportFragmentManager();
		Fragment oldFragment = fm.findFragmentById(R.id.fragment_container);

		TransitionInflater transitionInflater = TransitionInflater.from(this);
		oldFragment.setExitTransition(
				transitionInflater.inflateTransition(android.R.transition.fade));
		fragment.setEnterTransition(
				transitionInflater.inflateTransition(android.R.transition.slide_right));

		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.fragment_container, fragment);
		ft.addToBackStack(null);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();
	}
}
