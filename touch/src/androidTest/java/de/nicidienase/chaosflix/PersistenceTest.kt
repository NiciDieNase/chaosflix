package de.nicidienase.chaosflix

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgress
import java.util.Date
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by felix on 31.10.17.
 */

@RunWith(AndroidJUnit4::class)
class PersistenceTest {

    @Test
    fun test1() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val dummyGuid = "asasdlfkjsd"
        val playbackProgressDao = ChaosflixDatabase.getInstance(context).playbackProgressDao()
        val watchDate = Date().time
        playbackProgressDao.saveProgress(PlaybackProgress(23, dummyGuid, 1337, watchDate))
        playbackProgressDao.getProgressForEvent(dummyGuid)
                .observeForever { assert(it?.id == 23L && it.progress == 1337L) }
    }
}
