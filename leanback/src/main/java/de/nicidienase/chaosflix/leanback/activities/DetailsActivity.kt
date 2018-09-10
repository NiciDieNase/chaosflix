package de.nicidienase.chaosflix.leanback.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import de.nicidienase.chaosflix.R

/*
 * Details activity class that loads LeanbackDetailsFragment class
 */
class DetailsActivity : AppCompatActivity() {

	/**
	 * Called when the activity is first created.
	 */
	public override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_event_details)
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
	}

}
