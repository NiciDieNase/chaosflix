package de.nicidienase.chaosflix.common.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceDao
import de.nicidienase.chaosflix.common.mediadata.sync.Downloader

class SplashViewModel(private val downloader: Downloader, private val conferenceDao: ConferenceDao): ViewModel() {

    val conferencesAvailable: LiveData<Boolean>?
            = Transformations.map(conferenceDao.getConferenceCount(),{ it > 0 })

    fun updateConferences() = downloader.updateConferencesAndGroups()
}