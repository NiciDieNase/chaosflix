package de.nicidienase.chaosflix.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by felix on 17.03.17.
 */

public class Conferences{
	private List<Conference> conferences;

	private final String CONGRESS = "congress";
	private final String EVENTS = "events";
	private final String CONFERENCES = "conferences";
	private final String DEFAULT_CONFERENCE_GROUP = "other Conferences";
	private final String EVENT_GROUP = "Events";

	private Map<String,List<Conference>> conferenceMap = null;


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
						getListForTag(CONGRESS).add(conference);
						break;
					case CONFERENCES:
						switch (split.length){
							case 2:
								getListForTag(DEFAULT_CONFERENCE_GROUP).add(conference);
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
				}
			}
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
