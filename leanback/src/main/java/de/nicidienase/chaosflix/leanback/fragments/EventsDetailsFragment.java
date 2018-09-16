package de.nicidienase.chaosflix.leanback.fragments;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.DetailsSupportFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewLogoPresenter;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentConference;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentRecording;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Group;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Stream;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamUrl;
import de.nicidienase.chaosflix.common.viewmodel.DetailsViewModel;
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory;
import de.nicidienase.chaosflix.leanback.EventDetailsDescriptionPresenter;
import de.nicidienase.chaosflix.leanback.ItemViewClickedListener;
import de.nicidienase.chaosflix.leanback.activities.ConferencesActivity;
import de.nicidienase.chaosflix.leanback.activities.DetailsActivity;
import de.nicidienase.chaosflix.leanback.activities.EventDetailsActivity;
import de.nicidienase.chaosflix.leanback.activities.PlayerActivity;

/**
 * Created by felix on 18.03.17.
 */

public class EventsDetailsFragment extends DetailsSupportFragment {
	private static final int DETAIL_THUMB_WIDTH = 254;
	private static final int DETAIL_THUMB_HEIGHT = 143;
	private static final int NUM_RELATED_TALKS = 5;
	private static final int NUM_RANDOM_TALKS = NUM_RELATED_TALKS;

	private static final String TAG = EventsDetailsFragment.class.getSimpleName();
	public static final int FRAGMENT = R.id.details_fragment;
	public static final int DEFAULT_DRAWABLE = R.drawable.default_background;

	private static final long ACTION_PLAY = 1646465164;
	private static final long ACTION_ADD_WATCHLIST = 1646465165;
	private static final long ACTION_REMOVE_WATCHLIST = 1646465166;

	private PersistentEvent event;
	private Room room;
	private int eventType;
	private ArrayList<StreamUrl> streamUrlList;
	private BackgroundManager backgroundManager;
	private ArrayObjectAdapter recordingActionsAdapter;
	private OnActionClickedListener onActionClickedListener = new DetailActionClickedListener();
	private DetailsViewModel viewModel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		viewModel = ViewModelProviders.of(this, new ViewModelFactory(requireContext())).get(DetailsViewModel.class);

//		prepareBackgroundManager();
		final BrowseErrorFragment browseErrorFragment =
				BrowseErrorFragment.showErrorFragment(getFragmentManager(), FRAGMENT);
		eventType = getActivity().getIntent().getIntExtra(DetailsActivity.Companion.getTYPE(), -1);

		if (eventType == DetailsActivity.Companion.getTYPE_RECORDING()) {
			event = getActivity().getIntent()
					.getParcelableExtra(DetailsActivity.Companion.getEVENT());
			setTitle(event.getTitle());
//			viewModel.setEvent(event).observe(this, event1 -> {
//				List<PersistentRecording> recordings = event1.getRecordings();
//				if(recordings != null){
//					recordingActionsAdapter.setItems(recordings, new DiffCallback<PersistentRecording>() {
//						@Override
//						public boolean areItemsTheSame(@NonNull PersistentRecording oldItem, @NonNull PersistentRecording newItem) {
//							return oldItem.getUrl().equals(newItem.getUrl());
//						}
//
//						@Override
//						public boolean areContentsTheSame(@NonNull PersistentRecording oldItem, @NonNull PersistentRecording newItem) {
//							return oldItem.getRecordingUrl().equals(newItem.getRecordingUrl());
//						}
//					});
//				}
//			});
		} else if (eventType == DetailsActivity.Companion.getTYPE_STREAM()) {
			room = getActivity().getIntent()
					.getParcelableExtra(DetailsActivity.Companion.getROOM());
		}

