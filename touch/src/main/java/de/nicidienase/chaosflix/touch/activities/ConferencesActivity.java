package de.nicidienase.chaosflix.touch.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.touch.adapters.ItemRecyclerViewAdapter;
import de.nicidienase.chaosflix.touch.ConferenceGroupsFragmentPager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by felix on 17.09.17.
 */

public class ConferencesActivity extends TouchBaseActivity implements ItemRecyclerViewAdapter.OnListFragmentInteractionListener<Conference>{

	private static final String TAG = ConferencesActivity.class.getSimpleName();
	CompositeDisposable mDisposables = new CompositeDisposable();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.touch_browse);
		setContentView(R.layout.tab_layout);
		Disposable disposable = getApiServiceObservable()
				.subscribe(mediaApiService -> {
					mediaApiService.getConferences()
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(conferencesWrapper -> {
								ConferenceGroupsFragmentPager fragmentPager
										= new ConferenceGroupsFragmentPager(ConferencesActivity.this,getSupportFragmentManager());
								fragmentPager.setContent(conferencesWrapper.getConferencesBySeries());

								ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
								pager.setAdapter(fragmentPager);

								TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
								tabLayout.setupWithViewPager(pager);
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
	public void onListFragmentInteraction(Conference item) {
		Log.d(TAG,"Clicked " + item.getAcronym());
		Intent intent = new Intent(this, EventsActivity.class);
		intent.putExtra(EventsActivity.CONFERENCE_KEY,item);
		startActivity(intent);
	}


}
