package de.nicidienase.chaosflix

import android.arch.lifecycle.ViewModelProviders
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.test.ActivityInstrumentationTestCase2
import android.view.View
import de.nicidienase.chaosflix.common.entities.userdata.PlaybackProgress

import org.junit.Test
import org.junit.runner.RunWith

import de.nicidienase.chaosflix.touch.ViewModelFactory
import io.reactivex.functions.Consumer

/**
 * Created by felix on 31.10.17.
 */

@RunWith(AndroidJUnit4::class)
class PersistenceTest {

    @Test
    fun test1() {
        val playbackProgressDao = ViewModelFactory.database.playbackProgressDao()
        playbackProgressDao.saveProgress(PlaybackProgress(23,1337))
        playbackProgressDao.getProgressForEvent(23).subscribe({
            assert(it.eventId == 23L && it.progress == 1337L)
        })
    }
}
