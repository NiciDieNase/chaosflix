package de.nicidienase.chaosflix.activities;

import android.app.Activity;
import android.os.Bundle;

import de.nicidienase.chaosflix.R;

public class EventsActivity extends Activity {
	public static final String CONFERENCE_ID = "conference_id";
	public static final String CONFERENCE = "conference";
	public static final String SHARED_ELEMENT_NAME = "shared_element";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
}
