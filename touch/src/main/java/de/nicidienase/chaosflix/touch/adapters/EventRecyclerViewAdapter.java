package de.nicidienase.chaosflix.touch.adapters;

import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;

import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.common.entities.recording.Event;

public class EventRecyclerViewAdapter extends ItemRecyclerViewAdapter<Event> {

	private final boolean areTagsUsefull;

	public EventRecyclerViewAdapter(Conference conference, OnListFragmentInteractionListener listener) {
		super(conference.getEvents(), listener);
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
				// Notify the active callbacks interface (the activity, if the
				// fragment is attached to one) that an item has been selected.
				mListener.onListFragmentInteraction(holder.mItem);
			}
		});
	}
}
