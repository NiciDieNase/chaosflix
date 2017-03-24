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

package de.nicidienase.chaosflix.fragments;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.DisplayMetrics;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.common.collect.Lists;

import de.nicidienase.chaosflix.CardPresenter;
import de.nicidienase.chaosflix.ItemViewClickedListener;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.activities.EventsActivity;
import de.nicidienase.chaosflix.entities.recording.Conference;
import de.nicidienase.chaosflix.entities.recording.Event;
import de.nicidienase.chaosflix.network.RecordingClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsBrowseFragment extends BrowseFragment {
	private static final String TAG = "MainFragment";

	private static final int BACKGROUND_UPDATE_DELAY = 300;
	private static final int GRID_ITEM_WIDTH = 200;
	private static final int GRID_ITEM_HEIGHT = 200;
	private static final int FRAGMENT = R.id.browse_fragment;

	private final Handler mHandler = new Handler();
	private ArrayObjectAdapter mRowsAdapter;
	private Drawable mDefaultBackground;
	private DisplayMetrics mMetrics;
	private Timer mBackgroundTimer;
	private URI mBackgroundURI;
	private BackgroundManager mBackgroundManager;
	private Conference conference;
	private RecordingClient client = new RecordingClient();;
	private int conferenceId;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onActivityCreated(savedInstanceState);
		final BrowseErrorFragment errorFragment =
				BrowseErrorFragment.showErrorFragment(getFragmentManager(),FRAGMENT);
		conferenceId = this.getActivity().getIntent().getIntExtra(EventsActivity.CONFERENCE_ID, 0);
		conference = this.getActivity().getIntent().getParcelableExtra(EventsActivity.CONFERENCE);
		client.getConference(conference.getApiID()).enqueue(new Callback<Conference>() {
			@Override
			public void onResponse(Call<Conference> call, Response<Conference> response) {
				conference = response.body();
				setupUIElements();
				loadRows();
				errorFragment.dismiss();
			}

			@Override
			public void onFailure(Call<Conference> call, Throwable t) {
				Log.d(TAG,"Error loading conferences",t);
				errorFragment.setErrorContent(t.getMessage());
				t.printStackTrace();
			}
		});
		prepareBackgroundManager();
		setupEventListeners();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (null != mBackgroundTimer) {
			Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
			mBackgroundTimer.cancel();
		}
	}

	private void loadRows() {
		HashMap<String, List<Event>> eventsByTags = conference.getEventsByTags();

		mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
		CardPresenter cardPresenter = new CardPresenter();

		List<Event> other = new LinkedList<Event>();
		List<String> keys = Lists.newArrayList(eventsByTags.keySet());
		Collections.sort(keys);
		for (String tag: keys) {
			List<Event> items = eventsByTags.get(tag);
			Collections.sort(items);
			if(android.text.TextUtils.isDigitsOnly(tag)){
				other.addAll(items);
			} else {
				ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
				listRowAdapter.addAll(0, items);
				HeaderItem header = new HeaderItem(tag);
				mRowsAdapter.add(new ListRow(header, listRowAdapter));
			}
		}
		if(other.size() > 0){
			ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
			listRowAdapter.addAll(0,other);
			HeaderItem header = new HeaderItem("other");
			mRowsAdapter.add(new ListRow(header,listRowAdapter));
		}
		setAdapter(mRowsAdapter);
	}

	private void prepareBackgroundManager() {
		mBackgroundManager = BackgroundManager.getInstance(getActivity());
		mBackgroundManager.attach(getActivity().getWindow());
		mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
		mMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
	}

	private void setupUIElements() {
		Glide.with(getActivity())
				.load(conference.getLogoUrl())
				.centerCrop()
				.error(mDefaultBackground)
				.into(new SimpleTarget<GlideDrawable>(432, 243) {
					@Override
					public void onResourceReady(GlideDrawable resource,
												GlideAnimation<? super GlideDrawable>
														glideAnimation) {
						setBadgeDrawable(resource);
					}
				});
		setTitle(conference.getTitle()); // Badge, when set, takes precedent
		// over title
		setHeadersState(HEADERS_ENABLED);
		setHeadersTransitionOnBackEnabled(true);

		// set fastLane (or headers) background color
		setBrandColor(getResources().getColor(R.color.fastlane_background));
		// set search icon color
		setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));

	}

	private void setupEventListeners() {
		setOnItemViewClickedListener(new ItemViewClickedListener(this));
		setOnItemViewSelectedListener(new ItemViewSelectedListener());
	}

	protected void updateBackground(String uri) {
		int width = mMetrics.widthPixels;
		int height = mMetrics.heightPixels;
		Glide.with(getActivity())
				.load(uri)
				.centerCrop()
				.error(mDefaultBackground)
				.into(new SimpleTarget<GlideDrawable>(width, height) {
					@Override
					public void onResourceReady(GlideDrawable resource,
												GlideAnimation<? super GlideDrawable>
														glideAnimation) {
						mBackgroundManager.setDrawable(resource);
					}
				});
		mBackgroundTimer.cancel();
	}

	private void startBackgroundTimer() {
		if (null != mBackgroundTimer) {
			mBackgroundTimer.cancel();
		}
		mBackgroundTimer = new Timer();
		mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
	}

	private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
		@Override
		public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
								   RowPresenter.ViewHolder rowViewHolder, Row row) {
			if (item instanceof Event) {
				try {
					mBackgroundURI = new URI(((Event) item).getPosterUrl());
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				// TODO make configurable (enable/disable)
//				startBackgroundTimer();
			}

		}
	}

	private class UpdateBackgroundTask extends TimerTask {

		@Override
		public void run() {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (mBackgroundURI != null) {
						updateBackground(mBackgroundURI.toString());
					}
				}
			});

		}
	}
}
