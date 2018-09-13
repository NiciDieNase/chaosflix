package de.nicidienase.chaosflix.leanback.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.FragmentActivity
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room


class EventDetailsActivity : FragmentActivity() {

	override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
		super.onCreate(savedInstanceState, persistentState)
		setContentView(R.layout.activity_event_details)
	}

	companion object {
		val EVENT = "event"
		val SHARED_ELEMENT_NAME = "transision_element"

		@JvmStatic
		fun start(context: Context, event: PersistentEvent, transition: Bundle){
			val i = Intent(context, EventsActivity::class.java)
			i.putExtra(EventDetailsActivity.EVENT, event)
			context.startActivity(i, transition)
		}

	}
}
