package de.nicidienase.chaosflix.leanback.activities

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v17.leanback.widget.ImageCardView
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager

import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentConference

class EventsActivity : FragmentActivity() {

	public override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val conference = intent.getParcelableExtra<PersistentConference>(CONFERENCE)
		if(conference.tagsUsefull){
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
		fun start(context: Context ,conference: PersistentConference, transition: Bundle? = null){
			val i = Intent(context, EventsActivity::class.java)
			i.putExtra(EventsActivity.CONFERENCE, conference)
			if(transition != null){
				context.startActivity(i, transition)
			} else {
				context.startActivity(i)
			}
		}
	}
}
