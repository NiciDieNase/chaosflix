package de.nicidienase.chaosflix.fragments;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewLogoPresenter;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.nicidienase.chaosflix.CardPresenter;
import de.nicidienase.chaosflix.EventDetailsDescriptionPresenter;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.activities.DetailsActivity;
import de.nicidienase.chaosflix.activities.EventDetailsActivity;
import de.nicidienase.chaosflix.entities.Conference;
import de.nicidienase.chaosflix.entities.Event;
import de.nicidienase.chaosflix.entities.Recording;
import de.nicidienase.chaosflix.network.MediaCCCClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by felix on 18.03.17.
 */

public class EventsDetailsFragment extends DetailsFragment {
	private static final int DETAIL_THUMB_WIDTH = 254;
	private static final int DETAIL_THUMB_HEIGHT = 254;
	private static final int NUM_RELATED_TALKS = 5;
	private static final int NUM_RANDOM_TALKS = NUM_RELATED_TALKS;

	private static final String TAG = EventsDetailsFragment.class.getSimpleName();
	public static final int FRAGMENT = R.id.details_fragment;
	private Event mSelectedEvent;
	private ArrayObjectAdapter mRowsAdapter;

	private MediaCCCClient client = new MediaCCCClient();
	private ArrayObjectAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG,"onCreate");
		super.onCreate(savedInstanceState);

		final BrowseErrorFragment browseErrorFragment =
				BrowseErrorFragment.showErrorFragment(getFragmentManager(),FRAGMENT);
		mSelectedEvent = getActivity().getIntent()
				.getParcelableExtra(DetailsActivity.EVENT);

		final ArrayObjectAdapter adapter = setupDetailsOverviewRowPresenter();
		final DetailsOverviewRow detailsOverviewRow = setupDetailsOverviewRow(mSelectedEvent);


		client.getEvent(mSelectedEvent.getApiID()).enqueue(new Callback<Event>() {
			@Override
			public void onResponse(Call<Event> call, Response<Event> response) {
				mSelectedEvent = response.body();
				final ArrayObjectAdapter recordingActionsAdapter =
						getRecordingActionsAdapter(mSelectedEvent.getRecordings());
				detailsOverviewRow.setActionsAdapter(recordingActionsAdapter);
				adapter.add(detailsOverviewRow);

				client.getConference((int) mSelectedEvent.getParentConferenceID()).enqueue(new Callback<Conference>() {
					@Override
					public void onResponse(Call<Conference> call, Response<Conference> response) {
						Conference conference = response.body();
						String tag = null;
						if(mSelectedEvent.getTags().size()>0) {
							tag = mSelectedEvent.getTags().get(0);
							List<Event> relatedEvents = conference.getEventsByTags().get(tag);
							relatedEvents.remove(mSelectedEvent);
							Collections.shuffle(relatedEvents);
							if (relatedEvents.size() > 5) {
								relatedEvents = relatedEvents.subList(0, NUM_RELATED_TALKS );
							}
							ArrayObjectAdapter relatedEventsAdapter
									= new ArrayObjectAdapter(new CardPresenter());
							relatedEventsAdapter.addAll(0,relatedEvents);
							HeaderItem header = new HeaderItem(getString(R.string.related_talks));
							adapter.add(new ListRow(header,relatedEventsAdapter));
						}
						List<Event> randomEvents = conference.getEvents();
						Collections.shuffle(randomEvents);
						List<Event> selectedEvents;
						if(tag != null){
							selectedEvents = new ArrayList<Event>();
							for(Event e : randomEvents){
								if(!e.getTags().contains(tag)){
									selectedEvents.add(e);
								}
								if(selectedEvents.size()==5){
									break;
								}
							}
						} else {
							selectedEvents = randomEvents.subList(0, NUM_RANDOM_TALKS );
						}
						ArrayObjectAdapter randomEventAdapter
								= new ArrayObjectAdapter(new CardPresenter());
						randomEventAdapter.addAll(0,selectedEvents);
						HeaderItem header = new HeaderItem(getString(R.string.random_talks));
						adapter.add(new ListRow(header,randomEventAdapter));

						setAdapter(adapter);
						setOnItemViewClickedListener(
								new ItemViewClickedListener(EventsDetailsFragment.this));
						browseErrorFragment.dismiss();
					}

					@Override
					public void onFailure(Call<Conference> call, Throwable t) {
						Log.d(TAG,"Error loading conferences",t);
						browseErrorFragment.setErrorContent(t.getMessage());
						t.printStackTrace();
					}
				});
			}

			@Override
			public void onFailure(Call<Event> call, Throwable t) {
				Log.d(TAG,"Error loading conferences",t);
				browseErrorFragment.setErrorContent(t.getMessage());
				t.printStackTrace();
			}
		});



	}

	private ArrayObjectAdapter setupDetailsOverviewRowPresenter() {
		FullWidthDetailsOverviewRowPresenter mDetailsPresenter = new FullWidthDetailsOverviewRowPresenter(
				new EventDetailsDescriptionPresenter(),
				new EventDetailsOverviewLogoPresenter());
		mDetailsPresenter.setBackgroundColor(getResources().getColor(R.color.selected_background));
		mDetailsPresenter.setInitialState(FullWidthDetailsOverviewRowPresenter.STATE_HALF);
		mDetailsPresenter.setAlignmentMode(FullWidthDetailsOverviewRowPresenter.ALIGN_MODE_START);

		FullWidthDetailsOverviewSharedElementHelper helper
				= new FullWidthDetailsOverviewSharedElementHelper();
		helper.setSharedElementEnterTransition(getActivity(),
				EventDetailsActivity.SHARED_ELEMENT_NAME);
		mDetailsPresenter.setListener(helper);
		prepareEntranceTransition();

		mDetailsPresenter.setOnActionClickedListener(new OnActionClickedListener() {
			@Override
			public void onActionClicked(Action action) {
				// TODO start video
				Toast.makeText(getActivity(),"Starting Video",Toast.LENGTH_SHORT);
			}
		});

		ClassPresenterSelector mPresenterSelector = new ClassPresenterSelector();
		mPresenterSelector.addClassPresenter(DetailsOverviewRow.class,mDetailsPresenter);
		mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
		return new ArrayObjectAdapter(mPresenterSelector);
	}

	private DetailsOverviewRow setupDetailsOverviewRow(Event event) {
		final DetailsOverviewRow row = new DetailsOverviewRow(event);
		Glide.with(getActivity())
				.load(event.getThumbUrl())
				.asBitmap()
				.error(R.drawable.default_background)
				.into(new SimpleTarget<Bitmap>(DETAIL_THUMB_WIDTH,DETAIL_THUMB_HEIGHT) {
					@Override
					public void onResourceReady(Bitmap resource,
												GlideAnimation<? super Bitmap> glideAnimation) {
						row.setImageBitmap(getActivity(),resource);
						startEntranceTransition();
					}
				});
		return row;
	}

	private ArrayObjectAdapter getRecordingActionsAdapter(List<Recording> recordings) {
		ArrayObjectAdapter actionsAdapter = new ArrayObjectAdapter();
		for(int i = 0; i < recordings.size(); i++){
			Recording recording = recordings.get(i);
			if(recording.getMimeType().startsWith("video/") && !recording.getLanguage().contains("-")){
				String quality = recording.isHighQuality() ? "HD" : "SD";
				actionsAdapter.add(new Action(recording.getApiID(),quality, recording.getLanguage()));
			}
		}
		return actionsAdapter;
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
