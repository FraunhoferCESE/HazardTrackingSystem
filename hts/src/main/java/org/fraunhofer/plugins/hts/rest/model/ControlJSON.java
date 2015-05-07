package org.fraunhofer.plugins.hts.rest.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.fraunhofer.plugins.hts.model.Hazard_Controls;
import org.fraunhofer.plugins.hts.model.Hazards;

import com.google.common.base.Strings;

@XmlRootElement(name = "HazardControlDTMinimalJson")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class ControlJSON {
	private int controlID;
	private int controlNumber;
	private String text;
	private Boolean transfer;
	private Boolean active;
	private String type;
	private int hazardId;
	private String hazardNumber;
	private String hazardOwner;

	public ControlJSON(Hazard_Controls control, Hazards hazard) {
		this.controlID = control.getID();
		this.controlNumber = control.getControlNumber();
		this.text = control.getDescription();
		this.transfer = control.getTransfer() == 0 ? false : true;
		this.active = Strings.isNullOrEmpty(control.getDeleteReason());
		this.hazardId = hazard.getID();
		this.hazardOwner = hazard.getPreparer() == null ? "N/A" : hazard.getPreparer();
		this.hazardNumber = hazard.getHazardNumber() == null ? "N/A" : hazard.getHazardNumber();
	}

	public ControlJSON(int controlID, int controlNumber, String text, boolean transfer, boolean active, String type) {
		this.controlID = controlID;
		this.controlNumber = controlNumber;
		this.text = text;
		this.transfer = transfer;
		this.active = active;
		this.type = type;
	}

	public int getControlID() {
		return controlID;
	}

	public int getControlNumber() {
		return controlNumber;
	}

	public String getText() {
		return text;
	}

	public Boolean getTransfer() {
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
