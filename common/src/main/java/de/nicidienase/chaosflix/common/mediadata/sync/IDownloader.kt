package de.nicidienase.chaosflix.common.mediadata.sync

import androidx.lifecycle.LiveData
import de.nicidienase.chaosflix.common.mediadata.entities.recording.ConferencesWrapper
import de.nicidienase.chaosflix.common.mediadata.entities.recording.EventDto
import de.nicidienase.chaosflix.common.mediadata.entities.recording.RecordingDto
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.RelatedEvent
import de.nicidienase.chaosflix.common.util.LiveEvent

interface IDownloader {
    fun updateConferencesAndGroups(): LiveData<LiveEvent<State, List<Conference>, String>>
    fun updateEventsForConference(conference: Conference): LiveData<LiveEvent<State, List<Event>, String>>

//    suspend fun updateEventsForConferencesSuspending(conference: Conference): List<Event>
    fun updateRecordingsForEvent(event: Event):
            LiveData<LiveEvent<State, List<Recording>, String>>

    suspend fun updateSingleEvent(guid: String): Event?
    fun deleteNonUserData()
    fun saveConferences(conferencesWrapper: ConferencesWrapper): List<Conference>
    fun getOrCreateConferenceGroup(name: String): ConferenceGroup
    fun saveEvents(persistentConference: Conference, events: List<EventDto>): List<Event>
    fun saveEvent(event: EventDto): Event
    fun updateConferencesAndGet(acronym: String): Long
    fun saveRelatedEvents(event: Event): List<RelatedEvent>
    fun saveRecordings(event: Event, recordings: List<RecordingDto>): List<Recording>

    enum class State {
        DONE, RUNNING
    }

}