package de.nicidienase.chaosflix.shared.entities.streaming;

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
			case "winkekatze":
				dummy.setUrl("https://r5---sn-4g57knlz.googlevideo.com/videoplayback?id=acfd4764b9107d34&itag=299&source=youtube&requiressl=yes&initcwndbps=1472500&pl=20&mv=m&ei=RVj6WJbVMZO2cMvgqIAF&ms=au&mm=31&mn=sn-4g57knlz&ratebypass=yes&mime=video/mp4&gir=yes&clen=17985037641&lmt=1418590845082732&dur=36011.200&key=dg_yt0&mt=1492801526&upn=cA3U7DFBWOw&signature=537B4E86C9CD83ED2EACFAFCFAEE840E259C2DD1.12BF28ED6A0BA6AFDD4FD1631786AA46DC798D12&ip=109.192.170.65&ipbits=0&expire=1492823205&sparams=ip,ipbits,expire,id,itag,source,requiressl,initcwndbps,pl,mv,ei,ms,mm,mn,ratebypass,mime,gir,clen,lmt,dur");
				dummy.setDisplay("winkekatze");
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
