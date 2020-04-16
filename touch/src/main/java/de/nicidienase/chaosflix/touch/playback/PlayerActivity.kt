package de.nicidienase.chaosflix.touch.playback

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.navArgs
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamUrl
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.cast.CastService

class PlayerActivity : AppCompatActivity() {

    private lateinit var casty: CastService

    private val args: PlayerActivityArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        casty = CastService(this, false)

        val extras = intent.extras
        if (savedInstanceState == null && extras != null) {

            val ft = supportFragmentManager.beginTransaction()
            val playerFragment = ExoPlayerFragment.newInstance(args.playbackItem)
            ft.replace(R.id.fragment_container, playerFragment)
            ft.commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menu?.let {
            casty.addMediaRouteMenuItem(it)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val CONTENT_TYPE = "content"
        const val CONTENT_RECORDING = "content_recording"
        const val CONTENT_STREAM = "content_stream"

        const val EVENT_KEY = "event"
        const val RECORDING_KEY = "recording"
        const val CONFERENCE = "live_conferences"
        const val ROOM = "room"
        const val STREAM = "stream"

        const val OFFLINE_URI = "recording_uri"

        fun launch(context: Context, event: Event, uri: String) {
            val i = Intent(context, PlayerActivity::class.java)
            i.putExtra(CONTENT_TYPE, CONTENT_RECORDING)
            i.putExtra(EVENT_KEY, event)
            i.putExtra(OFFLINE_URI, uri)
            context.startActivity(i)
        }

        fun launch(context: Context, event: Event, recording: Recording) {
            val i = Intent(context, PlayerActivity::class.java)
            i.putExtra(CONTENT_TYPE, CONTENT_RECORDING)
            i.putExtra(EVENT_KEY, event)
            i.putExtra(RECORDING_KEY, recording)
            context.startActivity(i)
        }

        fun launch(context: Context, conference: String, room: String, stream: StreamUrl) {
            val i = Intent(context, PlayerActivity::class.java)
            i.putExtra(CONTENT_TYPE, CONTENT_STREAM)
            i.putExtra(CONFERENCE, conference)
            i.putExtra(ROOM, room)
            i.putExtra(STREAM, stream.url)
            context.startActivity(i)
        }
    }
}
