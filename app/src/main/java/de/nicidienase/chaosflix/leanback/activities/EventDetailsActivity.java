package de.nicidienase.chaosflix.leanback.activities;

import android.os.Bundle;
import android.os.PersistableBundle;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.shared.AbstractServiceConnectedActivity;

/**
 * Created by felix on 18.03.17.
 */

public class EventDetailsActivity extends AbstractServiceConnectedActivity {

	public static final String EVENT = "event";
	public static final String SHARED_ELEMENT_NAME = "transision_element";

	@Override
	public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);
		setContentView(R.layout.activity_event_details);
	}
}
