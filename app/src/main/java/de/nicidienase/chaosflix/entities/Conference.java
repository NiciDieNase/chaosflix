package de.nicidienase.chaosflix.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by felix on 11.03.17.
 */

public class Conference extends SugarRecord implements Parcelable,Comparable<Conference> {
	String acronym;
	@SerializedName("aspect_ratio")
	String aspectRation;
	String title;
	String slug;
	@SerializedName("webgen_location")
	String webgenLocation;
	@SerializedName("schedule_url")
	String scheduleUrl;
	@SerializedName("logo_url")
	String logoUrl;
	@SerializedName("images_url")
	String imagesUrl;
	@SerializedName("recordings_url")
	String recordingsUrl;
	String url;
	@SerializedName("updated_at")
	String updatedAt;

	List<Event> events;

	public Conference() {
	}

	public Conference(String acronym, String aspectRation, String title, String slug,
					  String webgenLocation, String scheduleUrl, String logoUrl,
					  String imagesUrl, String recordingsUrl, String url,
					  String updatedAt, List<Event> events) {
		this.acronym = acronym;
		this.aspectRation = aspectRation;
		this.title = title;
		this.slug = slug;
		this.webgenLocation = webgenLocation;
		this.scheduleUrl = scheduleUrl;
		this.logoUrl = logoUrl;
		this.imagesUrl = imagesUrl;
		this.recordingsUrl = recordingsUrl;
		this.url = url;
		this.updatedAt = updatedAt;
		this.events = events;
	}

	protected Conference(Parcel in) {
		acronym = in.readString();
		aspectRation = in.readString();
		title = in.readString();
		slug = in.readString();
		webgenLocation = in.readString();
		scheduleUrl = in.readString();
		logoUrl = in.readString();
		imagesUrl = in.readString();
		recordingsUrl = in.readString();
		url = in.readString();
		updatedAt = in.readString();
		events = in.createTypedArrayList(Event.CREATOR);
	}

	public static final Creator<Conference> CREATOR = new Creator<Conference>() {
		@Override
		public Conference createFromParcel(Parcel in) {
			return new Conference(in);
		}

		@Override
		public Conference[] newArray(int size) {
			return new Conference[size];
		}
	};

	public HashMap<String, List<Event>> getEventsByTags(){
		HashMap<String, List<Event>> result = new HashMap<>();
		List<Event> untagged = new ArrayList<>();
		for(Event event: this.getEvents()){
			if(event.getTags().size()>0){
				for(String tag: event.getTags()){
					List<Event> list;
					if(result.keySet().contains(tag)){
						list = result.get(tag);
					} else {
						list = new LinkedList<>();
						result.put(tag,list);
					}
					list.add(event);
				}
			} else {
				untagged.add(event);
			}
		}
		if(untagged.size() > 0){
			result.put("untagged",untagged);
		}
		return result;
	}
	public int getApiID(){
		String[] strings = getUrl().split("/");
		return Integer.parseInt(strings[strings.length-1]);
	}

	public String getAcronym() {
		return acronym;
	}

	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}

	public String getAspectRation() {
		return aspectRation;
	}

	public void setAspectRation(String aspectRation) {
		this.aspectRation = aspectRation;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getWebgenLocation() {
		return webgenLocation;
	}

	public void setWebgenLocation(String webgenLocation) {
		this.webgenLocation = webgenLocation;
	}

	public String getScheduleUrl() {
		return scheduleUrl;
	}

	public void setScheduleUrl(String scheduleUrl) {
		this.scheduleUrl = scheduleUrl;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public String getImagesUrl() {
		return imagesUrl;
	}

	public void setImagesUrl(String imagesUrl) {
		this.imagesUrl = imagesUrl;
	}

	public String getRecordingsUrl() {
		return recordingsUrl;
	}

	public void setRecordingsUrl(String recordingsUrl) {
		this.recordingsUrl = recordingsUrl;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<Event> getEvents() {
//		return Event.find(Event.class,"parent_conference_id = ? ", String.valueOf(this.getId()));
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

	public void update(Conference conf) {
		if(!this.updatedAt.equals(conf.updatedAt)){
			// TODO actually update
			this.save();
		}
	}

	@Override
	public int compareTo(Conference conference) {
		return updatedAt.compareTo(conference.getUpdatedAt());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(acronym);
		parcel.writeString(aspectRation);
		parcel.writeString(title);
		parcel.writeString(slug);
		parcel.writeString(webgenLocation);
		parcel.writeString(scheduleUrl);
		parcel.writeString(logoUrl);
		parcel.writeString(imagesUrl);
		parcel.writeString(recordingsUrl);
		parcel.writeString(url);
		parcel.writeString(updatedAt);
		parcel.writeTypedList(events);
	}
}
