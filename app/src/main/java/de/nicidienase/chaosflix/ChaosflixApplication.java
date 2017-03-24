package de.nicidienase.chaosflix;

import android.app.Application;
import android.content.Intent;

import com.orm.SugarContext;

import de.nicidienase.chaosflix.activities.ConferencesActivity;

/**
 * Created by felix on 18.03.17.
 */

public class ChaosflixApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		SugarContext.init(this);
	}

	@Override
	public void onTerminate() {
		SugarContext.terminate();
		super.onTerminate();
	}
}
