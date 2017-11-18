package de.nicidienase.chaosflix.touch.playback

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentRecording
import de.nicidienase.chaosflix.common.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.entities.streaming.Room
import de.nicidienase.chaosflix.common.entities.streaming.Stream

class PlayerActivity : AppCompatActivity(), ExoPlayerFragment.OnMediaPlayerInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val contentType = intent.getStringExtra(CONTENT_TYPE)
        if (contentType.equals(CONTENT_RECORDING)) {
            val event = intent.extras!!.getParcelable<PersistentEvent>(EVENT_KEY)
            val recording = intent.extras!!.getParcelable<PersistentRecording>(RECORDING_KEY)

            if (savedInstanceState == null) {
                val ft = supportFragmentManager.beginTransaction()
                val playerFragment = ExoPlayerFragment.newInstance(event, recording)
                ft.replace(R.id.fragment_container, playerFragment)
                ft.commit()
            }
        } else if (contentType.equals(CONTENT_STREAM)){
            // TODO implement Player for Stream
        }
    }

    companion object {
        val CONTENT_TYPE = "content"
        val CONTENT_RECORDING = "content_recording"
        val CONTENT_STREAM = "content_stream"

        val EVENT_KEY = "event"
        val RECORDING_KEY = "recording"
        val CONFERENCE = "live_conferences"
        val ROOM = "room"
        val STREAM = "stream"

        fun launch(context: Context, event: PersistentEvent, recording: PersistentRecording) {
            val i = Intent(context, PlayerActivity::class.java)
            i.putExtra(CONTENT_TYPE, CONTENT_RECORDING)
            i.putExtra(PlayerActivity.EVENT_KEY, event)
            i.putExtra(PlayerActivity.RECORDING_KEY, recording)
            context.startActivity(i)
        }

        fun launch(context: Context, conference: LiveConference, room: Room, stream: Stream) {
            val i = Intent(context, PlayerActivity::class.java)
            i.putExtra(CONTENT_TYPE, CONTENT_STREAM)
            i.putExtra(CONTENT_TYPE, CONTENT_STREAM)
            i.putExtra(CONFERENCE, conference.conference)
            i.putExtra(ROOM, room)
            i.putExtra(STREAM, stream)
            context.startActivity(i)
        }
    }

}
