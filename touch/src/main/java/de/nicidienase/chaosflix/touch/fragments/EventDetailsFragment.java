package de.nicidienase.chaosflix.touch.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent;
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentRecording;
import de.nicidienase.chaosflix.common.entities.userdata.WatchlistItem;
import de.nicidienase.chaosflix.databinding.FragmentEventDetailsNewBinding;
import de.nicidienase.chaosflix.touch.Util;

public class EventDetailsFragment extends BrowseFragment {
	private static final String TAG = EventDetailsFragment.class.getSimpleName();
	private static final String EVENT_PARAM = "event_param";

	private OnEventDetailsFragmentInteractionListener listener;

	private long eventId;
	private boolean appBarExpanded;
	private PersistentEvent event;
	private WatchlistItem watchlistItem;

	public static EventDetailsFragment newInstance(long eventId) {
		EventDetailsFragment fragment = new EventDetailsFragment();
		Bundle args = new Bundle();
		args.putLong(EVENT_PARAM, eventId);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		postponeEnterTransition();
		Transition transition = TransitionInflater.from(getContext())
				.inflateTransition(android.R.transition.move);
//		transition.setDuration(getResources().getInteger(R.integer.anim_duration));
		setSharedElementEnterTransition(transition);

		if (getArguments() != null) {
			eventId = getArguments().getLong(EVENT_PARAM);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_event_details_new, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		FragmentEventDetailsNewBinding binding = FragmentEventDetailsNewBinding.bind(view);
		binding.playFab.setOnClickListener(v -> {
			play();
		});
		if (listener != null)
			listener.setActionbar(binding.animToolbar);

		binding.appbar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
			double v = (double) Math.abs(verticalOffset) / appBarLayout.getTotalScrollRange();
			if (appBarExpanded ^ v > 0.8) {
				if (listener != null) {
					listener.onToolbarStateChange();
				}
				appBarExpanded = v > 0.8;
				binding.collapsingToolbar.setTitleEnabled(appBarExpanded);
			}
		});

		getViewModel().getEventById(eventId)
				.observe(this, event -> {
					this.event = event;
					updateBookmark();
					binding.setEvent(event);
					binding.thumbImage.setTransitionName(getString(R.string.thumbnail) + event.getEventId());
					Picasso.with(getContext())
							.load(event.getThumbUrl())
							.noFade()
							.into(binding.thumbImage, new Callback() {
								@Override
								public void onSuccess() {
									startPostponedEnterTransition();
								}

								@Override
								public void onError() {
									startPostponedEnterTransition();
								}
							});
				});
	}

	private void updateBookmark() {
		getViewModel().getBookmarkForEvent(eventId)
				.observe(this,watchlistItem -> {
					this.watchlistItem = watchlistItem;
					listener.invalidateOptionsMenu();
				});
	}

	private void play() {
		if (listener != null && event != null) {
			getViewModel().getRecordingForEvent(eventId)
					.observe(this, persistentRecordings
							-> listener.playItem(event,
							Util.INSTANCE.getOptimalStream(persistentRecordings)));
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnEventDetailsFragmentInteractionListener) {
			listener = (OnEventDetailsFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		Log.d(TAG,"OnPrepareOptionsMenu");
		super.onPrepareOptionsMenu(menu);
		if(watchlistItem != null){
			menu.findItem(R.id.action_bookmark).setVisible(false);
			menu.findItem(R.id.action_unbookmark).setVisible(true);
		} else {
			menu.findItem(R.id.action_bookmark).setVisible(true);
			menu.findItem(R.id.action_unbookmark).setVisible(false);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
//		if (appBarExpanded)
		inflater.inflate(R.menu.details_menu, menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_play:
				play();
				return true;
			case R.id.action_bookmark:
				getViewModel().createBookmark(eventId);
				updateBookmark();
				return true;
			case R.id.action_unbookmark:
				getViewModel().removeBookmark(eventId);
				watchlistItem = null;
				listener.invalidateOptionsMenu();
				return true;
			case R.id.action_download:
				Snackbar.make(getView(), "Download not yet implemented", Snackbar.LENGTH_LONG).show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public interface OnEventDetailsFragmentInteractionListener {
		void onToolbarStateChange();
		void setActionbar(Toolbar toolbar);
		void invalidateOptionsMenu();
		void playItem(PersistentEvent event, PersistentRecording recording);
	}
}
