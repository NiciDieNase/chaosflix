package de.nicidienase.chaosflix.touch.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Event;

public class EventDetailsFragment extends Fragment {
	private static final String TAG = EventDetailsFragment.class.getSimpleName();
	private static final String EVENT_PARAM = "event_param";

	private OnEventDetailsFragmentInteractionListener mListener;
	private Event mEvent;

	@BindView(R.id.collapsing_toolbar)
	CollapsingToolbarLayout collapsingToolbar;
	@BindView(R.id.anim_toolbar)
	Toolbar mToolbar;
	@BindView(R.id.appbar)
	AppBarLayout mAppBarLayout;
	@BindView(R.id.title_text)
	TextView mTitleText;
	@BindView(R.id.subtitle_text)
	TextView mSubtitleText;
	@BindView(R.id.speaker_text)
	TextView mSpeakerText;
	@BindView(R.id.thumb_image)
	ImageView mThumbImage;
	@BindView(R.id.description_text)
	TextView mDescriptionText;
	@BindView(R.id.play_fab)
	FloatingActionButton mPlayButton;

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
		ButterKnife.bind(this,view);

		mPlayButton.setOnClickListener(v -> {
			play();
		});

		view.setTransitionName(getString(R.string.card));

		if(mListener != null)
			mListener.setActionbar(mToolbar);
		collapsingToolbar.setTitle(mEvent.getTitle());
//		mToolbar.setTitle(mEvent.getTitle());
		mTitleText.setText(mEvent.getTitle());

		mAppBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
			double v = (double) Math.abs(verticalOffset) / appBarLayout.getTotalScrollRange();
			Log.d(TAG,"Offset changed: " + v);
			if(appBarExpanded ^ v > 0.8){
//				invalidateOptionsMenu();
				if(mListener != null)
					mListener.onToolbarStateChange();
				appBarExpanded = v > 0.8;
				collapsingToolbar.setTitleEnabled(appBarExpanded);
			}
		});

		if(mEvent.getSubtitle() != null && mEvent.getSubtitle().length() > 0){
			mSubtitleText.setText(mEvent.getSubtitle());
		} else {
			mSubtitleText.setVisibility(View.GONE);
		}
		mSpeakerText.setText(
				android.text.TextUtils.join(", ",mEvent.getPersons()));
		StringBuilder sb = new StringBuilder();
		sb.append(mEvent.getDescription())
				.append("\n")
				.append("\nreleased at: ").append(mEvent.getReleaseDate())
				.append("\nTags: ").append(android.text.TextUtils.join(", ", mEvent.getTags()));
		mDescriptionText.setText(sb);

		mThumbImage.setTransitionName(getString(R.string.thumbnail)+mEvent.getApiID());
		Picasso.with(getContext())
				.load(mEvent.getThumbUrl())
				.noFade()
				.into(mThumbImage, new Callback() {
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
		Toast.makeText(getContext(),"Play the video",Toast.LENGTH_SHORT).show();
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
	}
}
