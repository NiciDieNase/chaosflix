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

package de.nicidienase.chaosflix.leanback;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;
import android.view.ContextThemeWrapper;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
	private static final String TAG = "CardPresenter";

//	private static int sSelectedBackgroundColor;
//	private static int sDefaultBackgroundColor;
	private Drawable defaultCardImage;
	private int style;

	public CardPresenter(){
		this(R.style.Theme_Leanback);
	}

	public CardPresenter(int style){
		this.style = style;
	}

//	private static void updateCardBackgroundColor(ImageCardView view, boolean selected) {
//		int color = selected ? sSelectedBackgroundColor : sDefaultBackgroundColor;
		// Both background colors should be set because the view's background is temporarily visible
		// during animations.
//		view.setBackgroundColor(color);
//		view.findViewById(R.id.info_field).setBackgroundColor(color);
//	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent) {
//		sDefaultBackgroundColor = parent.getResources().getColor(R.color.default_background);
//		sSelectedBackgroundColor = parent.getResources().getColor(R.color.selected_background);
		defaultCardImage = parent.getResources().getDrawable(R.drawable.default_background);

		ImageCardView cardView = new ImageCardView(new ContextThemeWrapper(parent.getContext(), style)) {
			@Override
			public void setSelected(boolean selected) {
//				updateCardBackgroundColor(this, selected);
				super.setSelected(selected);
			}
		};

		cardView.setFocusable(true);
		cardView.setFocusableInTouchMode(true);
//		updateCardBackgroundColor(cardView, false);
		return new ViewHolder(cardView);
	}

	@Override
	public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
		ImageCardView cardView = (ImageCardView) viewHolder.view;
		cardView.setMainImageScaleType(ImageView.ScaleType.FIT_CENTER);
		if (item instanceof Conference) {
			Conference conference = (Conference) item;
			cardView.setTitleText(conference.getTitle());
			cardView.setContentText(conference.getAcronym());
			if (conference.getLogoUrl() != null) {
				loadImage(viewHolder.view.getContext(),conference.getLogoUrl(), cardView.getMainImageView());
			}
		} else if (item instanceof Event) {
			Event event = (Event) item;
			cardView.setTitleText(event.getTitle());
			cardView.setContentText(event.getSubtitle());
//			cardView.setContentText(android.text.TextUtils.join(", ",event.getPersons()));
			if (event.getThumbUrl() != null) {
				loadImage(viewHolder.view.getContext(),event.getThumbUrl(), cardView.getMainImageView());
			}
		} else if (item instanceof LiveConference) {
			LiveConference con = (LiveConference) item;
			cardView.setTitleText(con.getConference());
			cardView.setMainImage(defaultCardImage);
			cardView.setContentText(con.getDescription());
		} else if (item instanceof Room) {
			Room room = (Room) item;
			cardView.setTitleText(room.getDisplay());
			cardView.setContentText(room.getSchedulename());
			if (room.getThumb() != null) {
				loadImage(viewHolder.view.getContext(),room.getThumb(), cardView.getMainImageView());
			}
		} else if(item instanceof String) {
			cardView.setTitleText((String) item);
			Glide.with(viewHolder.view.getContext())
					.load(R.drawable.icon_primary_background)
					.into(cardView.getMainImageView());
		}

	}

	public void loadImage(Context context, String source, ImageView destination){
		RequestOptions options = new RequestOptions();
		options.fitCenter();
		options.fallback(R.drawable.default_background);
		Glide.with(context)
				.load(source)
				.apply(options)
				.into(destination);
	}

	@Override
	public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
		ImageCardView cardView = (ImageCardView) viewHolder.view;
		// Remove references to images so that the garbage collector can free up memory
		cardView.setBadgeImage(null);
		cardView.setMainImage(null);
	}
}
