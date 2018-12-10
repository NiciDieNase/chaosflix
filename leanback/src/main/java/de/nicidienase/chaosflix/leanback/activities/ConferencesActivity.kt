package de.nicidienase.chaosflix.leanback.activities

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import de.nicidienase.chaosflix.leanback.R

class ConferencesActivity : FragmentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_conferences_browse)
	}
}
