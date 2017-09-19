package de.nicidienase.chaosflix.touch.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.common.entities.recording.Event;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by felix on 18.09.17.
 */

public class EventsActivity extends TouchBaseActivity implements EventsRecyclerViewAdapter.OnListFragmentInteractionListener {
	public static String CONFERENCE_KEY = "conference";
	private CompositeDisposable mDisposable = new CompositeDisposable();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recycler_view_layout);
		RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
		if(getNumColumns() <= 1){
			recyclerView.setLayoutManager(new LinearLayoutManager(this));
		} else {
			recyclerView.setLayoutManager(new GridLayoutManager(this,getNumColumns()));
		}

		Conference intentConference = (Conference) getIntent().getParcelableExtra(CONFERENCE_KEY);
		Disposable disposable = getApiServiceObservable().subscribe(mediaApiService -> {
			mediaApiService.getConference(intentConference.getApiID())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(conference -> {
						getSupportActionBar().setTitle(conference.getTitle());
						List<Event> events = conference.getEvents();
						// TODO build Adapters, etc.
						recyclerView.setAdapter(new EventsRecyclerViewAdapter(events,this));
					});
				}
		);
		mDisposable.add(disposable);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mDisposable.dispose();
	}

	@Override
	public void onListItemSelected(Event event) {
		// TODO start detailview for Events
	}

	private int getNumColumns() {
		return getResources().getInteger(R.integer.num_columns);
	}
}
