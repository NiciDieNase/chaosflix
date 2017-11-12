package de.nicidienase.chaosflix.common.entities.recording;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by felix on 06.04.17.
 */

public class Metadata extends SugarRecord implements Parcelable {
	Map<Long,Long> related;
	@SerializedName("remote_id")
	String remoteId;

	protected Metadata(Parcel in) {
		remoteId = in.readString();
		long mapSize = in.readLong();
		related = new HashMap<>();
		for(int i= 0; i< mapSize; i++){
			related.put(in.readLong(),in.readLong());
		}
	}

	public static final Creator<Metadata> CREATOR = new Creator<Metadata>() {
		@Override
		public Metadata createFromParcel(Parcel in) {
			return new Metadata(in);
		}

		@Override
		public Metadata[] newArray(int size) {
			return new Metadata[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(remoteId);
		dest.writeLong(related.size());
		for(Map.Entry<Long,Long> entry: related.entrySet()){
			dest.writeLong(entry.getKey());
			dest.writeLong(entry.getValue());
		}
	}

	public Map<Long, Long> getRelated() {
		return related;
	}

	public void setRelated(Map<Long, Long> related) {
		this.related = related;
	}

	public String getRemoteId() {
		return remoteId;
	}

	public void setRemoteId(String remoteId) {
		this.remoteId = remoteId;
	}
}
