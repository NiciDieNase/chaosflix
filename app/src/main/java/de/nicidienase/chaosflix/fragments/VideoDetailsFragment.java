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
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewLogoPresenter;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import android.support.v17.leanback.widget.HeaderItem;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.List;

import de.nicidienase.chaosflix.CardPresenter;
import de.nicidienase.chaosflix.EventDetailsDescriptionPresenter;
import de.nicidienase.chaosflix.activities.PlaybackOverlayActivity;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.Utils;
import de.nicidienase.chaosflix.activities.DetailsActivity;
import de.nicidienase.chaosflix.activities.EventsActivity;
import de.nicidienase.chaosflix.entities.Conference;
import de.nicidienase.chaosflix.entities.Event;
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

	private Event mSelectedEvent;

	private ArrayObjectAdapter mAdapter;
	private ClassPresenterSelector mPresenterSelector;

	private BackgroundManager mBackgroundManager;
	private Drawable mDefaultBackground;
	private DisplayMetrics mMetrics;
	private FullWidthDetailsOverviewRowPresenter detailsPresenter;

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
			Intent intent = new Intent(getActivity(), EventsActivity.class);
			startActivity(intent);
		}

		new MediaCCCClient().getEvent(mSelectedEvent.getApiID()).enqueue(new Callback<Event>() {
			@Override
			public void onResponse(Call<Event> call, Response<Event> response) {
				mSelectedEvent = response.body();
				setupDetailsOverviewRow();
			}

			@Override
			public void onFailure(Call<Event> call, Throwable t) {

			}
		});
		setupRelatedEvents();
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
//        mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
//        mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
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
		detailsPresenter = new FullWidthDetailsOverviewRowPresenter(
					new EventDetailsDescriptionPresenter(), new EventDetailsOverviewLogoPresenter());
		detailsPresenter.setBackgroundColor(getResources().getColor(R.color.selected_background));
		FullWidthDetailsOverviewSharedElementHelper listener = new FullWidthDetailsOverviewSharedElementHelper();
		listener.setSharedElementEnterTransition(getActivity(),DetailsActivity.SHARED_ELEMENT_NAME);
		detailsPresenter.setListener(listener);
		detailsPresenter.setAlignmentMode(FullWidthDetailsOverviewRowPresenter.ALIGN_MODE_MIDDLE);

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

	private void setupRelatedEvents(){
		new MediaCCCClient().getConference(101).enqueue(new Callback<Conference>() {
			@Override
			public void onResponse(Call<Conference> call, Response<Conference> response) {
				Conference conference = response.body();
				if(mSelectedEvent.getTags().size()>0){

					List<Event> events = conference.getEventsByTags().get(mSelectedEvent.getTags().get(0));
					CardPresenter cardPresenter = new CardPresenter();

					ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
					listRowAdapter.addAll(0, events);
					HeaderItem header = new HeaderItem("Related Talks");
					mAdapter.add(new ListRow(header, listRowAdapter));
				}
			}

			@Override
			public void onFailure(Call<Conference> call, Throwable t) {

			}
		});
	}

	private final class ItemViewClickedListener implements OnItemViewClickedListener {
		@Override
		public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
								  RowPresenter.ViewHolder rowViewHolder, Row row) {

			if (item instanceof Event) {
				Event event = (Event) item;
				Log.d(TAG, "Item: " + event.getTitle());
				Intent intent = new Intent(getActivity(), DetailsActivity.class);
				intent.putExtra(DetailsActivity.EVENT, mSelectedEvent);
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

	static class EventDetailsOverviewLogoPresenter extends DetailsOverviewLogoPresenter {
		static class ViewHolder extends DetailsOverviewLogoPresenter.ViewHolder {
			public ViewHolder(View view) {
				super(view);
			}

			public FullWidthDetailsOverviewRowPresenter getParentPresenter() {
				return mParentPresenter;
			}

			public FullWidthDetailsOverviewRowPresenter.ViewHolder getParentViewHolder() {
				return mParentViewHolder;
			}
		}

		@Override
		public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
			ImageView imageView = (ImageView) LayoutInflater.from(parent.getContext())
					.inflate(R.layout.lb_fullwidth_details_overview_logo, parent, false);

			Resources res = parent.getResources();
			int width = res.getDimensionPixelSize(R.dimen.detail_thumb_width);
			int height = res.getDimensionPixelSize(R.dimen.detail_thumb_height);
			imageView.setLayoutParams(new ViewGroup.MarginLayoutParams(width, height));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

			return new ViewHolder(imageView);
		}

		@Override
		public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
			DetailsOverviewRow row = (DetailsOverviewRow) item;
			ImageView imageView = ((ImageView) viewHolder.view);
			imageView.setImageDrawable(row.getImageDrawable());
			if (isBoundToImage((ViewHolder) viewHolder, row)) {
				EventDetailsOverviewLogoPresenter.ViewHolder vh =
						(EventDetailsOverviewLogoPresenter.ViewHolder) viewHolder;
				vh.getParentPresenter().notifyOnBindLogo(vh.getParentViewHolder());
			}
		}
	}
}
