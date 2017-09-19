package de.nicidienase.chaosflix.touch.adapters;

import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;

import de.nicidienase.chaosflix.common.entities.recording.Event;

public class EventRecyclerViewAdapter extends ItemRecyclerViewAdapter<Event> {

	public EventRecyclerViewAdapter(List<Event> items, OnListFragmentInteractionListener listener) {
		super(items, listener);
		Collections.sort(mItems,(o1, o2) -> o1.getTitle().compareTo(o2.getTitle()));
	}

	@Override
	public void onBindViewHolder(ItemRecyclerViewAdapter.ViewHolder holder, int position) {
		holder.mItem = mItems.get(position);
		holder.mTitleText.setText(mItems.get(position).getTitle());
		holder.mSubtitle.setText(mItems.get(position).getSubtitle());
		Glide.with(holder.mIcon.getContext())
				.load(mItems.get(position).getThumbUrl())
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
