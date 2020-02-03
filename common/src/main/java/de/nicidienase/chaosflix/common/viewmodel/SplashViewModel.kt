package de.nicidienase.chaosflix.common.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import java.lang.Exception
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashViewModel(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    val state: SingleLiveEvent<LiveEvent<State, Event, Exception>> = SingleLiveEvent()

    fun findEventForUri(data: Uri) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val event: Event? = mediaRepository.findEventForUri(data)
            if (event != null) {
                state.postValue(LiveEvent(State.FOUND, event))
            } else {
                state.postValue(LiveEvent(State.NOT_FOUND))
            }
        } catch (e: Exception) {
            state.postValue(LiveEvent(State.NOT_FOUND, error = e))
        }
    }

    enum class State {
        FOUND,
        NOT_FOUND
    }
}
