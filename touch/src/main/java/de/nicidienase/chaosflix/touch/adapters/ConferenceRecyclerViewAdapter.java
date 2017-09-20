package de.nicidienase.chaosflix.touch.adapters;

import com.bumptech.glide.Glide;

import java.util.List;

import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.touch.fragments.ConferencesTabBrowseFragment;

public class ConferenceRecyclerViewAdapter extends ItemRecyclerViewAdapter<Conference> {

	private final ConferencesTabBrowseFragment.OnConferenceListFragmentInteractionListener mListener;

	public ConferenceRecyclerViewAdapter(List<Conference> items, ConferencesTabBrowseFragment.OnConferenceListFragmentInteractionListener listener) {
		super(items);
		mListener = listener;
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
				mListener.onConferenceSelected((Conference) holder.mItem);
			}
		});
	}
}
