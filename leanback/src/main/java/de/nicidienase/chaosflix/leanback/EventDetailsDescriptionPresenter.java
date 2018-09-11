package de.nicidienase.chaosflix.leanback;

import android.content.Context;
import android.support.v17.leanback.widget.Presenter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room;
import de.nicidienase.chaosflix.databinding.DetailViewBinding;

/**
 * Created by felix on 18.03.17.
 */

public class EventDetailsDescriptionPresenter extends Presenter {

	private static final String TAG = EventDetailsDescriptionPresenter.class.getSimpleName();
	private final Context context;

	public EventDetailsDescriptionPresenter(Context context) {
		this.context = context;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent) {
		DetailViewBinding binding =
				DetailViewBinding.inflate(LayoutInflater.from(context));
		return new DescriptionViewHolder(binding.getRoot(),binding);
	}

	@Override
	public void onBindViewHolder(ViewHolder vh, Object item) {
		if(! (vh instanceof DescriptionViewHolder)){
			throw new IllegalStateException("Wrong ViewHolder");
		}
		DescriptionViewHolder viewHolder = (DescriptionViewHolder) vh;
		DetailDataHolder dataHolder;
		if (item instanceof PersistentEvent) {
			PersistentEvent event = (PersistentEvent) item;
			StringBuilder sb = new StringBuilder();
			String speaker = TextUtils.join(", ", event.getPersons());
			sb.append(event.getDescription())
					.append("\n")
					.append("\nreleased at: ").append(event.getReleaseDate())
					.append("\nTags: ").append(android.text.TextUtils.join(", ", event.getTags()));
			dataHolder = new DetailDataHolder(event.getTitle(),
					event.getSubtitle(),
					speaker,
					sb.toString());
		} else if (item instanceof Room) {
			Room room = (Room) item;
			dataHolder = new DetailDataHolder(room.getDisplay(),room.getSchedulename(),"","");
		} else {
			Log.e(TAG,"Item is neither PersistentEvent nor Room, this should not be happening");
			dataHolder = new DetailDataHolder("","","","");
		}
		viewHolder.binding.setItem(dataHolder);
	}

	public class DetailDataHolder {
		private String title;
		private String subtitle;
		private String speakers;
		private String description;

		DetailDataHolder(String title, String subtitle, String speakers, String description){

			this.title = title;
			this.subtitle = subtitle;
			this.speakers = speakers;
			this.description = description;
		}

		DetailDataHolder(PersistentEvent event){
			this(event.getTitle(),event.getSubtitle(), TextUtils.join(", ", event.getPersons()), "");
			StringBuilder sb = new StringBuilder();
			sb.append(event.getDescription())
					.append("\n")
					.append("\nreleased at: ").append(event.getReleaseDate())
					.append("\nTags: ").append(android.text.TextUtils.join(", ", event.getTags()));
			description = sb.toString();
		}

		DetailDataHolder(Room room){
			this(room.getDisplay(),room.getSchedulename(),"","");
		}

		public String getTitle() {
			return title;
		}

		public String getSubtitle() {
			return subtitle;
		}

		public String getSpeakers() {
			return speakers;
		}

		public String getDescription() {
			return description;
		}
	}

	public class DescriptionViewHolder extends ViewHolder{

		private final DetailViewBinding binding;

		public DescriptionViewHolder(View view, DetailViewBinding binding) {
			super(view);
			this.binding = binding;
		}

		public DetailViewBinding getBinding() {
			return binding;
		}
	}


	@Override
	public void onUnbindViewHolder(ViewHolder vh) {}
}
