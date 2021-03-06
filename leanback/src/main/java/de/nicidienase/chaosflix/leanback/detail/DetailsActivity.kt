package de.nicidienase.chaosflix.leanback.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.core.os.bundleOf
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room
import de.nicidienase.chaosflix.leanback.R

/*
 * Details activity class that loads LeanbackDetailsFragment class
 */
class DetailsActivity : androidx.fragment.app.FragmentActivity() {

    /**
	 * Called when the activity is first created.
	 */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)
        val fragment = when (intent.getIntExtra(TYPE, 0)) {
            TYPE_RECORDING -> EventDetailsFragment().apply {
                arguments = bundleOf(EVENT to intent.getParcelableExtra<Event>(EVENT))
            }
            TYPE_STREAM -> StreamDetailsFragment().apply {
                arguments = bundleOf(ROOM to intent.getParcelableExtra<Room>(ROOM))
            }
            else -> error("undefinded type")
        }
        supportFragmentManager.beginTransaction().replace(R.id.details_fragment_container, fragment).commit()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
        fun start(context: Context, event: Event, transition: Bundle? = null) {
            val i = Intent(context, DetailsActivity::class.java)
            i.putExtra(TYPE, TYPE_RECORDING)
            i.putExtra(EVENT, event)
            if (transition != null) {
                context.startActivity(i, transition)
            } else {
                context.startActivity(i)
            }
        }

        @JvmStatic
        fun start(context: Context, room: Room, transition: Bundle? = null) {
            val i = Intent(context, DetailsActivity::class.java)
            i.putExtra(TYPE, TYPE_STREAM)
            i.putExtra(ROOM, room)
            if (transition != null) {
                context.startActivity(i, transition)
            } else {
                context.startActivity(i)
            }
        }
    }
}
