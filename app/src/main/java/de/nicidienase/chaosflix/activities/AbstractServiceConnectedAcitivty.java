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
import io.reactivex.Single;

/**
 * Created by felix on 24.03.17.
 */

public class AbstractServiceConnectedAcitivty extends Activity {
	private MediaApiService mMediaApiService = null;
	private Single<MediaApiService> mApiServiceObservable;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);
		Intent s = new Intent(this, MediaApiService.class);
		mApiServiceObservable = Single.create(e -> {
			if(mMediaApiService != null){
				e.onSuccess(mMediaApiService);
			} else {
				AbstractServiceConnectedAcitivty.this.bindService(s, new ServiceConnection() {
					@Override
					public void onServiceConnected(ComponentName name, IBinder service) {
						mMediaApiService = ((MediaApiService.LocalBinder) service).getService();
						e.onSuccess(mMediaApiService);
					}

					@Override
					public void onServiceDisconnected(ComponentName name) {

					}
				}, Context.BIND_AUTO_CREATE);
			}
		});
	}

	public Single<MediaApiService> getmApiServiceObservable() {
		return mApiServiceObservable;
	}
}
