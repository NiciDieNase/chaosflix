package de.nicidienase.chaosflix.touch.browse.eventslist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.cast.CastService
import de.nicidienase.chaosflix.touch.eventdetails.EventDetailsActivity

class EventsListActivity : AppCompatActivity(), OnEventSelectedListener {

    protected val numColumns: Int
        get() = resources.getInteger(R.integer.num_columns)

    private lateinit var casty: CastService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events_list)

        casty = CastService(this)

        val conference = intent.getParcelableExtra<Conference>(CONFERENCE_KEY)

        if (savedInstanceState == null) {
            val eventsListFragment = EventsListFragment.newInstance(EventsListFragment.TYPE_EVENTS, conference, numColumns)

            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, eventsListFragment)
                    .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menu?.let {
            casty.addMediaRouteMenuItem(it)
        }
        return true
    }

    override fun onEventSelected(event: Event) {
        EventDetailsActivity.launch(this, event)
    }

    companion object {
        val CONFERENCE_KEY = "conference_id"

        fun start(context: Context, conference: Conference) {
            val i = Intent(context, EventsListActivity::class.java)
            i.putExtra(CONFERENCE_KEY, conference)
            context.startActivity(i)
        }
    }
}
