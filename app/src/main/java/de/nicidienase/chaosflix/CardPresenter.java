/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package de.nicidienase.chaosflix;

import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import de.nicidienase.chaosflix.entities.Conference;
import de.nicidienase.chaosflix.entities.Event;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
	private static final String TAG = "CardPresenter";

	private static final int CARD_WIDTH = 313;
	private static final int CARD_HEIGHT = 176;
	private static int sSelectedBackgroundColor;
	private static int sDefaultBackgroundColor;
	private Drawable mDefaultCardImage;

	private static void updateCardBackgroundColor(ImageCardView view, boolean selected) {
		int color = selected ? sSelectedBackgroundColor : sDefaultBackgroundColor;
		// Both background colors should be set because the view's background is temporarily visible
		// during animations.
		view.setBackgroundColor(color);
		view.findViewById(R.id.info_field).setBackgroundColor(color);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent) {
		sDefaultBackgroundColor = parent.getResources().getColor(R.color.default_background);
		sSelectedBackgroundColor = parent.getResources().getColor(R.color.selected_background);
		mDefaultCardImage = parent.getResources().getDrawable(R.drawable.movie);

		ImageCardView cardView = new ImageCardView(parent.getContext()) {
			@Override
			public void setSelected(boolean selected) {
				updateCardBackgroundColor(this, selected);
				super.setSelected(selected);
			}
		};

		cardView.setFocusable(true);
		cardView.setFocusableInTouchMode(true);
		updateCardBackgroundColor(cardView, false);
		return new ViewHolder(cardView);
	}

	@Override
	public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
		ImageCardView cardView = (ImageCardView) viewHolder.view;
		cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
		if(item instanceof Event){
			Event event = (Event) item;
			cardView.setTitleText(event.getTitle());
			cardView.setContentText(event.getSubtitle());
//			cardView.setContentText(android.text.TextUtils.join(", ",event.getPersons()));
			if (event.getThumbUrl() != null) {
				Glide.with(viewHolder.view.getContext())
						.load(event.getThumbUrl())
						.centerCrop()
						.error(mDefaultCardImage)
						.into(cardView.getMainImageView());
			}
		}
		if(item instanceof Conference){
			Conference conference = (Conference) item;
			cardView.setTitleText(conference.getTitle());
			cardView.setContentText(conference.getAcronym());
			if(conference.getLogoUrl() != null){

				Glide.with(viewHolder.view.getContext())
						.load(conference.getLogoUrl())
						.fitCenter()
						.error(mDefaultCardImage)
						.into(cardView.getMainImageView());
			}
		}
	}

	@Override
	public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
		ImageCardView cardView = (ImageCardView) viewHolder.view;
		// Remove references to images so that the garbage collector can free up memory
		cardView.setBadgeImage(null);
		cardView.setMainImage(null);
	}
}
