package de.nicidienase.chaosflix.leanback.activities

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory

/**
 * Created by felix on 18.03.17.
 */

class ConferencesActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val browseViewModel =
				ViewModelProviders
						.of(this, ViewModelFactory(this))
						.get(BrowseViewModel::class.java)
		//		setContentView(R.layout.activity_conferences_grid);
		setContentView(R.layout.activity_conferences_browse)
	}
}
