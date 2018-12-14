package de.nicidienase.chaosflix.leanback.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.leanback.R

class EventsActivity : FragmentActivity() {

	public override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val conference = intent.getParcelableExtra<Conference>(CONFERENCE)
		if (conference.tagsUsefull) {
			// TODO determin if we should use a Browse or a Grid layout
		}
		setContentView(R.layout.activity_events_browse)
	}

	companion object {
		@JvmStatic
		val CONFERENCE = "conference"
		@JvmStatic
		val SHARED_ELEMENT_NAME = "shared_element"

		@JvmStatic
		@JvmOverloads
		fun start(context: Context, conference: Conference, transition: Bundle? = null) {
			val i = Intent(context, EventsActivity::class.java)
			i.putExtra(EventsActivity.CONFERENCE, conference)
			if (transition != null) {
				context.startActivity(i, transition)
			} else {
				context.startActivity(i)
			}
		}
	}
}
