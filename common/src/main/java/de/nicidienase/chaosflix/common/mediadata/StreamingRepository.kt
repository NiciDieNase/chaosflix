package de.nicidienase.chaosflix.common.mediadata

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.nicidienase.chaosflix.common.AnalyticsWrapper
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.mediadata.network.StreamingApi
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StreamingRepository(
    private val streamingApi: StreamingApi,
    private val analyticsWrapper: AnalyticsWrapper
) {
    private val _streamingConferences: MutableLiveData<List<LiveConference>> = MutableLiveData()
    val streamingConferences: LiveData<List<LiveConference>> = _streamingConferences

    suspend fun update() = withContext(Dispatchers.IO) {
        try {
            val response = streamingApi.getStreamingConferences()
            if (response.isSuccessful) {
                _streamingConferences.postValue(response.body())
            }
        } catch (e: IOException) {
            Log.e(TAG, e.message, e)
            analyticsWrapper.trackException(e)
        }
        return@withContext
    }

    companion object {
        private val TAG = StreamingRepository::class.java.simpleName
    }
}
