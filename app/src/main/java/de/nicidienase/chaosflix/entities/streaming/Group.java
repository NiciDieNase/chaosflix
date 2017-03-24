package de.nicidienase.chaosflix.entities.streaming;

import java.util.List;

/**
 * Created by felix on 23.03.17.
 */

public class Group {
	String group;
	List<Room> rooms;

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public List<Room> getRooms() {
		return rooms;
	}

	public void setRooms(List<Room> rooms) {
		this.rooms = rooms;
	}
}
