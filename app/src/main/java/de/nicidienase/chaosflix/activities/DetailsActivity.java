/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package de.nicidienase.chaosflix.activities;

import android.app.Activity;
import android.os.Bundle;

import de.nicidienase.chaosflix.R;

/*
 * Details activity class that loads LeanbackDetailsFragment class
 */
public class DetailsActivity extends AbstractServiceConnectedAcitivty {

	public static final String SHARED_ELEMENT_NAME = "hero";
	public static final String EVENT = "event";
	public static final String ROOM = "room";
	public static final String STREAM_URL = "stream_url";
	public static final String RECORDING = "recording";
	public static final String TYPE = "event_type";
	public static final int TYPE_RECORDING = 0;
	public static final int TYPE_STREAM = 1;


	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_details);
	}

}
