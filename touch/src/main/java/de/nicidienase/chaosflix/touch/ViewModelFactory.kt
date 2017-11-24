package de.nicidienase.chaosflix.touch

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.persistence.room.Room
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase
import de.nicidienase.chaosflix.common.network.RecordingService
import de.nicidienase.chaosflix.common.network.StreamingService
import de.nicidienase.chaosflix.touch.browse.BrowseViewModel
import de.nicidienase.chaosflix.touch.eventdetails.DetailsViewModel
import de.nicidienase.chaosflix.touch.playback.PlayerViewModel
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

object ViewModelFactory : ViewModelProvider.Factory {

	val database = DatabaseFactory.database
	val recordingApi = ApiFactory.recordingApi
	val streamingApi = ApiFactory.streamingApi

	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel?> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(BrowseViewModel::class.java)) {
			return BrowseViewModel(database, recordingApi, streamingApi) as T

		} else if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
			return PlayerViewModel(database, recordingApi, streamingApi) as T

		} else if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
			return DetailsViewModel(database, recordingApi, streamingApi) as T

		} else {
			throw UnsupportedOperationException("The requested ViewModel is currently unsupported. " +
					"Please make sure to implement are correct creation of it. " +
					" Request: ${modelClass.getCanonicalName()}");
		}

	}

}