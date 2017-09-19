package de.nicidienase.chaosflix.touch.fragments;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Event;

public class EventDetailsFragment extends Fragment {
	private static final String EVENT_PARAM = "event_param";

	private OnEventDetailsFragmentInteractionListener mListener;
	private Event mEvent;

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
		return inflater.inflate(R.layout.fragment_event_details, container, false);

	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.bind(this,view);

		view.setTransitionName(getString(R.string.card));

		mTitleText.setText(mEvent.getTitle());
		mTitleText.setTransitionName(getString(R.string.title)+mEvent.getApiID());

		if(mEvent.getSubtitle() != null && mEvent.getSubtitle().length() > 0){
			mSubtitleText.setText(mEvent.getSubtitle());
			mSubtitleText.setTransitionName(getString(R.string.subtitle)+mEvent.getApiID());
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
				.fitCenter()
				.dontAnimate()
				.override(1024,768)
				.listener(new RequestListener<String, GlideDrawable>() {
					@Override
					public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
						startPostponedEnterTransition();
						return false;
					}

					@Override
					public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
						startPostponedEnterTransition();
						return false;
					}
				})
				.into(mThumbImage);
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

	public interface OnEventDetailsFragmentInteractionListener {
		void onEventSelected(Event event, View v);
	}
}
