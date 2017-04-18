package de.nicidienase.chaosflix.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by felix on 18.04.17.
 */

public class FiretvStartActivity extends Activity {
	private static final String TAG = FiretvStartActivity.class.getSimpleName();
	public static final String NO_FIRE_TV = "This is not a Fire TV device.";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final String AMAZON_FEATURE_FIRE_TV = "amazon.hardware.fire_tv";
		if (getPackageManager().hasSystemFeature(AMAZON_FEATURE_FIRE_TV)) {
			Log.v(TAG, "Yes, this is a Fire TV device.");
			Intent i = new Intent(this,ConferencesActivity.class);
			startActivity(i);
		} else {
			Toast.makeText(this, NO_FIRE_TV,Toast.LENGTH_SHORT).show();
			Log.v(TAG, NO_FIRE_TV);
		}
	}
}