		FullWidthDetailsOverviewRowPresenter mDetailsPresenter
				= setupDetailsOverviewRowPresenter();
		ClassPresenterSelector presenterSelector = new ClassPresenterSelector();
		presenterSelector.addClassPresenter(DetailsOverviewRow.class, mDetailsPresenter);
		presenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
		final ArrayObjectAdapter adapter = new ArrayObjectAdapter(presenterSelector);
		final DetailsOverviewRow detailsOverviewRow = setupDetailsOverviewRow(event);
		recordingActionsAdapter = getRecordingActionsAdapter();
		detailsOverviewRow.setActionsAdapter(recordingActionsAdapter);
		adapter.add(detailsOverviewRow);
		setOnItemViewClickedListener(new ItemViewClickedListener(this));
		setAdapter(adapter);
		browseErrorFragment.dismiss();

//		((LeanbackBaseActivity) getActivity()).getApiServiceObservable()
//		.doOnError(t -> browseErrorFragment.setErrorContent(t.getMessage()))
//		.subscribe(mediaApiService -> {
//			mMediaApiService = mediaApiService;
//			if (eventType == DetailsActivity.Companion.getTYPE_RECORDING()) {
//				final DetailsOverviewRow detailsOverviewRow = setupDetailsOverviewRow(event);
//				mediaApiService.getEvent(event.getApiID())
//						.doOnError(t -> browseErrorFragment.setErrorContent(t.getMessage()))
//						.subscribe(event -> {
//							this.event = event;
//							recordingActionsAdapter =
//									getRecordingActionsAdapter(this.event.getRecordings());
//							detailsOverviewRow.setActionsAdapter(recordingActionsAdapter);
//							adapter.add(detailsOverviewRow);
//							mediaApiService.getConference(
//									this.event.getConferenceId())
//									.observeOn(AndroidSchedulers.mainThread())
//									.subscribe(conference -> {
//										String tag = null;
//										if (this.event.getTags().size() > 0) {
//											tag = this.event.getTags().get(0);
//											List<Event> relatedEvents = conference.getEventsByTags().get(tag);
//											relatedEvents.remove(this.event);
//											Collections.shuffle(relatedEvents);
//											if (relatedEvents.size() > 5) {
//												relatedEvents = relatedEvents.subList(0, NUM_RELATED_TALKS);
//											}
//											ArrayObjectAdapter relatedEventsAdapter
//													= new ArrayObjectAdapter(new CardPresenter());
//											relatedEventsAdapter.addAll(0, relatedEvents);
//											HeaderItem header = new HeaderItem(getString(R.string.random_talks_on_this_track));
//											adapter.add(new ListRow(header, relatedEventsAdapter));
//										}
//
//										List<Event> selectedEvents = getRandomEvents(conference, tag);
//										if (selectedEvents.size() > 0) {
//											ArrayObjectAdapter randomEventAdapter
//													= new ArrayObjectAdapter(new CardPresenter());
//											randomEventAdapter.addAll(0, selectedEvents);
//											HeaderItem header = new HeaderItem(getString(R.string.random_talks));
//											adapter.add(new ListRow(header, randomEventAdapter));
//										}
//
//										setOnItemViewClickedListener(new ItemViewClickedListener(this));
//										setAdapter(adapter);
//										browseErrorFragment.dismiss();
//									});
//						});
//			} else if (eventType == DetailsActivity.Companion.getTYPE_STREAM()) {
//				mediaApiService.getStreamingConferences()
//						.observeOn(AndroidSchedulers.mainThread())
//						.subscribe(liveConferences -> {
//							if(room.getStreams() == null){
//								room = getRoom(room, liveConferences);
//							}
//							final DetailsOverviewRow detailsOverviewRow = setupDetailsOverviewRow(room);
//							ArrayObjectAdapter actionsAdapter = getStreamActionsAdapter(room.getStreams());
//							detailsOverviewRow.setActionsAdapter(actionsAdapter);
//							adapter.add(detailsOverviewRow);
//							setOnItemViewClickedListener(new ItemViewClickedListener(EventsDetailsFragment.this));
//							browseErrorFragment.dismiss();
//							setAdapter(adapter);
//							// TODO add other streams
//						});
//			}
//		});
//		new Handler().postDelayed(this::startEntranceTransition, 500);
		Log.d(TAG, "create finished");
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		backgroundManager.release();
		super.onStop();
	}

	private void prepareBackgroundManager() {
		backgroundManager = BackgroundManager.getInstance(getActivity());
		backgroundManager.attach(getActivity().getWindow());
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
	}

	private void updateBackground(String uri) {
		Glide.with(this)
				.load(uri)
				.asBitmap()
				.centerCrop()
				.into(new SimpleTarget<Bitmap>() {
					@Override
					public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
						backgroundManager.setBitmap(resource);
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
	private List<PersistentEvent> getRandomEvents(PersistentConference conference, String tag) {
		List<PersistentEvent> randomEvents = Collections.EMPTY_LIST; //conference.getEvents();
		Collections.shuffle(randomEvents);
		List<PersistentEvent> selectedEvents;
		if (tag != null) {
			selectedEvents = new ArrayList<PersistentEvent>();
			for (PersistentEvent e : randomEvents) {
				if (!Arrays.asList(e.getTags()).contains(tag)) {
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
		FullWidthDetailsOverviewRowPresenter detailsPresenter = new FullWidthDetailsOverviewRowPresenter(
				new EventDetailsDescriptionPresenter(getActivity()),
				new EventDetailsOverviewLogoPresenter());
		detailsPresenter.setBackgroundColor(getResources().getColor(R.color.selected_background));
		detailsPresenter.setInitialState(FullWidthDetailsOverviewRowPresenter.STATE_HALF);
		detailsPresenter.setAlignmentMode(FullWidthDetailsOverviewRowPresenter.ALIGN_MODE_START);

		FullWidthDetailsOverviewSharedElementHelper helper
				= new FullWidthDetailsOverviewSharedElementHelper();
		helper.setSharedElementEnterTransition(getActivity(),
				EventDetailsActivity.Companion.getSHARED_ELEMENT_NAME());
		detailsPresenter.setListener(helper);
		prepareEntranceTransition();

		detailsPresenter.setOnActionClickedListener(onActionClickedListener);
		return detailsPresenter;
	}

	private DetailsOverviewRow setupDetailsOverviewRow(Object event) {
		final DetailsOverviewRow row = new DetailsOverviewRow(event);
		String thumbUrl;
		if (event instanceof PersistentEvent) {
			thumbUrl = ((PersistentEvent) event).getThumbUrl();
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
						row.setImageBitmap(requireActivity(), resource);
					}

					@Override
					public void onLoadFailed(Exception e, Drawable errorDrawable) {
//						super.onLoadFailed(e, errorDrawable);
						row.setImageDrawable(getResources().getDrawable(DEFAULT_DRAWABLE));
					}
				});
		return row;
	}

	private ArrayObjectAdapter getRecordingActionsAdapter() {
		ArrayObjectAdapter actionsAdapter = new ArrayObjectAdapter();
		final Action watchlistAction = new Action(ACTION_ADD_WATCHLIST, getString(R.string.add_to_watchlist));
		actionsAdapter.add(watchlistAction);
		viewModel.getBookmarkForEvent(event.getGuid()).observe(this, watchlistItem -> {
			if (watchlistItem != null) {
				watchlistAction.setId(ACTION_REMOVE_WATCHLIST);
				watchlistAction.setLabel1(getString(R.string.remove_from_watchlist));
			} else {
				watchlistAction.setId(ACTION_ADD_WATCHLIST);
				watchlistAction.setLabel1(getString(R.string.add_to_watchlist));
			}
		});

		Action playAction = new Action(ACTION_PLAY, "Play");
		actionsAdapter.add(playAction);

//		if (recordings != null) {
//			for (int i = 0; i < recordings.size(); i++) {
//				PersistentRecording recording = recordings.get(i);
//				if (recording.getMimeType().startsWith("video/") && !recording.getMimeType().equals("video/webm")) {
//					String quality = recording.isHighQuality() ? "HD" : "SD";
//					String title = quality + " (" + recording.getLanguage() + ")";
//					actionsAdapter.add(new Action(recording.getId(), title, recording.getMimeType().substring(6)));
//				}
//			}
//		} else {
//			actionsAdapter.add(new Action(DUMMY_ID, "Dummy", "HLS"));
//		}
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

	private class DetailActionClickedListener implements OnActionClickedListener {
		@Override
		public void onActionClicked(Action action) {
			Log.d(TAG, "OnActionClicked");
			if (action.getId() == ACTION_ADD_WATCHLIST) {
//				new WatchlistItem(event.getApiID()).save();
				recordingActionsAdapter.replace(0, new Action(ACTION_REMOVE_WATCHLIST, EventsDetailsFragment.this.getString(R.string.remove_from_watchlist)));
				SharedPreferences preferences = requireActivity().getSharedPreferences(getString(R.string.watchlist_preferences_key), Context.MODE_PRIVATE);
				if (preferences.getBoolean(getString(R.string.watchlist_dialog_needed), true)) { // new item
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle(R.string.watchlist_message);
					builder.setNegativeButton(R.string.return_to_homescreen, (dialog, which) -> {
						Intent i = new Intent(getActivity(), ConferencesActivity.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(i);
//						getActivity().finish();
					});
					builder.setPositiveButton("OK", (dialog, which) -> {
					});
					SharedPreferences.Editor edit = preferences.edit();
					edit.putBoolean(getString(R.string.watchlist_dialog_needed), false);
					edit.apply();

					builder.create().show();
				}
			} else if (action.getId() == ACTION_REMOVE_WATCHLIST) {
				viewModel.removeBookmark(event.getGuid());
			} else if (action.getId() == ACTION_PLAY) {
				Intent i = new Intent(EventsDetailsFragment.this.getActivity(), PlayerActivity.class);
				i.putExtra(DetailsActivity.Companion.getTYPE(), eventType);
				if (eventType == DetailsActivity.Companion.getTYPE_RECORDING()) {
					i.putExtra(DetailsActivity.Companion.getEVENT(), event);
					for (PersistentRecording r : event.getRecordings()) {
						if (r.getId() == action.getId()) {
							i.putExtra(DetailsActivity.Companion.getRECORDING(), r);
							break;
						}
					}
				} else if (eventType == DetailsActivity.Companion.getTYPE_STREAM()) {
					i.putExtra(DetailsActivity.Companion.getROOM(), room);
					StreamUrl streamUrl = EventsDetailsFragment.this.getStreamUrlForActionId((int) action.getId());
					if (streamUrl != null) {
						i.putExtra(DetailsActivity.Companion.getSTREAM_URL(), streamUrl);
					} else {
						// TODO handle missing Stream
						return;
					}
				}
				EventsDetailsFragment.this.getActivity().startActivity(i);
			}
		}
	}
}
