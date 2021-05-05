package de.nicidienase.chaosflix.common

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import de.nicidienase.chaosflix.common.mediadata.entities.recording.RelatedEventDto
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.RelatedEvent
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Stream
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamEvent
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamUrl
import de.nicidienase.chaosflix.common.userdata.entities.download.OfflineEvent
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ParcelableTest {

    @Test
    fun conferenceParcelableTest() {
        val conference = Conference(23,
                42,
                "GPN42",
                "16:9",
                "Gulaschprogrammiernacht 42",
                "GPN42",
                "http://example/com",
                "http://example/com",
                "http://example/com",
                "http://example/com",
                "http://example/com",
                "http://example/com",
                "2042-04-05 23:42:50",
                false,
                "2042-04-05 23:42:50")

        assertTrue(conference.equals(Conference.createFromParcel(writeToParcel(conference))))
    }

    @Test
    fun eventParcelableTest() {
        val event = Event(23,
                42,
                "GPN42",
                "2314324-12323432-4326546",
                "Developing for Android 23",
                "finally in Swift ;)",
                "And23",
                "https://example.com/",
                "Lorem Ipsum",
                "klingon",
                "2042.4.5 23:42:59",
                "2042.4.5 23:42:59",
                "2042.4.5 23:42:59",
                1337,
                "https://example.com/thumb.png",
                "https://example.com/poster.png",
                "https://example.com/talk.json",
                "https://example.com/talk.html",
                "https://example.com/GPN42",
                true,
                230,
                null,
                null,
                null,
                null
        )
        assertTrue(event.equals(Event.createFromParcel(writeToParcel(event))))
    }

    @Test
    fun reletedEventDtoParcelableTest() {
        val relatedEventDto = RelatedEventDto("ß9834573240ß958", 42)
        assertTrue(relatedEventDto.equals(RelatedEventDto.createFromParcel(writeToParcel(relatedEventDto))))
    }

    @Test
    fun conferenceGroupParcelableTest() {
        val conferenceGroup = ConferenceGroup("conferenceGroup")
        assertTrue(conferenceGroup.equals(ConferenceGroup.createFromParcel(writeToParcel(conferenceGroup))))
    }

    @Test
    fun recordingParcelableTest() {
        val recording = Recording(23,
                42,
                2342,
                1337,
                "video/mp5",
                "klingon",
                "foo.bar",
                "bar",
                "foo",
                true,
                640,
                480,
                "1970.1.1 00:00:00",
                "https://example.com/recording",
                "https://example.com/item",
                "https://example.com/event",
                "https://example.com/conference",
                99)
        assertTrue(recording.equals(Recording.createFromParcel(writeToParcel(recording))))
    }

    @Test
    fun relatedEventParcelableTest() {
        val relatedEvent = RelatedEvent(23, 42, "asdlkfjasdf", 99)
        val other = RelatedEvent.createFromParcel(writeToParcel(relatedEvent))
        assertTrue(relatedEvent.equals(other))
    }

    @Test
    fun roomParcelableTest() {
        val room = Room("foo",
                "schedulename",
                "thumb",
                "link",
                "display",
                null,
                emptyList())
        val other = Room.createFromParcel(writeToParcel(room))
        assertTrue(room.equals(other))
    }

    @Test
    fun streamParcelableTest() {
        val stream = Stream("slug",
                "display",
                "type",
                true,
                intArrayOf(640, 480),
                HashMap<String, StreamUrl>())
        val other = Stream.createFromParcel(writeToParcel(stream))
        assertTrue(stream.equals(other))
    }

    @Test
    fun streamEventParcelableTest() {
        val streamEvent = StreamEvent("title",
                "speaker",
                "fstart",
                "fend",
                1,
                23,
                42,
                true)
        assertTrue(streamEvent.equals(StreamEvent.createFromParcel(writeToParcel(streamEvent))))
    }

    @Test
    fun streamUrlParcelableTest() {
        val streamUrl = StreamUrl("display", "tech", "url")
        assertTrue(streamUrl.equals(StreamUrl.createFromParcel(writeToParcel(streamUrl))))
    }

    @Test
    fun offlineEventParcelableTest() {
        val offlineEvent = OfflineEvent(12, "asdflkjasdf",
                34, 56, "/path/to/file.mp4")
        assertTrue(offlineEvent.equals(OfflineEvent.createFromParcel(writeToParcel(offlineEvent))))
    }

    private fun writeToParcel(parcelable: Parcelable): Parcel {
        val parcel = Parcel.obtain()
        parcelable.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        return parcel
    }
}
