package de.nicidienase.chaosflix.touch.activities;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.List;

import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.common.entities.recording.Event;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by felix on 18.09.17.
 */

public class EventsActivity extends TouchBaseActivity {
	public static String CONFERENCE_KEY = "conference";
	private CompositeDisposable mDisposable = new CompositeDisposable();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Conference intentConference = (Conference) getIntent().getParcelableExtra(CONFERENCE_KEY);
		Disposable disposable = getApiServiceObservable().subscribe(mediaApiService -> {
			mediaApiService.getConference(intentConference.getApiID())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(conference -> {
						List<Event> events = conference.getEvents();
						// TODO build Adapters, etc.
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
}
