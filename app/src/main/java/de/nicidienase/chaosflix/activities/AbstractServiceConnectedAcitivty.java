package de.nicidienase.chaosflix.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

import de.nicidienase.chaosflix.network.MediaApiService;
import io.reactivex.Observable;

/**
 * Created by felix on 24.03.17.
 */

public class AbstractServiceConnectedAcitivty extends Activity {
	private MediaApiService mMediaApiService;
	private Observable<MediaApiService> mApiServiceObservable;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);
		Intent s = new Intent(this, MediaApiService.class);
		mApiServiceObservable = Observable.create(e -> {
			AbstractServiceConnectedAcitivty.this.bindService(s, new ServiceConnection() {
				@Override
				public void onServiceConnected(ComponentName name, IBinder service) {
					mMediaApiService = ((MediaApiService.LocalBinder) service).getService();
					e.onNext(mMediaApiService);
				}

				@Override
				public void onServiceDisconnected(ComponentName name) {

				}
			}, Context.BIND_AUTO_CREATE);
		});
	}

	public MediaApiService getmMediaApiService() {
		return mMediaApiService;
	}

	public Observable<MediaApiService> getmApiServiceObservable() {
		return mApiServiceObservable;
	}
}
