package de.nicidienase.chaosflix.touch.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.common.entities.recording.Event;
import de.nicidienase.chaosflix.common.entities.recording.Metadata;
import de.nicidienase.chaosflix.common.network.MediaApiService;
import de.nicidienase.chaosflix.touch.fragments.ConferencesTabBrowseFragment;
import de.nicidienase.chaosflix.touch.fragments.EventDetailsFragment;
import de.nicidienase.chaosflix.touch.fragments.EventsFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by felix on 17.09.17.
 */

public class BrowseActivity extends TouchBaseActivity implements
		ConferencesTabBrowseFragment.OnConferenceListFragmentInteractionListener,
		EventsFragment.OnEventsListFragmentInteractionListener,
		EventDetailsFragment.OnEventDetailsFragmentInteractionListener{

	private static final String TAG = BrowseActivity.class.getSimpleName();
	private static final String TAG_RETAINED_FRAGMENT = "retained_fragment";
	CompositeDisposable mDisposables = new CompositeDisposable();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_container_layout);
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment fragment = null;
		if(savedInstanceState != null){
			fragment = fragmentManager.getFragment(savedInstanceState,TAG_RETAINED_FRAGMENT);
		}
		if(fragment != null){
			FragmentTransaction ft = fragmentManager.beginTransaction();
			ft.replace(R.id.fragment_container,fragment,TAG_RETAINED_FRAGMENT);
			ft.commit();
		} else {
			Disposable disposable = getApiServiceObservable()
					.subscribe(mediaApiService -> {
						mediaApiService.getConferences()
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(conferencesWrapper -> {
									ConferencesTabBrowseFragment browseFragment
									= ConferencesTabBrowseFragment.newInstance(getNumColumns());
									browseFragment.setContent(conferencesWrapper);
									FragmentTransaction ft = fragmentManager.beginTransaction();
									ft.replace(R.id.fragment_container,browseFragment,TAG_RETAINED_FRAGMENT);
									ft.commit();
					});
		});
			mDisposables.add(disposable);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment fragment = fragmentManager.findFragmentByTag(TAG_RETAINED_FRAGMENT);
		if(fragment != null){
			fragmentManager.putFragment(outState,TAG_RETAINED_FRAGMENT,fragment);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		mDisposables.clear();
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

								TransitionInflater transitionInflater = TransitionInflater.from(this);
								oldFragment.setExitTransition(
										transitionInflater.inflateTransition(android.R.transition.fade));
								eventsFragment.setEnterTransition(
										transitionInflater.inflateTransition(android.R.transition.slide_right));

								Slide slideTransition = new Slide(Gravity.RIGHT);
//								slideTransition.setDuration(1000);
								eventsFragment.setEnterTransition(slideTransition);

								FragmentTransaction ft = fm.beginTransaction();
								ft.replace(R.id.fragment_container, eventsFragment,TAG_RETAINED_FRAGMENT);
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

								detailsFragment.setAllowEnterTransitionOverlap(true);
								detailsFragment.setAllowReturnTransitionOverlap(true);

								FragmentTransaction ft = fm.beginTransaction();
								ft.replace(R.id.fragment_container, detailsFragment,TAG_RETAINED_FRAGMENT);
								ft.addToBackStack(null);

								View thumb = v.findViewById(R.id.imageView);
								ft.addSharedElement(thumb,ViewCompat.getTransitionName(thumb));

								ft.commit();
							});
				});
		mDisposables.add(disposable);
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

//	public List<Event> getRelatedEvents(Event event, MediaApiService service){
//		List<Event> results = new ArrayList<>();
//		Metadata metadata = event.getMetadata();
//		if(metadata != null && metadata.getRelated() != null){
//			Observable<Event> eventObservable = null;
//			for(long id: metadata.getRelated()){
//				if(eventObservable == null){
//					eventObservable = service.getEvent(id);
//				} else {
//					eventObservable.mergeWith(service.getEvent(id));
//				}
//			}
//			eventObservable.subscribe()
//		}
//		return results;
//	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.browse_menu,menu);
		return true;
	}
}
