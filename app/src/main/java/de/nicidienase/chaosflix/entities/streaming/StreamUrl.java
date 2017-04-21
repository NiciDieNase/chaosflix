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

	public StreamUrl() {
	}

	public StreamUrl(String display, String tech, String url) {
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

	public static StreamUrl getDummyObject(String codec){
		StreamUrl dummy = getDummyObject();
		switch (codec){
			case "webm,vp8":
				dummy.setUrl("https://storage.googleapis.com/wvmedia/clear/vp9/tears/tears.mpd");
				dummy.setDisplay("webm,vp8");
				break;
			case "mp4,h265":
				dummy.setUrl("https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears_hd.mpd");
				dummy.setDisplay("mp4,h265");
				break;
			case "hls":
				dummy.setUrl("https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8");
				dummy.setDisplay("hls");
				break;
			case "4x3":
				dummy.setUrl("https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8");
				dummy.setDisplay("4x3");
				break;
			case "dizzy":
				dummy.setUrl("http://html5demos.com/assets/dizzy.mp4");
				dummy.setDisplay("dizzy");
				break;
		}
		return dummy;
	}

	public static StreamUrl getDummyObject(){
		StreamUrl dummy = new StreamUrl();
		dummy.setUrl("https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8");
		dummy.setTech("");
		dummy.setDisplay("HLS");
		return dummy;
	}
}
