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
import de.nicidienase.chaosflix.touch.fragments.EventsFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by felix on 17.09.17.
 */

public class BrowseActivity extends TouchBaseActivity implements ItemRecyclerViewAdapter.OnListFragmentInteractionListener{

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

	@Override
	public void onListFragmentInteraction(Object item) {
		if(item instanceof Conference){
			Conference con = (Conference) item;
			Disposable disposable = getApiServiceObservable()
					.subscribe(mediaApiService -> {
						mediaApiService.getConference(con.getApiID())
								.observeOn(AndroidSchedulers.mainThread())
								.subscribe(conference -> {
									EventsFragment eventsFragment = EventsFragment.newInstance(getNumColumns());
									eventsFragment.setContent(conference);
									FragmentManager fm = getSupportFragmentManager();
									Fragment oldFragment = fm.findFragmentById(R.id.fragment_container);

									TransitionInflater transitionInflater = TransitionInflater.from(this);
									oldFragment.setExitTransition(
											transitionInflater.inflateTransition(android.R.transition.fade));
									eventsFragment.setEnterTransition(
											transitionInflater.inflateTransition(android.R.transition.slide_right));
									eventsFragment.setExitTransition(
											transitionInflater.inflateTransition(android.R.transition.slide_right));

									FragmentTransaction ft = fm.beginTransaction();
									ft.replace(R.id.fragment_container,eventsFragment);
									ft.addToBackStack(null);
									ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
									ft.commit();
								});
					});
			mDisposables.add(disposable);
		} else if (item instanceof Event){
			Event event = (Event) item;
			// TODO show event details
		}
	}

	private int getNumColumns() {
		return getResources().getInteger(R.integer.num_columns);
	}
}
