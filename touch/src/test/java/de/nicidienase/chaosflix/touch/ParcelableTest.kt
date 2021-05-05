package de.nicidienase.chaosflix.touch

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import de.nicidienase.chaosflix.touch.playback.PlaybackItem
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ParcelableTest {

    @Test
    fun playbackItemParcelableTest() {
        val playbackItem = PlaybackItem("title", "subtitle", "asdlökfjasd", "http://foo.bar/test")
        assertTrue(playbackItem.equals(PlaybackItem.createFromParcel(writeToParcel(playbackItem))))
    }

    private fun writeToParcel(parcelable: Parcelable): Parcel {
        val parcel = Parcel.obtain()
        parcelable.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        return parcel
    }
}
