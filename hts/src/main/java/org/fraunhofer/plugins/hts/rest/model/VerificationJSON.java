package org.fraunhofer.plugins.hts.rest.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.fraunhofer.plugins.hts.model.Hazard_Causes;
import org.fraunhofer.plugins.hts.model.Hazard_Controls;
import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.model.Verifications;

import com.google.common.base.Strings;

@XmlRootElement(name = "HazardVerificationDTMinimalJson")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class VerificationJSON {
	private int verificationId;
	private int verificationNumber;
	private String text;
	private Boolean transfer;
	private Boolean active;
	private String type;
	private int hazardId;
	private String hazardNumber;
	private String hazardOwner;
	private String fullyQualifiedNumber;

	public VerificationJSON(Verifications verification, Hazards hazard) {
		this.verificationId = verification.getID();
		this.verificationNumber = verification.getVerificationNumber();
		this.text = verification.getVerificationDesc();
		this.transfer = verification.getTransfer() == 0 ? false : true;
		this.active = Strings.isNullOrEmpty(verification.getDeleteReason());
		this.hazardId = hazard.getID();
		this.hazardOwner = hazard.getPreparer() == null ? "N/A" : hazard.getPreparer();
		this.hazardNumber = hazard.getHazardNumber() == null ? "N/A" : hazard.getHazardNumber();

		Hazard_Controls[] controls = verification.getControls();
		if (controls == null || controls.length == 0)
			this.fullyQualifiedNumber = "Orph." + verificationNumber;
		else {
			Hazard_Causes[] causes = controls[0].getCauses();
			if (causes == null || causes.length == 0)
				this.fullyQualifiedNumber = "Orph." + controls[0].getControlNumber() + "." + verificationNumber;
			else
				this.fullyQualifiedNumber = causes[0].getCauseNumber() + "." + controls[0].getControlNumber() + "."
						+ verificationNumber;
		}
	}

	public int getVerificationId() {
		return verificationId;
	}

	public int getVerificationNumber() {
		return verificationNumber;
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

	public String getHazardNumber() {
		return hazardNumber;
	}

	public String getHazardOwner() {
		return hazardOwner;
	}

	public String getFullyQualifiedNumber() {
		return fullyQualifiedNumber;
	}

}
