package de.nicidienase.chaosflix.common.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import de.nicidienase.chaosflix.common.mediadata.network.ApiFactory

class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

	val apiFactory = ApiFactory(context.resources)

	val database = DatabaseFactory(context).database
	val recordingApi = apiFactory.recordingApi
	val streamingApi = apiFactory.streamingApi

	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel?> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(BrowseViewModel::class.java)) {
			return BrowseViewModel(database, recordingApi, streamingApi) as T

		} else if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
			return PlayerViewModel(database) as T

		} else if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
			return DetailsViewModel(database, recordingApi) as T

		} else {
			throw UnsupportedOperationException("The requested ViewModel is currently unsupported. " +
					"Please make sure to implement are correct creation of it. " +
					" Request: ${modelClass.getCanonicalName()}");
		}

	}

}