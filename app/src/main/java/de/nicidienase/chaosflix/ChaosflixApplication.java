package de.nicidienase.chaosflix;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;

import com.facebook.stetho.Stetho;
import com.orm.SugarContext;

import de.nicidienase.chaosflix.activities.ConferencesActivity;

/**
 * Created by felix on 18.03.17.
 */

public class ChaosflixApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Stetho.initializeWithDefaults(this);
		SugarContext.init(this);
	}

	@Override
	public void onTerminate() {
		SugarContext.terminate();
		super.onTerminate();
	}

	@Override
	protected void attachBaseContext(Context base) {
		MultiDex.install(base);
		super.attachBaseContext(base);
	}
}
