package de.nicidienase.chaosflix.common.entities.recording;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by felix on 17.03.17.
 */

public class ConferencesWrapper implements Parcelable{
	private static final String TAG = ConferencesWrapper.class.getSimpleName();
	private static final String CONGRESS = "congress";
	private static final String EVENTS = "events";
	private static final String CONFERENCES = "conferences";
	private static final String DEFAULT_CONFERENCE_GROUP = "other conferences";
	private static final String EVENT_GROUP = "Events";
	private static final int MIN_NUM_CONS = 1;

	private List<Conference> conferences;
	private Map<String, List<Conference>> conferenceMap = null;

	protected ConferencesWrapper(Parcel in) {
		conferences = in.createTypedArrayList(Conference.CREATOR);
	}

	public static final Creator<ConferencesWrapper> CREATOR = new Creator<ConferencesWrapper>() {
		@Override
		public ConferencesWrapper createFromParcel(Parcel in) {
			return new ConferencesWrapper(in);
		}

		@Override
		public ConferencesWrapper[] newArray(int size) {
			return new ConferencesWrapper[size];
		}
	};

	public static String getStringForTag(String tag) {
		switch (tag) {
			case "congress":
				return "Congress";
			case "sendezentrum":
				return "Sendezentrum";
			case "camp":
				return "Camp";
			case "broadcast/chaosradio":
				return "Chaosradio";
			case "eh":
				return "Easterhegg";
			case "gpn":
				return "GPN";
			case "froscon":
				return "FrOSCon";
			case "mrmcd":
				return "MRMCD";
			case "sigint":
				return "SIGINT";
			case "datenspuren":
				return "Datenspuren";
			case "fiffkon":
				return "FifFKon";
			case "blinkenlights":
				return "Blinkenlights";
			case "chaoscologne":
				return "1c2 Chaos Cologne";
			case "cryptocon":
				return "CryptoCon";
			case "other conferences":
				return "Other Conferences";
			case "denog":
				return "DENOG";
			case "vcfb":
				return "Vintage Computing Festival Berlin";
			case "hackover":
				return "Hackover";
			case "netzpolitik":
				return "Das ist Netzpolitik!";
			default:
				return tag;
		}
	}

	public static List<String> getOrderedConferencesList() {
		return Arrays.asList("congress", "sendezentrum", "camp",
				"gpn", "mrmcd", "broadcast/chaosradio",
				"eh", "froscon", "sigint",
				"datenspuren", "fiffkon", "cryptocon");
	}


	public List<Conference> getConferences() {
		return conferences;
	}

	public Map<String, List<Conference>> getConferencesBySeries() {
		if (conferenceMap == null) {
			conferenceMap = new HashMap<>();
			for (Conference conference : conferences) {
				String[] split = conference.getSlug().split("/");
				List<Conference> list;
				switch (split[0]) {
					case CONGRESS:
						if (split[1].endsWith("sendezentrum")) {
							getListForTag("sendezentrum").add(conference);
						} else {
							getListForTag(CONGRESS).add(conference);
						}
						break;
					case CONFERENCES:
						switch (split.length) {
							case 2:
								if (split[1].startsWith("camp")) {
									getListForTag("camp").add(conference);
								} else if (split[1].startsWith("sigint")) {
									getListForTag("sigint").add(conference);
								} else if (split[1].startsWith("eh")) {
									getListForTag("eh").add(conference);
								} else {
									getListForTag(DEFAULT_CONFERENCE_GROUP).add(conference);
								}
								break;
							case 3:
								getListForTag(split[1]).add(conference);
								break;
							default:
								getListForTag(DEFAULT_CONFERENCE_GROUP).add(conference);
								break;
						}
						break;
					case EVENTS:
						getListForTag(EVENT_GROUP).add(conference);
						break;
					default:
						getListForTag(conference.getSlug()).add(conference);
				}
			}
		}
		List<Conference> other = conferenceMap.get(DEFAULT_CONFERENCE_GROUP);
		Set<String> keySet = conferenceMap.keySet();
		List<String> removeList = new ArrayList<>();
		for (String tag : keySet) {
			if (!tag.equals(DEFAULT_CONFERENCE_GROUP)) {
				List<Conference> list = conferenceMap.get(tag);
				Collections.sort(list);
				Collections.reverse(list);
				if (list.size() <= MIN_NUM_CONS) {
					Log.d(TAG, "To few conferences: " + tag);
					other.addAll(list);
					removeList.add(tag);
				}
			}
		}
		for (String key : removeList) {
			conferenceMap.remove(key);
		}
		return conferenceMap;
	}

	private List<Conference> getListForTag(String s) {
		if (conferenceMap != null) {
			if (conferenceMap.keySet().contains(s)) {
				return conferenceMap.get(s);
			} else {
				List<Conference> list = new ArrayList<>();
				conferenceMap.put(s, list);
				return list;
			}
		}
		return null;
	}

	public void setConferences(List<Conference> conferences) {
		this.conferences = conferences;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(conferences);
		dest.writeString(CONGRESS);
		dest.writeString(EVENTS);
		dest.writeString(CONFERENCES);
		dest.writeString(DEFAULT_CONFERENCE_GROUP);
		dest.writeString(EVENT_GROUP);
		dest.writeInt(MIN_NUM_CONS);
	}
}
