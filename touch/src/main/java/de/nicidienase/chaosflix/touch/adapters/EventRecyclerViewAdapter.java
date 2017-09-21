package de.nicidienase.chaosflix.touch.adapters;

import android.content.res.Resources;
import android.support.v4.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.Collections;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.common.entities.recording.Event;
import de.nicidienase.chaosflix.touch.fragments.EventsFragment;

public class EventRecyclerViewAdapter extends ItemRecyclerViewAdapter<Event> {

	private final boolean areTagsUsefull;
	private final EventsFragment.OnEventsListFragmentInteractionListener mListener;

	public EventRecyclerViewAdapter(Conference conference, EventsFragment.OnEventsListFragmentInteractionListener listener) {
		super(conference.getEvents());
		mListener = listener;
		areTagsUsefull = conference.areTagsUsefull();
		Collections.sort(mItems,(o1, o2) -> o1.getTitle().compareTo(o2.getTitle()));
	}

	@Override
	public void onBindViewHolder(ItemRecyclerViewAdapter.ViewHolder holder, int position) {
		Event event = mItems.get(position);

		holder.mItem = event;
		holder.mTitleText.setText(event.getTitle());
		holder.mSubtitle.setText(event.getSubtitle());
		if(areTagsUsefull){
			StringBuilder tagString = new StringBuilder();
			for(String tag: event.getTags()){
				if(tagString.length() > 0) {
					tagString.append(", ");
				}
				tagString.append(tag);
			}
			holder.mTag.setText(tagString);
		}
		Picasso.with(holder.mIcon.getContext())
				.load(event.getPosterUrl())
				.noFade()
				.fit()
				.centerInside()
				.into(holder.mIcon);
//		Glide.with(holder.mIcon.getContext())
//				.load(event.getThumbUrl())
//				.dontAnimate()
//				.fitCenter()
//				.into(holder.mIcon);

		Resources resources = holder.mTitleText.getContext().getResources();
		ViewCompat.setTransitionName(holder.mTitleText,
				resources.getString(R.string.title)+event.getApiID());
		ViewCompat.setTransitionName(holder.mSubtitle,
				resources.getString(R.string.subtitle)+event.getApiID());
		ViewCompat.setTransitionName(holder.mIcon,
				resources.getString(R.string.thumbnail)+event.getApiID());

		holder.mView.setOnClickListener(v -> {
			if (null != mListener) {
				mListener.onEventSelected((Event) holder.mItem, v);
			}
		});
	}
}
