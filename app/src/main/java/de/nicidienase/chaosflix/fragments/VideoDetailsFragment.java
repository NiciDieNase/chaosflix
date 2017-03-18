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

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.DetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.DisplayMetrics;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.List;

import de.nicidienase.chaosflix.DetailsDescriptionPresenter;
import de.nicidienase.chaosflix.activities.PlaybackOverlayActivity;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.Utils;
import de.nicidienase.chaosflix.activities.DetailsActivity;
import de.nicidienase.chaosflix.activities.MainActivity;
import de.nicidienase.chaosflix.entities.Event;
import de.nicidienase.chaosflix.entities.Movie;
import de.nicidienase.chaosflix.entities.Recording;
import de.nicidienase.chaosflix.network.MediaCCCClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * LeanbackDetailsFragment extends DetailsFragment, a Wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its meta plus related videos.
 */
public class VideoDetailsFragment extends DetailsFragment {
	private static final String TAG = "VideoDetailsFragment";

	private static final int DETAIL_THUMB_WIDTH = 274;
	private static final int DETAIL_THUMB_HEIGHT = 274;

	private static final int NUM_COLS = 10;

	private Event mSelectedEvent;

	private ArrayObjectAdapter mAdapter;
	private ClassPresenterSelector mPresenterSelector;

	private BackgroundManager mBackgroundManager;
	private Drawable mDefaultBackground;
	private DisplayMetrics mMetrics;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate DetailsFragment");
		super.onCreate(savedInstanceState);

		prepareBackgroundManager();

		mSelectedEvent = getActivity().getIntent()
				.getParcelableExtra(DetailsActivity.EVENT);
		if (mSelectedEvent != null) {
			setupAdapter();
			setupDetailsOverviewRowPresenter();
			mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
			updateBackground(mSelectedEvent.getPosterUrl());
			setOnItemViewClickedListener(new ItemViewClickedListener());
		} else {
			Intent intent = new Intent(getActivity(), MainActivity.class);
			startActivity(intent);
		}

		new MediaCCCClient().getEvent(mSelectedEvent.getEventID()).enqueue(new Callback<Event>() {
			@Override
			public void onResponse(Call<Event> call, Response<Event> response) {
				mSelectedEvent = response.body();
				setupDetailsOverviewRow();
			}

			@Override
			public void onFailure(Call<Event> call, Throwable t) {

			}
		});
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	private void prepareBackgroundManager() {
		mBackgroundManager = BackgroundManager.getInstance(getActivity());
		mBackgroundManager.attach(getActivity().getWindow());
		mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
		mMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
	}

	protected void updateBackground(String uri) {
		Glide.with(getActivity())
				.load(uri)
				.centerCrop()
				.error(mDefaultBackground)
				.into(new SimpleTarget<GlideDrawable>(mMetrics.widthPixels, mMetrics.heightPixels) {
					@Override
					public void onResourceReady(GlideDrawable resource,
												GlideAnimation<? super GlideDrawable> glideAnimation) {
						mBackgroundManager.setDrawable(resource);
					}
				});
	}

	private void setupAdapter() {
		mPresenterSelector = new ClassPresenterSelector();
		mAdapter = new ArrayObjectAdapter(mPresenterSelector);
		setAdapter(mAdapter);
	}

	private void setupDetailsOverviewRow() {
		Log.d(TAG, "doInBackground: " + mSelectedEvent.toString());
		final DetailsOverviewRow row = new DetailsOverviewRow(mSelectedEvent);
		row.setImageDrawable(getResources().getDrawable(R.drawable.default_background));
		int width = Utils.convertDpToPixel(getActivity()
				.getApplicationContext(), DETAIL_THUMB_WIDTH);
		int height = Utils.convertDpToPixel(getActivity()
				.getApplicationContext(), DETAIL_THUMB_HEIGHT);
		Glide.with(getActivity())
				.load(mSelectedEvent.getThumbUrl())
				.centerCrop()
				.error(R.drawable.default_background)
				.into(new SimpleTarget<GlideDrawable>(width, height) {
					@Override
					public void onResourceReady(GlideDrawable resource,
												GlideAnimation<? super GlideDrawable>
														glideAnimation) {
						Log.d(TAG, "details overview card image url ready: " + resource);
						row.setImageDrawable(resource);
						mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
					}
				});

		List<Recording> recordings = mSelectedEvent.getRecordings();
		for(int i = 0; i < recordings.size(); i++){
			if(recordings.get(i).getMimeType().startsWith("video/") && !recordings.get(i).getLanguage().contains("-")){
				String quality = recordings.get(i).isHighQuality() ? "HD" : "SD";
				int id = recordings.get(i).getLanguage().equals(mSelectedEvent.getOriginalLanguage()) ? 0 : 1;
				row.addAction(new Action(i,quality,recordings.get(i).getLanguage()));
			}
		}

		mAdapter.add(row);
	}

	private void setupDetailsOverviewRowPresenter() {
		// Set detail background and style.
		DetailsOverviewRowPresenter detailsPresenter =
				new DetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
		detailsPresenter.setBackgroundColor(getResources().getColor(R.color.selected_background));
		detailsPresenter.setStyleLarge(true);

		// Hook up transition element.
		detailsPresenter.setSharedElementEnterTransition(getActivity(),
				DetailsActivity.SHARED_ELEMENT_NAME);

		detailsPresenter.setOnActionClickedListener(new OnActionClickedListener() {
			@Override
			public void onActionClicked(Action action) {
				Intent intent = new Intent(getActivity(), PlaybackOverlayActivity.class);
				intent.putExtra(DetailsActivity.EVENT, mSelectedEvent);
				intent.putExtra(DetailsActivity.RECORDING, action.getId());
				startActivity(intent);
			}
		});
		mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
	}

	private final class ItemViewClickedListener implements OnItemViewClickedListener {
		@Override
		public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
								  RowPresenter.ViewHolder rowViewHolder, Row row) {

			if (item instanceof Movie) {
				Movie movie = (Movie) item;
				Log.d(TAG, "Item: " + item.toString());
				Intent intent = new Intent(getActivity(), DetailsActivity.class);
				intent.putExtra(getResources().getString(R.string.movie), mSelectedEvent);
				intent.putExtra(getResources().getString(R.string.should_start), true);
				startActivity(intent);


				Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
						getActivity(),
						((ImageCardView) itemViewHolder.view).getMainImageView(),
						DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
				getActivity().startActivity(intent, bundle);
			}
		}
	}
}
