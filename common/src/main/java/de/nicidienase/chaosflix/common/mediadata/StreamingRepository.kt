package de.nicidienase.chaosflix.common.mediadata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.mediadata.network.StreamingApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StreamingRepository(
    private val streamingApi: StreamingApi
) {
    private val _streamingConferences: MutableLiveData<List<LiveConference>> = MutableLiveData()
    val streamingConferences: LiveData<List<LiveConference>> = _streamingConferences

    fun update(scope: CoroutineScope): LiveData<List<LiveConference>> {
        scope.launch(Dispatchers.IO) {
            _streamingConferences.postValue(streamingApi.getStreamingConferences())
        }
        return streamingConferences
    }
}
