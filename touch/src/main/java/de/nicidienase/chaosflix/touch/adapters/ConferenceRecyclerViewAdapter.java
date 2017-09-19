package de.nicidienase.chaosflix.touch.adapters;

import com.bumptech.glide.Glide;

import java.util.List;

import de.nicidienase.chaosflix.common.entities.recording.Conference;

public class ConferenceRecyclerViewAdapter extends ItemRecyclerViewAdapter<Conference> {

	public ConferenceRecyclerViewAdapter(List<Conference> items, OnListFragmentInteractionListener<Conference> listener) {
		super(items, listener);
	}


	@Override
	public void onBindViewHolder(ItemRecyclerViewAdapter.ViewHolder holder, int position) {
		holder.mItem = mItems.get(position);
		holder.mTitleText.setText(mItems.get(position).getTitle());
		holder.mSubtitle.setText(mItems.get(position).getAcronym());
		Glide.with(holder.mIcon.getContext())
				.load(mItems.get(position).getLogoUrl())
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
