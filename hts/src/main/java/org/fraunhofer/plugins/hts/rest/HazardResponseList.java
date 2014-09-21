package org.fraunhofer.plugins.hts.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.fraunhofer.plugins.hts.db.Hazards;

@XmlRootElement(name = "hazardResponseList")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class HazardResponseList {
	private int hazardID;
	private String title;
	private String hazardNumber;
	private String type;
	private Boolean active;

	public int getHazardID() {
		return hazardID;
	}

	public String getTitle() {
		return title;
	}

	public String getHazardNumber() {
		return hazardNumber;
	}
	
	public String getType() {
		return type;
	}
	
	public Boolean getActive() {
		return active;
	}

	public static HazardResponseList hazards(Hazards hazard) {
		HazardResponseList list = new HazardResponseList();
		list.hazardID = hazard.getID();
		list.title = hazard.getTitle();
		list.hazardNumber = hazard.getHazardNum();
		list.active = hazard.getActive();
		list.type = "HAZARD";
		return list;
	}
}
