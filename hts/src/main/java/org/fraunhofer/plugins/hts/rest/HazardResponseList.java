package org.fraunhofer.plugins.hts.rest;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazards;

@XmlRootElement(name = "responseList")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class HazardResponseList {
	private int hazardID;
	private String title;
	private String hazardNumber;
	
	public int getHazardID() {
		return hazardID;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getHazardNumber() {
		return hazardNumber;
	}
	
	public static HazardResponseList hazards(Hazards hazard) {
		HazardResponseList list = new HazardResponseList();
		list.hazardID = hazard.getID();
		list.title = hazard.getTitle();
		list.hazardNumber = hazard.getHazardNum();
		
		return list;
	}
}
