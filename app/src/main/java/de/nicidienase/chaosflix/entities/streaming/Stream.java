package de.nicidienase.chaosflix.entities.streaming;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

/**
 * Created by felix on 23.03.17.
 */

public class Stream implements Parcelable{
	String slug;
	String display;
	String type;
	boolean isTranslated;
	int[] videoSize;
	Map<String,StreamUrl> urls;

	protected Stream(Parcel in) {
		slug = in.readString();
		display = in.readString();
		type = in.readString();
		isTranslated = in.readByte() != 0;
		videoSize = in.createIntArray();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(slug);
		dest.writeString(display);
		dest.writeString(type);
		dest.writeByte((byte) (isTranslated ? 1 : 0));
		dest.writeIntArray(videoSize);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<Stream> CREATOR = new Creator<Stream>() {
		@Override
		public Stream createFromParcel(Parcel in) {
			return new Stream(in);
		}

		@Override
		public Stream[] newArray(int size) {
			return new Stream[size];
		}
	};

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isTranslated() {
		return isTranslated;
	}

	public void setTranslated(boolean translated) {
		isTranslated = translated;
	}

	public int[] getVideoSize() {
		return videoSize;
	}

	public void setVideoSize(int[] videoSize) {
		this.videoSize = videoSize;
	}

	public Map<String, StreamUrl> getUrls() {
		return urls;
	}

	public void setUrls(Map<String, StreamUrl> urls) {
		this.urls = urls;
	}
}
