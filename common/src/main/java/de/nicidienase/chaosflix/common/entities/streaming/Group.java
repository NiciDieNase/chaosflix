package de.nicidienase.chaosflix.common.entities.streaming;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by felix on 23.03.17.
 */

public class Group {
	String group;
	List<Room> rooms;

	public Group() {}

	public Group(String group) {
		this.group = group;
	}

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

	public static Group getDummyObject(){
		Group dummy = new Group();
		dummy.setGroup("Dummy Group");
		dummy.setRooms(new ArrayList<>());
		dummy.getRooms().add(Room.getDummyObject());
		return dummy;
	}
}
