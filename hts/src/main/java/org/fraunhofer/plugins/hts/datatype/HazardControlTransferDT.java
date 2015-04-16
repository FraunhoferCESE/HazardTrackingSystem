package org.fraunhofer.plugins.hts.datatype;

import org.fraunhofer.plugins.hts.db.ControlGroups;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Transfers;

import com.google.common.base.Strings;

public class HazardControlTransferDT {
	// Joint
	private int transferID;
	private String transferTargetType;
	private int targetHazardID;
	private String targetHazardTitle;
	private String targetHazardNumber;
	private String transferReason;
	private int targetID;
	private int originID;
	private boolean deleted; 
	// For ControlToControl transfers
	private int targetControlNumber;
	private String targetControlDescription;
	private String targetControlControlGroup;
	// For ControlToCause transfers
	private int targetCauseNumber;
	private String targetCauseTitle;
	
	public static HazardControlTransferDT createControlToControl(Transfers transfer,
			Hazard_Controls originControl, Hazard_Controls targetControl) {
		HazardControlTransferDT instance = new HazardControlTransferDT();
		instance.setTransferID(transfer.getID());
		instance.setTransferTargetType(transfer.getTargetType());
		instance.setTargetHazardID(targetControl.getHazard()[0].getID());
		instance.setTargetHazardTitle(targetControl.getHazard()[0].getHazardTitle());
		instance.setTargetHazardNumber(targetControl.getHazard()[0].getHazardNumber());
		instance.setTransferReason(originControl.getDescription());
		instance.setTargetID(transfer.getTargetID());
		instance.setOriginID(transfer.getOriginID());
		instance.setTargetControlDescription(targetControl.getDescription());
		instance.setTargetControlNumber(targetControl.getControlNumber());
		
		ControlGroups controlGroup = targetControl.getControlGroup();
		if (controlGroup == null) {
			instance.setTargetControlControlGroup("N/A");
		} else {
			instance.setTargetControlControlGroup(targetControl.getControlGroup().getLabel());
		}
		
		if (Strings.isNullOrEmpty(targetControl.getDeleteReason())) {
			System.out.println("HCTDT ");
			instance.setDeleted(false);
		} else {
			instance.setDeleted(true);
		}
		
		return instance;
	}
	
	public static HazardControlTransferDT createControlToCause(Transfers transfer,
			Hazard_Controls originControl, Hazard_Causes targetCause) {
		HazardControlTransferDT instance = new HazardControlTransferDT();
		instance.setTransferID(transfer.getID());
		instance.setTransferTargetType(transfer.getTargetType());
		instance.setTargetHazardID(targetCause.getHazards()[0].getID());
		instance.setTargetHazardTitle(targetCause.getHazards()[0].getHazardTitle());
		instance.setTargetHazardNumber(targetCause.getHazards()[0].getHazardNumber());
		instance.setTransferReason(originControl.getDescription());
		instance.setTargetID(transfer.getTargetID());
		instance.setOriginID(transfer.getOriginID());
		instance.setTargetCauseTitle(targetCause.getTitle());
		instance.setTargetCauseNumber(targetCause.getCauseNumber());	
		
		if (Strings.isNullOrEmpty(targetCause.getDeleteReason())) {
			instance.setDeleted(false);
		} else {
			instance.setDeleted(true);
		}
		
		return instance;
	}
	
	public int getTransferID() {
		return this.transferID;
	}
	
	public void setTransferID(int theTransferID) {
		this.transferID = theTransferID;
	}
	
	public String getTransferTargetType() {
		return transferTargetType;
	}

	public void setTransferTargetType(String transferTargetType) {
		this.transferTargetType = transferTargetType;
	}
	
	public int getTargetHazardID() {
		return this.targetHazardID;
	}
	
	public void setTargetHazardID(int theTargetHazardID) {
		this.targetHazardID = theTargetHazardID;
	}
	
	public String getTargetHazardTitle() {
		return this.targetHazardTitle;
	}
	
	public void setTargetHazardTitle(String theTargetHazardTitle) {
		this.targetHazardTitle = theTargetHazardTitle;
	}
	
	public String getTargetHazardNumber() {
		return this.targetHazardNumber;
	}
	
	public void setTargetHazardNumber(String theTargetHazardNumber) {
		this.targetHazardNumber = theTargetHazardNumber;
	}
	
	public String getTransferReason() {
		return this.transferReason;
	}
	
	public void setTransferReason(String transferReason) {
		this.transferReason = transferReason;
	}
	
	public int getTargetID() {
		return this.targetID;
	}
	
	public void setTargetID(int theTargetID) {
		this.targetID = theTargetID;
	}
	
	public int getOriginID() {
		return this.originID;
	}
	
	public void setOriginID(int theOriginID) {
		this.originID = theOriginID;
	}
	
	public boolean isDeleted() {
		return this.deleted;
	}
	
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public int getTargetControlNumber() {
		return this.targetControlNumber;
	}
	
	public void setTargetControlNumber(int theTargetControlNumber) {
		this.targetControlNumber = theTargetControlNumber;
	}
	
	public String getTargetControlDescription() {
		return this.targetControlDescription;
	}
	
	public void setTargetControlDescription(String theTargetControlDescription) {
		this.targetControlDescription = theTargetControlDescription;
	}
	
	public String getTargetControlControlGroup() {
		return targetControlControlGroup;
	}

	public void setTargetControlControlGroup(String targetControlControlGroup) {
		this.targetControlControlGroup = targetControlControlGroup;
	}
	
	public int getTargetCauseNumber() {
		return this.targetCauseNumber;
	}
	
	public void setTargetCauseNumber(int theTargetCauseNumber) {
		this.targetCauseNumber = theTargetCauseNumber;
	}

	public String getTargetCauseTitle() {
		return this.targetCauseTitle;
	}
	
	public void setTargetCauseTitle(String theTargetCauseTitle) {
		this.targetCauseTitle = theTargetCauseTitle;
	}
}
