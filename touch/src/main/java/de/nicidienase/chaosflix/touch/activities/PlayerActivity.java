package de.nicidienase.chaosflix.touch.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Event;
import de.nicidienase.chaosflix.common.entities.recording.Recording;
import de.nicidienase.chaosflix.common.entities.userdata.PlaybackProgress;
import de.nicidienase.chaosflix.touch.ViewModelFactory;
import de.nicidienase.chaosflix.touch.fragments.ExoPlayerFragment;
import de.nicidienase.chaosflix.touch.viewmodels.PlayerViewModel;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PlayerActivity extends AppCompatActivity implements ExoPlayerFragment.OnMediaPlayerInteractionListener {
	public static final String EVENT_ID = "event_id";
	public static final String RECORDING_ID = "recording_id";
	private PlayerViewModel viewModel;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_container_layout);

		viewModel = ViewModelProviders.of(this, ViewModelFactory.INSTANCE).get(PlayerViewModel.class);

		long eventId = getIntent().getExtras().getLong(EVENT_ID);
		long recordingId = getIntent().getExtras().getLong(RECORDING_ID);

		if(savedInstanceState == null){
			loadFragment(eventId, recordingId);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		int eventId = intent.getExtras().getInt(EVENT_ID);
		int recordingId = intent.getExtras().getInt(RECORDING_ID);

		loadFragment(eventId,recordingId);
	}

	private void loadFragment(long eventId, long recordingId) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Observable.zip(viewModel.getEvent(eventId),viewModel.getRecording(recordingId),
				(event, recording) -> new Object[]{event,recording})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(objects -> {
					Fragment playerFragment =
							ExoPlayerFragment.newInstance((Event)objects[0],(Recording) objects[1]);
					ft.replace(R.id.fragment_container,playerFragment);
					ft.commit();
				});
	}

	@Override
	public Flowable<PlaybackProgress> getPlaybackProgress(long apiId) {
		return viewModel.getPlaybackProgress(apiId);
	}

	@Override
	public void setPlaybackProgress(long apiId, long progress) {
		viewModel.setPlaybackProgress(apiId,progress);
	}
}
