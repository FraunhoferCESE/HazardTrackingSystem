package org.fraunhofer.plugins.hts.rest.datatype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazards;

import com.google.common.base.Strings;

@XmlRootElement(name = "HazardCauseDTMinimalJson")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class CauseJSON {
	private int causeID;
	private int causeNumber;
	private String text;
	private boolean transfer;
	private boolean active;
	private String type;
	private int hazardId;
	private String hazardOwner;
	private String hazardNumber;

	public CauseJSON(Hazard_Causes cause, Hazards hazard) {
		this.causeID = cause.getID();
		this.causeNumber = cause.getCauseNumber();
		this.text = cause.getDescription();
		this.transfer = cause.getTransfer() == 0 ? false : true;
		this.active = Strings.isNullOrEmpty(cause.getDeleteReason());
		this.hazardId = hazard.getID();
		this.hazardOwner = hazard.getPreparer() == null ? "N/A" : hazard.getPreparer();
		this.hazardNumber = hazard.getHazardNumber() == null ? "N/A" : hazard.getHazardNumber();
	}

	public CauseJSON(int causeID, int causeNumber, String text, boolean transfer, boolean active, String type) {
		this.causeID = causeID;
		this.causeNumber = causeNumber;
		this.text = text;
		this.transfer = transfer;
		this.active = active;
		this.type = type;
	}

	public int getCauseID() {
		return causeID;
	}

	public int getCauseNumber() {
		return causeNumber;
	}

	public String getText() {
		return text;
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

	public int getHazardId() {
		return hazardId;
	}

	public String getHazardOwner() {
		return hazardOwner;
	}

	public String getHazardNumber() {
		return hazardNumber;
	}

}