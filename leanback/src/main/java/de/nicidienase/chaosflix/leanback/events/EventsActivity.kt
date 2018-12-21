package de.nicidienase.chaosflix.leanback.events

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.leanback.R

class EventsActivity : FragmentActivity() {

	public override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val conference = intent.getParcelableExtra<Conference>(CONFERENCE)
		setContentView(R.layout.activity_events_browse)
		if(savedInstanceState == null){
			val fragment: Fragment = EventsRowsBrowseFragment.create(conference)
//					if (conference.tagsUsefull) {
//				EventsRowsBrowseFragment.create(conference)
//			} else {
//				EventsGridBrowseFragment.create(conference)
//			}
			supportFragmentManager.beginTransaction().replace(R.id.browse_fragment, fragment).commit()
		}
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
			i.putExtra(CONFERENCE, conference)
			if (transition != null) {
				context.startActivity(i, transition)
			} else {
				context.startActivity(i)
			}
		}
	}
}
