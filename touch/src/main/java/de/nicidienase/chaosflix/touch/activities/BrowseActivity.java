package de.nicidienase.chaosflix.touch.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.view.View;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.common.entities.recording.Event;
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
								FragmentManager fm = getSupportFragmentManager();
								Fragment oldFragment = fm.findFragmentById(R.id.fragment_container);

//								TransitionInflater transitionInflater = TransitionInflater.from(this);
//								oldFragment.setExitTransition(
//										transitionInflater.inflateTransition(android.R.transition.fade));
//								fragment.setEnterTransition(
//										transitionInflater.inflateTransition(android.R.transition.slide_right));
//
//								Slide slideTransition = new Slide(Gravity.RIGHT);
//								slideTransition.setDuration(1000);
//								fragment.setEnterTransition(slideTransition);
//
//								Transition changeBoundsTransition = TransitionInflater.from(this).inflateTransition(R.transition.change_bounds);
//								fragment.setSharedElementEnterTransition(changeBoundsTransition);

								FragmentTransaction ft = fm.beginTransaction();
								ft.replace(R.id.fragment_container, eventsFragment);
								ft.addToBackStack(null);
								ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
								ft.commit();
							});
				});
		mDisposables.add(disposable);
	}

	@Override
	public void onEventSelected(Event e, View v) {
		Disposable disposable = getApiServiceObservable()
				.subscribe(mediaApiService -> {
					mediaApiService.getEvent(e.getApiID())
							.subscribe(event -> {
								EventDetailsFragment detailsFragment = EventDetailsFragment.newInstance(event);
								FragmentManager fm = getSupportFragmentManager();

								FragmentTransaction ft = fm.beginTransaction();
								ft.replace(R.id.fragment_container, detailsFragment);
								ft.addToBackStack(null);

								View title = v.findViewById(R.id.title_text);
								View subtitle = v.findViewById(R.id.acronym_text);
								View thumb = v.findViewById(R.id.imageView);
								ft.addSharedElement(title,ViewCompat.getTransitionName(title));
								ft.addSharedElement(subtitle,ViewCompat.getTransitionName(subtitle));
								ft.addSharedElement(thumb,ViewCompat.getTransitionName(thumb));
								ft.commit();
							});
				});
		mDisposables.add(disposable);
	}

}
