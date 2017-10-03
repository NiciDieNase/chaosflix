package de.nicidienase.chaosflix.touch.adapters;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.touch.fragments.ConferencesTabBrowseFragment;

public class ConferenceRecyclerViewAdapter extends ItemRecyclerViewAdapter<Conference> {

	private final ConferencesTabBrowseFragment.OnConferenceListFragmentInteractionListener mListener;

	public ConferenceRecyclerViewAdapter(ConferencesTabBrowseFragment.OnConferenceListFragmentInteractionListener listener){
		this(new ArrayList<>(),listener);
	}

	public ConferenceRecyclerViewAdapter(List<Conference> items, ConferencesTabBrowseFragment.OnConferenceListFragmentInteractionListener listener) {
		super(items);
		mListener = listener;
	}

	@Override
	public void onBindViewHolder(ItemRecyclerViewAdapter.ViewHolder holder, int position) {
		holder.mItem = mItems.get(position);
		holder.mTitleText.setText(mItems.get(position).getTitle());
		holder.mSubtitle.setText(mItems.get(position).getAcronym());
		Picasso.with(holder.mIcon.getContext())
				.load(mItems.get(position).getLogoUrl())
				.fit()
				.centerInside()
				.into(holder.mIcon);

		holder.mView.setOnClickListener(v -> {
			if (null != mListener) {
				mListener.onConferenceSelected((Conference) holder.mItem);
			}
		});
	}

	@Override
	int getLayout() {
		return R.layout.conference_cardview_item;
	}
}
