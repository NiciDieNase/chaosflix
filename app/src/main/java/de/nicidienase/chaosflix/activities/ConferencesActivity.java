package de.nicidienase.chaosflix.activities;

import android.os.Bundle;

import de.nicidienase.chaosflix.R;

/**
 * Created by felix on 18.03.17.
 */

public class ConferencesActivity extends AbstractServiceConnectedAcitivty {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_conferences_grid);
		setContentView(R.layout.activity_conferences_browse);
	}
}
