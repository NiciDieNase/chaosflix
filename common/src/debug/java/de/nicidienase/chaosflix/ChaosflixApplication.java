package de.nicidienase.chaosflix;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.orm.SugarContext;

import org.jetbrains.annotations.NotNull;

/**
 * Created by felix on 18.03.17.
 */

public class ChaosflixApplication extends Application {
	private static Context APPLICATION_CONTEXT;

	@Override
	public void onCreate() {
		super.onCreate();
		SugarContext.init(this);
		APPLICATION_CONTEXT = this;
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

	@NotNull
	public static Context getContext() {
		return APPLICATION_CONTEXT;
	}
}
