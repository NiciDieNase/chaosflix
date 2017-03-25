package de.nicidienase.chaosflix;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.text.TextUtils;

import de.nicidienase.chaosflix.entities.recording.Event;
import de.nicidienase.chaosflix.entities.streaming.Room;

/**
 * Created by felix on 18.03.17.
 */

public class EventDetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {
	@Override
	protected void onBindDescription(ViewHolder vh, Object item) {
		if(item instanceof Event){
			Event event = (Event) item;

			vh.getTitle().setText(event.getTitle());
			vh.getSubtitle().setText(event.getSubtitle());
			StringBuilder sb = new StringBuilder();
			String speaker = TextUtils.join(", ", event.getPersons());
			sb.append(event.getDescription())
					.append("\n")
					.append("by: ").append(speaker)
					.append("\nreleased at: ").append(event.getReleaseDate())
					.append("\nlast updated at: ").append(event.getUpdatedAt())
					.append("\nTags: ").append(android.text.TextUtils.join(", ",event.getTags()));
			vh.getBody().setText(sb.toString());
		} else if(item instanceof Room){
			Room room = (Room) item;
			vh.getTitle().setText(room.getDisplay());
			vh.getSubtitle().setText(room.getShedulename());
		}
	}
}
