package de.nicidienase.chaosflix;

import android.app.Application;

import com.orm.SugarContext;

/**
 * Created by felix on 18.03.17.
 */

public class ChaosflixApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		SugarContext.init(this);
	}
}
