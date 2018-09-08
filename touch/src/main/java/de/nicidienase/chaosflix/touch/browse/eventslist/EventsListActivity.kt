package de.nicidienase.chaosflix.touch.browse.eventslist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentConference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.touch.eventdetails.EventDetailsActivity

class EventsListActivity : AppCompatActivity(), OnEventSelectedListener {

	protected val numColumns: Int
		get() = resources.getInteger(R.integer.num_columns)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_events_list)

		val conference = intent.getParcelableExtra<PersistentConference>(CONFERENCE_KEY)

		if (savedInstanceState == null) {
			val eventsListFragment = EventsListFragment.newInstance(EventsListFragment.TYPE_EVENTS, conference, numColumns)

			supportFragmentManager.beginTransaction()
					.replace(R.id.fragment_container, eventsListFragment)
					.commit();
		}
	}

	override fun onEventSelected(event: PersistentEvent) {
		EventDetailsActivity.launch(this, event)
	}

	companion object {
		val CONFERENCE_KEY = "conference_id"

		fun start(context: Context, conference: PersistentConference) {
			val i = Intent(context, EventsListActivity::class.java)
			i.putExtra(CONFERENCE_KEY, conference)
			context.startActivity(i)
		}
	}
}
