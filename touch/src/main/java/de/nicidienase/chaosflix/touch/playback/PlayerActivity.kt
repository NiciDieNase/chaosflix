package de.nicidienase.chaosflix.touch.playback

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamUrl
import de.nicidienase.chaosflix.touch.browse.cast.CastService

class PlayerActivity : AppCompatActivity() {

	private lateinit var casty: CastService

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_player)

		casty = CastService(this,false)

		val extras = intent.extras
		if (savedInstanceState == null && extras != null) {
			val contentType = intent.getStringExtra(CONTENT_TYPE)
			var playbackItem = PlaybackItem("Empty", "Empty", "", "")
			if (contentType == CONTENT_RECORDING) {
				val event = extras.getParcelable<Event>(EVENT_KEY)
				val recording = extras.getParcelable<Recording>(RECORDING_KEY)
				val recordingUri = extras.getString(OFFLINE_URI)
				playbackItem = PlaybackItem(
						event?.title ?: "",
						event?.subtitle ?: "",
						event?.guid ?: "",
						recordingUri ?: recording?.recordingUrl ?: "")
			} else if (contentType.equals(CONTENT_STREAM)) {
				// TODO implement Player for Stream
				val conference = extras.getString(CONFERENCE,"")
				val room = extras.getString(ROOM,"")
				val stream = extras.getString(STREAM, "")
				playbackItem = PlaybackItem(conference,room,"", stream)
			}

			val ft = supportFragmentManager.beginTransaction()
			val playerFragment = ExoPlayerFragment.newInstance(playbackItem)
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
		val CONTENT_TYPE = "content"
		val CONTENT_RECORDING = "content_recording"
		val CONTENT_STREAM = "content_stream"

		val EVENT_KEY = "event"
		val RECORDING_KEY = "recording"
		val CONFERENCE = "live_conferences"
		val ROOM = "room"
		val STREAM = "stream"

		val OFFLINE_URI = "recording_uri"

		fun launch(context: Context, event: Event, uri: String) {
			val i = Intent(context, PlayerActivity::class.java)
			i.putExtra(CONTENT_TYPE, CONTENT_RECORDING)
			i.putExtra(PlayerActivity.EVENT_KEY, event)
			i.putExtra(PlayerActivity.OFFLINE_URI, uri)
			context.startActivity(i)
		}

		fun launch(context: Context, event: Event, recording: Recording) {
			val i = Intent(context, PlayerActivity::class.java)
			i.putExtra(CONTENT_TYPE, CONTENT_RECORDING)
			i.putExtra(PlayerActivity.EVENT_KEY, event)
			i.putExtra(PlayerActivity.RECORDING_KEY, recording)
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
