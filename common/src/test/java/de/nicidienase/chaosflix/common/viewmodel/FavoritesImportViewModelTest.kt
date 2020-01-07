package de.nicidienase.chaosflix.common.viewmodel

import de.nicidienase.chaosflix.common.InstantExecutorExtension
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, InstantExecutorExtension::class)
internal class FavoritesImportViewModelTest {

    @RelaxedMockK
    private lateinit var mediaRepository: MediaRepository

    private lateinit var favoritesImportViewModel: FavoritesImportViewModel

    @BeforeEach
    fun setup() {
        favoritesImportViewModel = FavoritesImportViewModel(mediaRepository)
    }

    @Test
    fun selectAll() {
        val slot = slot<String>()
        coEvery { mediaRepository.findEventByTitle(capture(slot)) }.answers {
            Event(title = slot.captured)
        }
        runBlocking {
            favoritesImportViewModel.handleLecturesInternal(SAMPLE_JSON)
        }

        favoritesImportViewModel.selectAll(null)
        val items = favoritesImportViewModel.items.value ?: emptyList()
        val selected = items.map { it.selected }
        assertEquals(2, selected.size)
        selected.map { value ->
            assertEquals(true, value)
        }
    }

    @Test
    fun unselectAll() {
        val slot = slot<String>()
        coEvery { mediaRepository.findEventByTitle(capture(slot)) }.answers {
            Event(title = slot.captured)
        }
        runBlocking {
            favoritesImportViewModel.handleLecturesInternal(SAMPLE_JSON)
        }

        favoritesImportViewModel.selectAll(false)
        val items = favoritesImportViewModel.items.value ?: emptyList()

        assertEquals(2, items.size)
        items.map { value ->
            assertEquals(false, value.selected)
        }
    }

    companion object {
        private const val SAMPLE_JSON = "{\"conference\":\"ccc36c3\",\"lectures\":[" +
                "{\"lectureId\":\"11223\"," +
                "\"title\":\"Opening Ceremony\"," +
                "\"subtitle\":\"\",\"day\":1," +
                "\"room\":\"Ada\"," +
                "\"slug\":\"36c3-11223-opening_ceremony\"," +
                "\"url\":\"https://fahrplan.events.ccc.de/congress/2019/Fahrplan/events/11223.html\"," +
                "\"speakers\":\"bleeptrack;blinry\"," +
                "\"track\":\"CCC\"," +
                "\"type\":\"de\"," +
                "\"lang\":\"\"," +
                "\"abstract\":\"Welcome!\"," +
                "\"description\":\"\"," +
                "\"links\":\"2019-12-27\"}," +
                "{\"lectureId\":\"11224\"," +
                "\"title\":\"Closing Ceremony\"," +
                "\"subtitle\":\"\"," +
                "\"day\":4," +
                "\"room\":\"Ada\"," +
                "\"slug\":\"36c3-11224-closing_ceremony\"," +
                "\"url\":\"\"," +
                "\"speakers\":\"bleeptrack;blinry\"," +
                "\"track\":\"CCC\"," +
                "\"type\":\"de\"," +
                "\"lang\":\"\"," +
                "\"abstract\":\"Welcome!\"," +
                "\"description\":\"\"," +
                "\"links\":\"2019-12-30\"}" +
                "]}"
    }
}