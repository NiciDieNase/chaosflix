package de.nicidienase.chaosflix.leanback.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room
import de.nicidienase.chaosflix.leanback.R
import kotlinx.coroutines.launch

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
        lifecycleScope.launch {
            val fragment = if (intent.action == Intent.ACTION_VIEW) {
                val guid = intent.data.lastPathSegment
                EventDetailsFragment().apply {
                    arguments = bundleOf(EventDetailsFragment.ARG_EVENT_GUID to guid)
                }
            } else {
                when (intent.getIntExtra(TYPE, 0)) {
                    TYPE_RECORDING -> EventDetailsFragment().apply {
                        arguments = bundleOf(EventDetailsFragment.ARG_EVENT to intent.getParcelableExtra<Event>(EVENT))
                    }
                    TYPE_STREAM -> StreamDetailsFragment().apply {
                        arguments = bundleOf(ROOM to intent.getParcelableExtra<Room>(ROOM))
                    }
                    else -> error("undefinded type")
                }
            }
            supportFragmentManager.beginTransaction().replace(R.id.details_fragment_container, fragment).commit()
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    companion object {
        const val SHARED_ELEMENT_NAME = "hero"
        const val EVENT = "event"
        const val ROOM = "room"
        const val TYPE = "event_type"
        const val TYPE_RECORDING = 0
        const val TYPE_STREAM = 1

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
