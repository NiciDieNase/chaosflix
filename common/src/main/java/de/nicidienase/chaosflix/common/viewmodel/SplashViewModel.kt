package de.nicidienase.chaosflix.common.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceDao
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.EventDao
import de.nicidienase.chaosflix.common.mediadata.network.RecordingService
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class SplashViewModel(
    private val eventDao: EventDao,
    private val conferenceDao: ConferenceDao,
    private val recordingService: RecordingService
) : ViewModel() {

    val state: SingleLiveEvent<LiveEvent<State, Event, Exception>> = SingleLiveEvent()

    fun findEventForUri(data: Uri) = viewModelScope.launch(Dispatchers.IO) {
        try {
            var event: Event? = eventDao.findEventForFrontendUrl(data.toString())

            val pathSegment = data.lastPathSegment
            if (event == null && pathSegment != null) {
                val searchEvents = recordingService.searchEvents(pathSegment)
                if (searchEvents.events.isNotEmpty()) {
                    val eventDto = searchEvents.events[0]
                    val conference =
                        conferenceDao.findConferenceByAcronymSuspend(eventDto.conferenceUrl.split("/").last())
                    if (conference?.id != null) {
                        event = Event(eventDto, conference.id)
                        eventDao.updateOrInsert(event)
                    }
                }
            }
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