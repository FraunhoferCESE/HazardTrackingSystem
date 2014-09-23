package org.fraunhofer.plugins.hts.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.Date;

import org.fraunhofer.plugins.hts.db.Hazard_Causes;

import com.google.common.base.Strings;

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
	private String riskCategory;
	private String riskLikelihood;
	private boolean transfer;
	private Boolean active;
	private String type;

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
	
	public String getRiskCategory() {
		return riskCategory;
	}
	
	public String getRiskLikelihood() {
		return riskLikelihood;
	}
	
	public boolean getTransfer() {
		return transfer;
	}
	
	public Boolean getActive() {
		return active;
	}
	
	public String getType() {
		return type;
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
		list.type = "CAUSE";
		
		if (Strings.isNullOrEmpty(cause.getDeleteReason())) {
			list.active = true;
		}
		else {
			list.active = false;
		}
		
		list.transfer = true;
		if (cause.getTransfer() == 0) {
			list.riskCategory = cause.getRiskCategory().getValue();
			list.riskLikelihood = cause.getRiskLikelihood().getValue();
			list.transfer = false;
		}	
		
		return list;
	}
}
