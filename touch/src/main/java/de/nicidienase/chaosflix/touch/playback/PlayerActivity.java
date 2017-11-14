package de.nicidienase.chaosflix.touch.playback;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent;
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentRecording;

public class PlayerActivity extends AppCompatActivity implements ExoPlayerFragment.OnMediaPlayerInteractionListener {
	public static final String EVENT_KEY = "event";
	public static final String RECORDING_KEY = "recording";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);

		PersistentEvent event = getIntent().getExtras().getParcelable(EVENT_KEY);
		PersistentRecording recording = getIntent().getExtras().getParcelable(RECORDING_KEY);

		if (savedInstanceState == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment playerFragment =
					ExoPlayerFragment.newInstance(event, recording);
			ft.replace(R.id.fragment_container, playerFragment);
			ft.commit();
		}
	}

}
