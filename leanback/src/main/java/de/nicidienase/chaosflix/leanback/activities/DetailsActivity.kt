package de.nicidienase.chaosflix.leanback.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity

import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room

/*
 * Details activity class that loads LeanbackDetailsFragment class
 */
class DetailsActivity : FragmentActivity() {

	/**
	 * Called when the activity is first created.
	 */
	public override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_event_details)
	}

	companion object {
		@JvmStatic
		val SHARED_ELEMENT_NAME = "hero"
		@JvmStatic
		val EVENT = "event"
		@JvmStatic
		val ROOM = "room"
		@JvmStatic
		val STREAM_URL = "stream_url"
		@JvmStatic
		val RECORDING = "recording"
		@JvmStatic
		val TYPE = "event_type"
		@JvmStatic
		val TYPE_RECORDING = 0
		@JvmStatic
		val TYPE_STREAM = 1

		@JvmStatic
		fun start(context: Context, event: PersistentEvent, transistion: Bundle? = null){
			val i = Intent(context, DetailsActivity::class.java)
			i.putExtra(DetailsActivity.TYPE, DetailsActivity.TYPE_RECORDING)
			i.putExtra(DetailsActivity.EVENT, event)
			context.startActivity(i, transistion)
		}

		@JvmStatic
		fun start(context: Context, room: Room, transition: Bundle){
			val i = Intent(context, DetailsActivity::class.java)
			i.putExtra(DetailsActivity.TYPE, DetailsActivity.TYPE_STREAM)
			i.putExtra(DetailsActivity.ROOM, room)
			context.startActivity(i, transition)
		}
	}


}
