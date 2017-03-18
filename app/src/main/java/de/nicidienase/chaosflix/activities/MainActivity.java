package de.nicidienase.chaosflix.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import de.nicidienase.chaosflix.entities.Conference;

/**
 * Created by felix on 18.03.17.
 */

public class MainActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);
		Intent intent = new Intent(this, ConferenceActivity.class);
		intent.putExtra(ConferenceActivity.CONFERENCE_ID,101);
		startActivity(intent);
		this.finish();
	}
}
