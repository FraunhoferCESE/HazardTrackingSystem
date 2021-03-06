package org.fraunhofer.plugins.hts.view.model;

import org.fraunhofer.plugins.hts.model.ControlGroups;
import org.fraunhofer.plugins.hts.model.Hazard_Causes;
import org.fraunhofer.plugins.hts.model.Hazard_Controls;
import org.fraunhofer.plugins.hts.model.Transfers;

import com.google.common.base.Strings;

public class ControlTransfer {
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
	private String movedProject;

	public String getMovedProject() {
		return movedProject;
	}

	public void setMovedProject(String movedProject) {
		this.movedProject = movedProject;
	}

	public static ControlTransfer createControlToControl(Transfers transfer, Hazard_Controls originControl,
			Hazard_Controls targetControl) {
		ControlTransfer instance = new ControlTransfer();
		instance.setMovedProject(null);
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
		Hazard_Causes[] cause = targetControl.getCauses();
		instance.setTargetCauseNumber( cause.length > 0 ? cause[0].getCauseNumber() : -1);

		ControlGroups controlGroup = targetControl.getControlGroup();
		if (controlGroup == null) {
			instance.setTargetControlControlGroup("N/A");
		} else {
			instance.setTargetControlControlGroup(targetControl.getControlGroup().getLabel());
		}

		if (Strings.isNullOrEmpty(targetControl.getDeleteReason())) {
			instance.setDeleted(false);
		} else {
			instance.setDeleted(true);
		}

		return instance;
	}

	public static ControlTransfer createControlToCause(Transfers transfer, Hazard_Controls originControl,
			Hazard_Causes targetCause) {
		ControlTransfer instance = new ControlTransfer();
		instance.setMovedProject(null);
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

	public static ControlTransfer createMovedProjectTransfer(Transfers transfer, String projectName) {
		ControlTransfer instance = new ControlTransfer();
		instance.setTransferID(transfer.getID());
		instance.setTransferTargetType(transfer.getTargetType());
		instance.setOriginID(transfer.getOriginID());
		instance.setMovedProject(projectName);
		return instance;
	}
}
