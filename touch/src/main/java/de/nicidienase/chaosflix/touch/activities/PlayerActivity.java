package de.nicidienase.chaosflix.touch.activities;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Recording;
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent;
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentRecording;
import de.nicidienase.chaosflix.common.entities.userdata.PlaybackProgress;
import de.nicidienase.chaosflix.touch.ViewModelFactory;
import de.nicidienase.chaosflix.touch.fragments.ExoPlayerFragment;
import de.nicidienase.chaosflix.touch.viewmodels.PlayerViewModel;
import io.reactivex.disposables.CompositeDisposable;

public class PlayerActivity extends AppCompatActivity implements ExoPlayerFragment.OnMediaPlayerInteractionListener {
	public static final String EVENT_KEY = "event";
	public static final String RECORDING_KEY = "recording";
	private PlayerViewModel viewModel;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_container_layout);

		viewModel = ViewModelProviders.of(this, ViewModelFactory.INSTANCE).get(PlayerViewModel.class);

		PersistentEvent event = getIntent().getExtras().getParcelable(EVENT_KEY);
		PersistentRecording recording = getIntent().getExtras().getParcelable(RECORDING_KEY);

		if (savedInstanceState == null) {
			loadFragment(event,recording);
		}
	}

	private void loadFragment(PersistentEvent event, PersistentRecording recording) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment playerFragment =
				ExoPlayerFragment.newInstance(event, recording);
		ft.replace(R.id.fragment_container, playerFragment);
		ft.commit();
	}

	@Override
	public LiveData<PlaybackProgress> getPlaybackProgress(long apiId) {
		return viewModel.getPlaybackProgress(apiId);
	}

	@Override
	public void setPlaybackProgress(PersistentEvent event, long progress) {
		viewModel.setPlaybackProgress(event, progress);
	}
}
