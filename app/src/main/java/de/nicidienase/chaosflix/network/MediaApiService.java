package de.nicidienase.chaosflix.network;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.entities.recording.Conference;
import de.nicidienase.chaosflix.entities.recording.Conferences;
import de.nicidienase.chaosflix.entities.recording.Event;
import de.nicidienase.chaosflix.entities.recording.Recording;
import de.nicidienase.chaosflix.entities.streaming.LiveConference;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by felix on 24.03.17.
 */

public class MediaApiService extends Service {

	private final IBinder mBinder = new LocalBinder();
	private final MediaCCCService mApiService;

	public class LocalBinder extends Binder {
		MediaApiService getService() {
			return MediaApiService.this;
		}
	}

	public MediaApiService(){
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(getString(R.string.api_media_ccc_url))
				.addConverterFactory(GsonConverterFactory.create())
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.build();
		mApiService = retrofit.create(MediaCCCService.class);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

}
