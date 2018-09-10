package de.nicidienase.chaosflix.leanback.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import de.nicidienase.chaosflix.R

class EventsActivity : AppCompatActivity() {

	public override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		// TODO determin if we should use a Browse or a Grid layout
		setContentView(R.layout.activity_events_browse)
	}

	companion object {

		val CONFERENCE_ACRONYM = "conference_acronym"
		val CONFERENCE = "conference"
		val SHARED_ELEMENT_NAME = "shared_element"
	}
}
