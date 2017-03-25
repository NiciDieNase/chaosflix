package de.nicidienase.chaosflix.entities.recording;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by felix on 17.03.17.
 */

public class ConferencesWrapper {
	private static final String TAG = ConferencesWrapper.class.getSimpleName();
	private List<Conference> conferences;

	private final String CONGRESS = "congress";
	private final String EVENTS = "events";
	private final String CONFERENCES = "conferences";
	private final String DEFAULT_CONFERENCE_GROUP = "other conferences";
	private final String EVENT_GROUP = "Events";

	private Map<String,List<Conference>> conferenceMap = null;
	private int MIN_NUM_CONS = 1;


	public List<Conference> getConferences() {
		return conferences;
	}

	public Map<String,List<Conference>> getConferencesBySeries(){
		if(conferenceMap == null){
			conferenceMap = new HashMap<>();
			for(Conference conference: conferences){
				String[] split = conference.getSlug().split("/");
				List<Conference> list;
				switch (split[0]){
					case CONGRESS:
						if(split[1].endsWith("sendezentrum")){
							getListForTag("sendezentrum").add(conference);
						} else {
							getListForTag(CONGRESS).add(conference);
						}
						break;
					case CONFERENCES:
						switch (split.length){
							case 2:
								if(split[1].startsWith("camp")){
									getListForTag("camp").add(conference);
								} else if(split[1].startsWith("sigint")){
									getListForTag("sigint").add(conference);
								} else if(split[1].startsWith("eh")){
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
		for(String tag: keySet){
			if(!tag.equals(DEFAULT_CONFERENCE_GROUP)){
				List<Conference> list = conferenceMap.get(tag);
				Collections.sort(list);
				Collections.reverse(list);
				if(list.size() <= MIN_NUM_CONS){
					Log.d(TAG,"To few conferences: " + tag);
					other.addAll(list);
					removeList.add(tag);
				}
			}
		}
		for(String key:removeList){
			conferenceMap.remove(key);
		}
		return conferenceMap;
	}

	private List<Conference> getListForTag(String s){
		if(conferenceMap != null){
			if(conferenceMap.keySet().contains(s)){
				return conferenceMap.get(s);
			} else {
				List<Conference> list = new ArrayList<>();
				conferenceMap.put(s,list);
				return list;
			}
		}
		return null;
	}

	public void setConferences(List<Conference> conferences) {
		this.conferences = conferences;
	}
}
