package de.nicidienase.chaosflix.common.entities.streaming;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by felix on 23.03.17.
 */

public class Stream implements Parcelable {
	private static final String MAP_KEY = "map-key";
	String slug;
	String display;
	String type;
	boolean isTranslated;
	int[] videoSize;
	HashMap<String, StreamUrl> urls;

	public Stream() {}

	protected Stream(Parcel in) {
		slug = in.readString();
		display = in.readString();
		type = in.readString();
		isTranslated = in.readByte() != 0;
		videoSize = in.createIntArray();
		int mapSize = in.readInt();
		urls = new HashMap<>();
		for(int i = 0; i< mapSize; i++){
			urls.put(in.readString(),in.readParcelable(StreamUrl.class.getClassLoader()));
		}
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(slug);
		dest.writeString(display);
		dest.writeString(type);
		dest.writeByte((byte) (isTranslated ? 1 : 0));
		dest.writeIntArray(videoSize);
		dest.writeInt(urls.size());
		Set<String> keys = urls.keySet();
		for(String s: keys){
			dest.writeString(s);
			dest.writeParcelable(urls.get(s),0);
		}
	}

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

	public void setUrls(HashMap<String, StreamUrl> urls) {
		this.urls = urls;
	}

	public static Stream getDummyObject(){
		Stream dummy = new Stream();
		dummy.setSlug("dummy");
		dummy.setDisplay("Dummy");
		dummy.setType("video");
		dummy.setTranslated(false);
		dummy.setVideoSize(new int[]{1, 1});
		dummy.setUrls(new HashMap<>());
		dummy.getUrls().put("hls",StreamUrl.getDummyObject("hls"));
		dummy.getUrls().put("dizzy",StreamUrl.getDummyObject("dizzy"));
		dummy.getUrls().put("webm,vp8",StreamUrl.getDummyObject("webm,vp8"));
		dummy.getUrls().put("mp4,h265",StreamUrl.getDummyObject("mp4,h265"));
		dummy.getUrls().put("4x3",StreamUrl.getDummyObject("4x3"));
		dummy.getUrls().put("winkekatze",StreamUrl.getDummyObject("winkekatze"));
		dummy.setType("dummy");
		return dummy;
	}
}
