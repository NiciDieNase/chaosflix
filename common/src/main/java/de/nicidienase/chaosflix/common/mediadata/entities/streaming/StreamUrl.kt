package de.nicidienase.chaosflix.common.mediadata.entities.streaming

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class StreamUrl(var display: String = "",
    var tech: String = "",
    var url: String = "") : Parcelable {


    protected constructor(`in`: Parcel) : this(
        display = `in`.readString() ?: "",
        tech = `in`.readString() ?: "",
        url = `in`.readString() ?: "")

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(display)
        dest.writeString(tech)
        dest.writeString(url)
    }

    companion object CREATOR : Parcelable.Creator<StreamUrl> {
        override fun createFromParcel(parcel: Parcel): StreamUrl {
            return StreamUrl(parcel)
        }

        override fun newArray(size: Int): Array<StreamUrl?> {
            return arrayOfNulls(size)
        }

        fun getDummyObject(codec: String): StreamUrl {
            val dummy = dummyObject
            when (codec) {
                "webm,vp8" -> {
                    dummy.url = "https://storage.googleapis.com/wvmedia/clear/vp9/tears/tears.mpd"
                    dummy.display = "webm,vp8"
                }
                "mp4,h265" -> {
                    dummy.url = "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears_hd.mpd"
                    dummy.display = "mp4,h265"
                }
                "hls" -> {
                    dummy.url = "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8"
                    dummy.display = "hls"
                }
                "4x3" -> {
                    dummy.url = "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8"
                    dummy.display = "4x3"
                }
                "dizzy" -> {
                    dummy.url = "http://html5demos.com/assets/dizzy.mp4"
                    dummy.display = "dizzy"
                }
                "winkekatze" -> {
                    dummy.url = "https://r5---sn-4g57knlz.googlevideo.com/videoplayback?id=acfd4764b9107d34&itag=299&source=youtube&requiressl=yes&initcwndbps=1472500&pl=20&mv=m&ei=RVj6WJbVMZO2cMvgqIAF&ms=au&mm=31&mn=sn-4g57knlz&ratebypass=yes&mime=video/mp4&gir=yes&clen=17985037641&lmt=1418590845082732&dur=36011.200&key=dg_yt0&mt=1492801526&upn=cA3U7DFBWOw&signature=537B4E86C9CD83ED2EACFAFCFAEE840E259C2DD1.12BF28ED6A0BA6AFDD4FD1631786AA46DC798D12&ip=109.192.170.65&ipbits=0&expire=1492823205&sparams=ip,ipbits,expire,id,itag,source,requiressl,initcwndbps,pl,mv,ei,ms,mm,mn,ratebypass,mime,gir,clen,lmt,dur"
                    dummy.display = "winkekatze"
                }
            }
            return dummy
        }

        val dummyObject: StreamUrl
            get() {
                return StreamUrl("https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8", "","HLS")
            }
    }
}
