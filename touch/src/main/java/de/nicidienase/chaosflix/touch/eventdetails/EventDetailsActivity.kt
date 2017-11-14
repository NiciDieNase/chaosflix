package de.nicidienase.chaosflix.touch.eventdetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentRecording
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.touch.playback.PlayerActivity

class EventDetailsActivity: AppCompatActivity(),
        EventDetailsFragment.OnEventDetailsFragmentInteractionListener,
        OnEventSelectedListener{

    override fun onEventSelected(event: PersistentEvent, v: View) {
        showFragmentForEvent(event.eventId,true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eventdetails)

        val eventId = intent.getLongExtra(EXTRA_EVENT,0)

        showFragmentForEvent(eventId)
    }

    private fun showFragmentForEvent(eventId: Long, addToBackStack: Boolean = false) {
        val detailsFragment = EventDetailsFragment.newInstance(eventId)

        detailsFragment.allowEnterTransitionOverlap = true
        detailsFragment.allowReturnTransitionOverlap = true

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, detailsFragment)
        if(addToBackStack){
            ft.addToBackStack(null)
        }
        ft.setReorderingAllowed(true)

        ft.commit()
    }

    override fun onToolbarStateChange() {
        invalidateOptionsMenu()
    }

    override fun setActionbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
    }

    override fun playItem(event: PersistentEvent, recording: PersistentRecording) {
        val i = Intent(this, PlayerActivity::class.java)
        i.putExtra(PlayerActivity.EVENT_KEY, event)
        i.putExtra(PlayerActivity.RECORDING_KEY, recording)
        startActivity(i)
    }

    companion object {
        private val EXTRA_EVENT = "extra_event"

        fun launch(context: Context, eventId: Long){
            val intent = Intent(context, EventDetailsActivity::class.java)
            intent.putExtra(EXTRA_EVENT,eventId)
            context.startActivity(intent)
        }
    }
}