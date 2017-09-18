package de.nicidienase.chaosflix.touch.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import de.nicidienase.chaosflix.common.network.MediaApiService;
import io.reactivex.Single;

/**
 * Created by felix on 24.03.17.
 */

public class TouchBaseActivity extends AppCompatActivity {
	private MediaApiService mMediaApiService = null;
	private ServiceConnection conn;
	private boolean mConnected = false;
	private String serverUrl = null;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);
		serverUrl = getIntent().getStringExtra("server_url");
	}

	@Override
	protected void onDestroy() {
		if (mConnected) {
			unbindService(conn);
			conn = null;
		}
		super.onDestroy();
	}


	public Single<MediaApiService> getApiServiceObservable() {
		Intent s = new Intent(this, MediaApiService.class);
		if (serverUrl != null) {
			s.putExtra(MediaApiService.RECORDING_URL, serverUrl);
			s.putExtra(MediaApiService.STREAMING_URL, serverUrl);
		}
		return Single.create(e -> {
			if (mMediaApiService != null) {
				e.onSuccess(mMediaApiService);
			} else {
				conn = new ServiceConnection() {
					@Override
					public void onServiceConnected(ComponentName name, IBinder service) {
						mConnected = true;
						mMediaApiService = ((MediaApiService.LocalBinder) service).getService();
						e.onSuccess(mMediaApiService);
					}

					@Override
					public void onServiceDisconnected(ComponentName name) {
						mMediaApiService = null;
						mConnected = false;
					}
				};
				TouchBaseActivity.this.bindService(s, conn, Context.BIND_AUTO_CREATE);
			}
		});
	}
}
