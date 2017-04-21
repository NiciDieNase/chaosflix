package de.nicidienase.chaosflix.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.nicidienase.chaosflix.CardPresenter;
import de.nicidienase.chaosflix.EventDetailsDescriptionPresenter;
import de.nicidienase.chaosflix.ItemViewClickedListener;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.activities.AbstractServiceConnectedAcitivty;
import de.nicidienase.chaosflix.activities.DetailsActivity;
import de.nicidienase.chaosflix.activities.EventDetailsActivity;
import de.nicidienase.chaosflix.activities.PlayerActivity;
import de.nicidienase.chaosflix.entities.WatchlistItem;
import de.nicidienase.chaosflix.entities.recording.Conference;
import de.nicidienase.chaosflix.entities.recording.Event;
import de.nicidienase.chaosflix.entities.recording.Recording;
import de.nicidienase.chaosflix.entities.streaming.Group;
import de.nicidienase.chaosflix.entities.streaming.LiveConference;
import de.nicidienase.chaosflix.entities.streaming.Room;
import de.nicidienase.chaosflix.entities.streaming.Stream;
import de.nicidienase.chaosflix.entities.streaming.StreamUrl;
import de.nicidienase.chaosflix.network.MediaApiService;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by felix on 18.03.17.
 */

public class EventsDetailsFragment extends DetailsFragment {
	private static final int DETAIL_THUMB_WIDTH = 254;
	private static final int DETAIL_THUMB_HEIGHT = 143;
	private static final int NUM_RELATED_TALKS = 5;
	private static final int NUM_RANDOM_TALKS = NUM_RELATED_TALKS;

	private static final String TAG = EventsDetailsFragment.class.getSimpleName();
	public static final int FRAGMENT = R.id.details_fragment;
	public static final int DUMMY_ID = 1646465164;
	public static final int DEFAULT_DRAWABLE = R.drawable.default_background;
	private static final long ADD_WATCHLIST_ACTION = 1646465165;
	private static final long REMOVE_WATCHLIST_ACTION = 1646465166;
	private Event mSelectedEvent;
	private MediaApiService mMediaApiService;
	private Room mRoom;
	private int eventType;
	private ArrayList<StreamUrl> streamUrlList;
	private BackgroundManager mBackgroundmanager;
	private DisplayMetrics mMetrics;
	private WatchlistItem mWatchlistItem;
	private ArrayObjectAdapter mRecordingActionsAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		prepareBackgroundManager();
		final BrowseErrorFragment browseErrorFragment =
				BrowseErrorFragment.showErrorFragment(getFragmentManager(), FRAGMENT);
		eventType = getActivity().getIntent().getIntExtra(DetailsActivity.TYPE, -1);

		if (eventType == DetailsActivity.TYPE_RECORDING) {
			mSelectedEvent = getActivity().getIntent()
					.getParcelableExtra(DetailsActivity.EVENT);
			mWatchlistItem = WatchlistItem.findById(WatchlistItem.class, mSelectedEvent.getApiID());
		} else if (eventType == DetailsActivity.TYPE_STREAM) {
			mRoom = getActivity().getIntent()
					.getParcelableExtra(DetailsActivity.ROOM);
		}

