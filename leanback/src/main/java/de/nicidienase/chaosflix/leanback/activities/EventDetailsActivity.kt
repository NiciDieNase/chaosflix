package de.nicidienase.chaosflix.leanback.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity

import de.nicidienase.chaosflix.R


class EventDetailsActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
		super.onCreate(savedInstanceState, persistentState)
		setContentView(R.layout.activity_event_details)
	}

	companion object {

		val EVENT = "event"
		val SHARED_ELEMENT_NAME = "transision_element"
	}
}
