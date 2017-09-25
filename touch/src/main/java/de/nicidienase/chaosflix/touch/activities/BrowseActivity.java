package de.nicidienase.chaosflix.touch.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.common.entities.recording.Event;
import de.nicidienase.chaosflix.common.entities.recording.Recording;
import de.nicidienase.chaosflix.touch.ChaosflixViewModel;
import de.nicidienase.chaosflix.touch.fragments.ConferencesTabBrowseFragment;
import de.nicidienase.chaosflix.touch.fragments.EventDetailsFragment;
import de.nicidienase.chaosflix.touch.fragments.EventsFragment;
import de.nicidienase.chaosflix.touch.fragments.ExoPlayerFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by felix on 17.09.17.
 */

public class BrowseActivity extends AppCompatActivity implements
		ConferencesTabBrowseFragment.OnConferenceListFragmentInteractionListener,
		EventsFragment.OnEventsListFragmentInteractionListener,
		EventDetailsFragment.OnEventDetailsFragmentInteractionListener,
		ExoPlayerFragment.OnMediaPlayerInteractionListener {

	private static final String TAG = BrowseActivity.class.getSimpleName();
	CompositeDisposable mDisposables = new CompositeDisposable();
	private ChaosflixViewModel mViewModel;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_container_layout);

		Resources res = getResources();
		ChaosflixViewModel.Factory factory =
				new ChaosflixViewModel.Factory(
						res.getString(R.string.api_media_ccc_url),
						res.getString(R.string.streaming_media_ccc_url));
		mViewModel = ViewModelProviders.of(this,factory).get(ChaosflixViewModel.class);

		if(savedInstanceState == null){
			ConferencesTabBrowseFragment browseFragment
					= ConferencesTabBrowseFragment.newInstance(getNumColumns());
//			mViewModel.getConferencesWrapperAsLiveData().observe(browseFragment,conferencesWrapper -> {
//				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//				ft.replace(R.id.fragment_container,browseFragment);
//				ft.setReorderingAllowed(true);
//				ft.commit();
//			});
			mDisposables.add(mViewModel.getConferencesWrapper()
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(conferencesWrapper -> {
						FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
						ft.replace(R.id.fragment_container,browseFragment);
						ft.setReorderingAllowed(true);
						ft.commit();
					}, throwable -> Snackbar.make(findViewById(R.id.fragment_container),throwable.getMessage(),Snackbar.LENGTH_INDEFINITE).show()));
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
		mDisposables.add(mViewModel.getConference(con.getApiID())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(conference -> {
					EventsFragment eventsFragment = EventsFragment.newInstance(conference.getApiID(),getNumColumns());
					FragmentManager fm = getSupportFragmentManager();
					Fragment oldFragment = fm.findFragmentById(R.id.fragment_container);

					TransitionInflater transitionInflater = TransitionInflater.from(this);
					oldFragment.setExitTransition(
							transitionInflater.inflateTransition(android.R.transition.fade));
					eventsFragment.setEnterTransition(
							transitionInflater.inflateTransition(android.R.transition.slide_right));

					Slide slideTransition = new Slide(Gravity.RIGHT);
					eventsFragment.setEnterTransition(slideTransition);

					FragmentTransaction ft = fm.beginTransaction();
					ft.replace(R.id.fragment_container, eventsFragment);
					ft.setReorderingAllowed(true);
					ft.addToBackStack(null);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					ft.commit();
				}));
	}

	@Override
	public void onEventSelected(Event e, View v) {
		mDisposables.add(mViewModel.getEvent(e.getApiID())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(event -> {

					EventDetailsFragment detailsFragment = EventDetailsFragment.newInstance(event);
					FragmentManager fm = getSupportFragmentManager();

					detailsFragment.setAllowEnterTransitionOverlap(true);
					detailsFragment.setAllowReturnTransitionOverlap(true);

					FragmentTransaction ft = fm.beginTransaction();
					ft.replace(R.id.fragment_container, detailsFragment);
					ft.addToBackStack(null);
					ft.setReorderingAllowed(true);

					View thumb = v.findViewById(R.id.imageView);
					ft.addSharedElement(thumb,ViewCompat.getTransitionName(thumb));

					ft.commit();
				}));
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
	public void playItem(Event event, Recording recording) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		Fragment playerFragment = ExoPlayerFragment.newInstance(event,recording);
		ft.replace(R.id.fragment_container,playerFragment);
		ft.addToBackStack(null);
		ft.commit();
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.browse_menu,menu);
		return true;
	}
}
