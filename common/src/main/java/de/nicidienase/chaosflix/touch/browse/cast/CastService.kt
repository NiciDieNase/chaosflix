package de.nicidienase.chaosflix.touch.browse.cast

import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamUrl
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgress
import de.nicidienase.chaosflix.touch.browse.streaming.StreamingItem

interface CastService {
    val connected: Boolean

    fun attachToActivity(activity: AppCompatActivity)
    fun castStream(streamingItem: StreamingItem, streamUrl: StreamUrl, contentKey: String)
    fun loadMediaAndPlay(recording: Recording, event: Event, progress: PlaybackProgress?)
    fun addMediaRouteMenuItem(menu: Menu)
}
