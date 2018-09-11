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

package de.nicidienase.chaosflix.leanback.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseSupportFragment;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentConference;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent;
import de.nicidienase.chaosflix.leanback.CardPresenter;
import de.nicidienase.chaosflix.leanback.ItemViewClickedListener;
import de.nicidienase.chaosflix.leanback.activities.EventsActivity;

public class EventsBrowseFragment extends BrowseSupportFragment {
	private static final String TAG = EventsBrowseFragment.class.getSimpleName();

	private static final int BACKGROUND_UPDATE_DELAY = 300;
	private static final int FRAGMENT = R.id.browse_fragment;

	private final Handler mHandler = new Handler();
	private ArrayObjectAdapter mRowsAdapter;
	private Drawable defaultBackground;
	private DisplayMetrics metrics;
	private Timer mBackgroundTimer;
	private URI mBackgroundURI;
	private BackgroundManager backgroundManager;
	private PersistentConference conference;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onActivityCreated(savedInstanceState);
		final BrowseErrorFragment errorFragment =
				BrowseErrorFragment.showErrorFragment(getFragmentManager(), FRAGMENT);

		conference = this.getActivity().getIntent().getParcelableExtra(EventsActivity.getCONFERENCE());
		setupUIElements(conference);

		CardPresenter cardPresenter = new CardPresenter();
		mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
		setAdapter(mRowsAdapter);

		prepareBackgroundManager();
		setOnItemViewClickedListener(new ItemViewClickedListener(this));
		setOnItemViewSelectedListener(new ItemViewSelectedListener());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (null != mBackgroundTimer) {
			Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
			mBackgroundTimer.cancel();
		}
	}

	private Map<String, List<PersistentEvent>> loadRows(List<PersistentEvent> events) {
		HashMap<String, List<PersistentEvent>> eventsByTags = new HashMap<>();

		List<PersistentEvent> other = new LinkedList<PersistentEvent>();
		List<String> keys = new ArrayList<>();
		Iterator<String> iterator = eventsByTags.keySet().iterator();
		while (iterator.hasNext()){
			keys.add(iterator.next());
		}
		Collections.sort(keys);
		for (String tag : keys) {
			List<PersistentEvent> items = eventsByTags.get(tag);
			Collections.sort(items);
			if (android.text.TextUtils.isDigitsOnly(tag)) {
				other.addAll(items);
			}
		}
		return eventsByTags;
	}

	private Row buildRowForEvents(CardPresenter cardPresenter,String tag, List<PersistentEvent> items){
		ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
		listRowAdapter.addAll(0, items);
		HeaderItem header = new HeaderItem(tag);
		return new ListRow(header, listRowAdapter);
	}

	private void prepareBackgroundManager() {
		backgroundManager = BackgroundManager.getInstance(getActivity());
		backgroundManager.attach(getActivity().getWindow());
		defaultBackground = getResources().getDrawable(R.drawable.default_background);
		metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
	}

	private void setupUIElements(PersistentConference conference) {
		Glide.with(getActivity())
				.load(conference.getLogoUrl())
				.centerCrop()
				.error(defaultBackground)
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

	protected void updateBackground(String uri) {
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;
		Glide.with(getActivity())
				.load(uri)
				.centerCrop()
				.error(defaultBackground)
				.into(new SimpleTarget<GlideDrawable>(width, height) {
					@Override
					public void onResourceReady(GlideDrawable resource,
												GlideAnimation<? super GlideDrawable>
														glideAnimation) {
						backgroundManager.setDrawable(resource);
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
			if (item instanceof PersistentEvent) {
				try {
					mBackgroundURI = new URI(((PersistentEvent) item).getPosterUrl());
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
