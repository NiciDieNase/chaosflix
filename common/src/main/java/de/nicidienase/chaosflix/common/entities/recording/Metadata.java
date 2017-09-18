package de.nicidienase.chaosflix.common.entities.recording;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

/**
 * Created by felix on 06.04.17.
 */

public class Metadata extends SugarRecord implements Parcelable {
	long[] related;
	@SerializedName("remote_id")
	String remoteId;

	protected Metadata(Parcel in) {
		related = in.createLongArray();
		remoteId = in.readString();
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

	public long[] getRelated() {
		return related;
	}

	public void setRelated(long[] related) {
		this.related = related;
	}

	public String getRemoteId() {
		return remoteId;
	}

	public void setRemoteId(String remoteId) {
		this.remoteId = remoteId;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLongArray(related);
		dest.writeString(remoteId);
	}
}
