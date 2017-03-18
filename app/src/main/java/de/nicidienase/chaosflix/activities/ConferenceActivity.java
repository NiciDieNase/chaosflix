package de.nicidienase.chaosflix.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.entities.Conference;
import de.nicidienase.chaosflix.fragments.ConferenceBrowseFragment;
import de.nicidienase.chaosflix.network.MediaCCCClient;

public class ConferenceActivity extends Activity {
	public static final String CONFERENCE_ID = "conference_id";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
}
