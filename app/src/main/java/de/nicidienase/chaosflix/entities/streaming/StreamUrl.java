package de.nicidienase.chaosflix.entities.streaming;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by felix on 23.03.17.
 */

public class StreamUrl implements Parcelable {

	String display;
	String tech;

	String url;

	protected StreamUrl(Parcel in) {
		display = in.readString();
		tech = in.readString();
		url = in.readString();
	}

	public static final Creator<StreamUrl> CREATOR = new Creator<StreamUrl>() {
		@Override
		public StreamUrl createFromParcel(Parcel in) {
			return new StreamUrl(in);
		}

		@Override
		public StreamUrl[] newArray(int size) {
			return new StreamUrl[size];
		}
	};

	public StreamUrl(){}

	public StreamUrl(String display, String tech, String url){
		this.display = display;
		this.tech = tech;
		this.url = url;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public String getTech() {
		return tech;
	}

	public void setTech(String tech) {
		this.tech = tech;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(display);
		dest.writeString(tech);
		dest.writeString(url);
	}
}
