package de.nicidienase.chaosflix.touch.adapters;

import com.bumptech.glide.Glide;

import java.util.Collections;

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
		Glide.with(holder.mIcon.getContext())
				.load(event.getThumbUrl())
				.fitCenter()
				.into(holder.mIcon);

		holder.mView.setOnClickListener(v -> {
			if (null != mListener) {
				mListener.onEventSelected((Event) holder.mItem);
			}
		});
	}
}
