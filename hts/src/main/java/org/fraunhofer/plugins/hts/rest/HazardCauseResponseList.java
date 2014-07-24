package org.fraunhofer.plugins.hts.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

import org.fraunhofer.plugins.hts.db.Hazard_Causes;

@XmlRootElement(name = "causeResponseList")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class HazardCauseResponseList {
	private int causeID;
	private String title;
	private int causeNumber;
	private String description;
	private String owner;
	private String effects;
	private Date lastUpdated;

	public HazardCauseResponseList() {

	}

	public int getCauseID() {
		return causeID;
	}

	public String getTitle() {
		return title;
	}

	public int getCauseNumber() {
		return causeNumber;
	}

	public String getDescription() {
		return description;
	}

	public String getOwner() {
		return owner;
	}

	public String getEffects() {
		return effects;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public static HazardCauseResponseList causes(Hazard_Causes cause) {
		HazardCauseResponseList list = new HazardCauseResponseList();
		list.causeID = cause.getID();
		list.title = cause.getTitle();
		list.causeNumber = cause.getCauseNumber();
		list.description = cause.getDescription();
		list.owner = cause.getOwner();
		list.effects = cause.getEffects();
		list.lastUpdated = cause.getLastUpdated();
		return list;
	}
}