		FullWidthDetailsOverviewRowPresenter mDetailsPresenter
				= setupDetailsOverviewRowPresenter();
		ClassPresenterSelector mPresenterSelector = new ClassPresenterSelector();
		mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, mDetailsPresenter);
		mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
		final ArrayObjectAdapter adapter = new ArrayObjectAdapter(mPresenterSelector);

		((AbstractServiceConnectedAcitivty) getActivity()).getmApiServiceObservable()
				.doOnError(t -> browseErrorFragment.setErrorContent(t.getMessage()))
				.subscribe(mediaApiService -> {
					mMediaApiService = mediaApiService;
					if (eventType == DetailsActivity.TYPE_RECORDING) {
						final DetailsOverviewRow detailsOverviewRow = setupDetailsOverviewRow(mSelectedEvent);
						mediaApiService.getEvent(mSelectedEvent.getApiID())
								.doOnError(t -> browseErrorFragment.setErrorContent(t.getMessage()))
								.subscribe(event -> {
									mSelectedEvent = event;
									mRecordingActionsAdapter =
											getRecordingActionsAdapter(mSelectedEvent.getRecordings());
									detailsOverviewRow.setActionsAdapter(mRecordingActionsAdapter);
									adapter.add(detailsOverviewRow);
									mediaApiService.getConference(
											mSelectedEvent.getConferenceId())
											.observeOn(AndroidSchedulers.mainThread())
											.subscribe(conference -> {
												String tag = null;
												if (mSelectedEvent.getTags().size() > 0) {
													tag = mSelectedEvent.getTags().get(0);
													List<Event> relatedEvents = conference.getEventsByTags().get(tag);
													relatedEvents.remove(mSelectedEvent);
													Collections.shuffle(relatedEvents);
													if (relatedEvents.size() > 5) {
														relatedEvents = relatedEvents.subList(0, NUM_RELATED_TALKS);
													}
													ArrayObjectAdapter relatedEventsAdapter
															= new ArrayObjectAdapter(new CardPresenter());
													relatedEventsAdapter.addAll(0, relatedEvents);
													HeaderItem header = new HeaderItem(getString(R.string.random_talks_on_this_track));
													adapter.add(new ListRow(header, relatedEventsAdapter));
												}

												List<Event> selectedEvents = getRandomEvents(conference, tag);
												if (selectedEvents.size() > 0) {
													ArrayObjectAdapter randomEventAdapter
															= new ArrayObjectAdapter(new CardPresenter());
													randomEventAdapter.addAll(0, selectedEvents);
													HeaderItem header = new HeaderItem(getString(R.string.random_talks));
													adapter.add(new ListRow(header, randomEventAdapter));
												}

												setAdapter(adapter);
												ItemViewClickedListener listener = new ItemViewClickedListener(EventsDetailsFragment.this);
												setOnItemViewClickedListener(listener);
												browseErrorFragment.dismiss();
											});
								});
					} else if (eventType == DetailsActivity.TYPE_STREAM) {
						mediaApiService.getStreamingConferences()
								.observeOn(AndroidSchedulers.mainThread())
								.subscribe(liveConferences -> {
									if(mRoom.getStreams() == null){
										mRoom = getRoom(mRoom, liveConferences);
									}
									final DetailsOverviewRow detailsOverviewRow = setupDetailsOverviewRow(mRoom);
									ArrayObjectAdapter actionsAdapter = getStreamActionsAdapter(mRoom.getStreams());
									detailsOverviewRow.setActionsAdapter(actionsAdapter);
									adapter.add(detailsOverviewRow);
									setAdapter(adapter);
									setOnItemViewClickedListener(new ItemViewClickedListener(EventsDetailsFragment.this));
									browseErrorFragment.dismiss();
									// TODO add other streams
								});
					}
				});
	}

	@Override
	public void onStart() {
		super.onStart();
		startEntranceTransition();
	}

	@Override
	public void onStop() {
		mBackgroundmanager.release();
		super.onStop();
	}

	private void prepareBackgroundManager() {
		mBackgroundmanager = BackgroundManager.getInstance(getActivity());
		mBackgroundmanager.attach(getActivity().getWindow());
		mMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
	}

	private void updateBackground(String uri) {
		Glide.with(this)
				.load(uri)
				.asBitmap()
				.centerCrop()
				.into(new SimpleTarget<Bitmap>() {
					@Override
					public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
						mBackgroundmanager.setBitmap(resource);
					}
				});
	}

	private Room getRoom(Room room, List<LiveConference> liveConferences) {
		for (LiveConference con : liveConferences) {
			for (Group g : con.getGroups()) {
				for (Room r : g.getRooms()) {
					if (r.getSlug().equals(room.getSlug())) {
						return r;
					}
				}
			}
		}
		return null;
	}

	@NonNull
	private List<Event> getRandomEvents(Conference conference, String tag) {
		List<Event> randomEvents = conference.getEvents();
		Collections.shuffle(randomEvents);
		List<Event> selectedEvents;
		if (tag != null) {
			selectedEvents = new ArrayList<Event>();
			for (Event e : randomEvents) {
				if (!e.getTags().contains(tag)) {
					selectedEvents.add(e);
				}
				if (selectedEvents.size() == 5) {
					break;
				}
			}
		} else {
			selectedEvents = randomEvents.subList(0, NUM_RANDOM_TALKS);
		}
		return selectedEvents;
	}

	private FullWidthDetailsOverviewRowPresenter setupDetailsOverviewRowPresenter() {
		FullWidthDetailsOverviewRowPresenter mDetailsPresenter = new FullWidthDetailsOverviewRowPresenter(
				new EventDetailsDescriptionPresenter(getActivity()),
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

		mDetailsPresenter.setOnActionClickedListener(action -> {
			Log.d(TAG,"OnActionClicked");
			if(action.getId() == ADD_WATCHLIST_ACTION) {
				new WatchlistItem(mSelectedEvent.getApiID()).save();
				mRecordingActionsAdapter.replace(0,new Action(REMOVE_WATCHLIST_ACTION,getString(R.string.remove_from_watchlist)));
			} else if (action.getId() == REMOVE_WATCHLIST_ACTION) {
				if(mWatchlistItem != null){
					mWatchlistItem.delete();
				}
				mRecordingActionsAdapter.replace(0,new Action(ADD_WATCHLIST_ACTION,getString(R.string.add_to_watchlist)));
			} else {
				Intent i = new Intent(getActivity(), PlayerActivity.class);
				i.putExtra(DetailsActivity.TYPE, eventType);
				if (eventType == DetailsActivity.TYPE_RECORDING) {
					i.putExtra(DetailsActivity.EVENT, mSelectedEvent);
					if (action.getId() == DUMMY_ID) {
						Recording dummy = new Recording();
						dummy.setRecordingUrl("https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8");
						dummy.setMimeType("video/hls");
						dummy.setLanguage("eng");
						dummy.setHighQuality(true);
						i.putExtra(DetailsActivity.RECORDING, dummy);
					} else {
						for (Recording r : mSelectedEvent.getRecordings()) {
							if (r.getApiID() == action.getId()) {
								i.putExtra(DetailsActivity.RECORDING, r);
								break;
							}
						}
					}
				} else if (eventType == DetailsActivity.TYPE_STREAM) {
					i.putExtra(DetailsActivity.ROOM, mRoom);
					StreamUrl streamUrl = getStreamUrlForActionId((int) action.getId());
					if (streamUrl != null) {
						i.putExtra(DetailsActivity.STREAM_URL, streamUrl);
					} else {
						// TODO handle missing Stream
						return;
					}
				}
				getActivity().startActivity(i);
			}
		});
		return mDetailsPresenter;
	}

	private DetailsOverviewRow setupDetailsOverviewRow(Object event) {
		final DetailsOverviewRow row = new DetailsOverviewRow(event);
		String thumbUrl;
		if (event instanceof Event) {
			thumbUrl = ((Event) event).getThumbUrl();
		} else {
			thumbUrl = ((Room) event).getThumb();
		}
		Glide.with(getActivity())
				.load(thumbUrl)
				.asBitmap()
				.into(new SimpleTarget<Bitmap>(DETAIL_THUMB_WIDTH, DETAIL_THUMB_HEIGHT) {
					@Override
					public void onResourceReady(Bitmap resource,
												GlideAnimation<? super Bitmap> glideAnimation) {
						row.setImageBitmap(getActivity(), resource);
					}

					@Override
					public void onLoadFailed(Exception e, Drawable errorDrawable) {
//						super.onLoadFailed(e, errorDrawable);
						row.setImageDrawable(getResources().getDrawable(DEFAULT_DRAWABLE));
					}
				});
		return row;
	}

	private ArrayObjectAdapter getRecordingActionsAdapter(List<Recording> recordings) {
		ArrayObjectAdapter actionsAdapter = new ArrayObjectAdapter();
		if(mWatchlistItem != null){
			actionsAdapter.add(new Action(REMOVE_WATCHLIST_ACTION,getString(R.string.remove_from_watchlist)));
		} else {
			actionsAdapter.add(new Action(ADD_WATCHLIST_ACTION,getString(R.string.add_to_watchlist)));
		}
		if (recordings != null) {
			for (int i = 0; i < recordings.size(); i++) {
				Recording recording = recordings.get(i);
				if (recording.getMimeType().startsWith("video/") || true) {
					String quality = recording.isHighQuality() ? "HD" : "SD";
					String title = quality + " (" + recording.getLanguage() + ")";
					actionsAdapter.add(new Action(recording.getApiID(), title, recording.getMimeType().substring(6)));
				}
			}
		} else {
			actionsAdapter.add(new Action(DUMMY_ID, "Dummy", "HLS"));
		}
		return actionsAdapter;
	}

	private ArrayObjectAdapter getStreamActionsAdapter(List<Stream> streams) {
		ArrayObjectAdapter actionsAdapter = new ArrayObjectAdapter();
		streamUrlList = new ArrayList<StreamUrl>();
		for (Stream s : streams) {
			if (s.getType().equals("video") || true)
				for (String key : s.getUrls().keySet()) {
					StreamUrl url = s.getUrls().get(key);
					int index = streamUrlList.size();
					streamUrlList.add(url);
					actionsAdapter.add(new Action(index, s.getDisplay(), url.getDisplay()));
				}
		}
		return actionsAdapter;
	}

	private StreamUrl getStreamUrlForActionId(int actionId) {
		if (streamUrlList != null && streamUrlList.size() > actionId) {
			return streamUrlList.get(actionId);
		} else {
			return null;
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
