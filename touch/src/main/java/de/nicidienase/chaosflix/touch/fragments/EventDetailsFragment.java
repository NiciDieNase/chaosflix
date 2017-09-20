package de.nicidienase.chaosflix.touch.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;

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
		postponeEnterTransition();
		Transition transition = TransitionInflater.from(getContext())
				.inflateTransition(android.R.transition.move);
		transition.setDuration(getResources().getInteger(R.integer.anim_duration));
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
			Toast.makeText(v.getContext(),"Play the video",Toast.LENGTH_SHORT).show();
		});

		view.setTransitionName(getString(R.string.card));

		collapsingToolbar.setTitle(mEvent.getTitle());
//		mToolbar.setTitle(mEvent.getTitle());
		mTitleText.setText(mEvent.getTitle());

		mAppBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
			double v = (double) Math.abs(verticalOffset) / appBarLayout.getTotalScrollRange();
			Log.d(TAG,"Offset changed: " + v);
			appBarExpanded = v > 0.8;
			collapsingToolbar.setTitleEnabled(appBarExpanded);
//			invalidateOptionsMenu();
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
		Glide.with(getContext())
				.load(mEvent.getPosterUrl())
				.asBitmap()
				.fitCenter()
				.dontAnimate()
				.dontTransform()
//				.listener(new RequestListener<String, GlideDrawable>() {
//					@Override
//					public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
////						startPostponedEnterTransition();
//						return false;
//					}
//
//					@Override
//					public boolean onResourceReady(GlideDrawable resource, String model,
//													Target<GlideDrawable> target,
//													boolean isFromMemoryCache,
//													boolean isFirstResource) {
////						startPostponedEnterTransition();
//						return false;
//					}
//				})
				.into(new BitmapImageViewTarget(mThumbImage){
					@Override
					public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
						super.onResourceReady(resource, glideAnimation);
						mThumbImage.setImageBitmap(resource);
						Palette.from(resource).generate(palette -> {
							int vibrantColor = palette.getVibrantColor(getResources().getColor(R.color.primary_500));
							collapsingToolbar.setContentScrimColor(vibrantColor);
							collapsingToolbar.setStatusBarScrimColor(getResources().getColor(R.color.black_trans80));
						});
						startPostponedEnterTransition();
					}
				});
//				.into(mThumbImage);
		startPostponedEnterTransition();
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

	public interface OnEventDetailsFragmentInteractionListener {
		void onEventSelected(Event event, View v);
	}
}
