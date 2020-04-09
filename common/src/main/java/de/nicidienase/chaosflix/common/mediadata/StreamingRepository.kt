package de.nicidienase.chaosflix.common.mediadata

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
        val response = streamingApi.getStreamingConferences()
        if (response.isSuccessful) {
            _streamingConferences.postValue(response.body())
        }
    }
}
