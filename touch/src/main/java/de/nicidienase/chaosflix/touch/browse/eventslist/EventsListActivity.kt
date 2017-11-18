package de.nicidienase.chaosflix.touch.browse.eventslist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.widget.Toolbar
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.touch.browse.BrowseBaseActivity

class EventsListActivity : BrowseBaseActivity(), EventsListFragment.OnEventsListFragmentInteractionListener {

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events_list)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val conferenceId = intent.getLongExtra(CONFERENCE_ID_KEY, 0)

        if(savedInstanceState == null){
            val eventsListFragment = EventsListFragment.newInstance(conferenceId, numColumns)

            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, eventsListFragment)
                    .commit();
        }
    }

    override fun setToolbarTitle(title: String) {
        toolbar.setTitle(title)
    }

    companion object {
        val CONFERENCE_ID_KEY = "conference_id"

        fun start(context: Context, conferenceId: Long) {
            val i = Intent(context, EventsListActivity::class.java)
            i.putExtra(CONFERENCE_ID_KEY, conferenceId)
            context.startActivity(i)
        }
    }
}
