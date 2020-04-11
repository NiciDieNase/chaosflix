package de.nicidienase.chaosflix.common.mediadata

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.mediadata.network.StreamingApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StreamingRepository(
    private val streamingApi: StreamingApi
) {
    private val _streamingConferences: MutableLiveData<List<LiveConference>> = MutableLiveData()
    val streamingConferences: LiveData<List<LiveConference>> = _streamingConferences

    suspend fun update() = withContext(Dispatchers.IO) {
        try {
            val response = streamingApi.getStreamingConferences()
            if (response.isSuccessful) {
                _streamingConferences.postValue(response.body())
            }
        } catch (ex: Exception){
            Log.e(TAG, ex.message, ex)
        }
        return@withContext
    }

    companion object {
        private val TAG = StreamingRepository::class.java.simpleName
    }
}
