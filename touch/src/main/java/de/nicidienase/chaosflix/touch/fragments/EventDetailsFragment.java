package de.nicidienase.chaosflix.touch.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import de.nicidienase.chaosflix.common.entities.recording.Event;
import de.nicidienase.chaosflix.common.entities.recording.Recording;
import de.nicidienase.chaosflix.databinding.FragmentEventDetailsNewBinding;

public class EventDetailsFragment extends Fragment {
	private static final String TAG = EventDetailsFragment.class.getSimpleName();
	private static final String EVENT_PARAM = "event_param";

	private OnEventDetailsFragmentInteractionListener mListener;
	private Event mEvent;

	private boolean appBarExpanded;

	public EventDetailsFragment() {
		// Required empty public constructor
	}

	public static EventDetailsFragment newInstance(Event event) {
		EventDetailsFragment fragment = new EventDetailsFragment();
		Bundle args = new Bundle();
		args.putParcelable(EVENT_PARAM,event);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
		postponeEnterTransition();
		Transition transition = TransitionInflater.from(getContext())
				.inflateTransition(android.R.transition.move);
//		transition.setDuration(getResources().getInteger(R.integer.anim_duration));
		setSharedElementEnterTransition(transition);

		if (getArguments() != null) {
			mEvent = getArguments().getParcelable(EVENT_PARAM);
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
		binding.setEvent(mEvent);

		binding.playFab.setOnClickListener(v -> {
			play();
		});

		if(mListener != null)
			mListener.setActionbar(binding.animToolbar);

		binding.appbar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
			double v = (double) Math.abs(verticalOffset) / appBarLayout.getTotalScrollRange();
			if(appBarExpanded ^ v > 0.8){
				if(mListener != null)
					mListener.onToolbarStateChange();
				appBarExpanded = v > 0.8;
				binding.collapsingToolbar.setTitleEnabled(appBarExpanded);
			}
		});

		binding.thumbImage.setTransitionName(getString(R.string.thumbnail)+mEvent.getApiID());
		Picasso.with(getContext())
				.load(mEvent.getThumbUrl())
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
	}

	private void play() {
		if(mListener != null){
			mListener.playItem(mEvent,mEvent.getOptimalStream());
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnEventDetailsFragmentInteractionListener) {
			mListener = (OnEventDetailsFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if(appBarExpanded)
			inflater.inflate(R.menu.details_menu,menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.play:
				play();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public interface OnEventDetailsFragmentInteractionListener {
		void onToolbarStateChange();
		void setActionbar(Toolbar toolbar);
		void playItem(Event event, Recording recording);
	}
}
