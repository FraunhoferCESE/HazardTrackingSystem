package org.fraunhofer.plugins.hts.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.fraunhofer.plugins.hts.db.Hazard_Causes;

@XmlRootElement(name = "causeResponseList")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class HazardCauseResponseList {
	private int causeID;
	private String title;
	private String causeNumber;
	
	public int getCauseID() {
		return causeID;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getCauseNumber() {
		return causeNumber;
	}
	
	public static HazardCauseResponseList causes(Hazard_Causes cause) {
		HazardCauseResponseList list = new HazardCauseResponseList();
		list.causeID = cause.getID();
		list.title = cause.getTitle();
		list.causeNumber = cause.getCauseNumber();
		return list;
	}
}
