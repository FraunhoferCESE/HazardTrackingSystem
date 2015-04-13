package org.fraunhofer.plugins.hts.datatype;

import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Risk_Categories;
import org.fraunhofer.plugins.hts.db.Risk_Likelihoods;
import org.fraunhofer.plugins.hts.db.Transfers;

import com.google.common.base.Strings;

public class HazardCauseTransferDT {
	// Joint
	private int transferID;
	private String transferTargetType;
	private int targetHazardID;
	private String targetHazardTitle;
	private String targetHazardNumber;
	private String transferReason;
	private int targetID; // targetCauseID or targetHazardID
	private int originID; // always originCauseID, might not need this?
	private boolean deleted;
	// For CauseToCause transfers
	private String targetCauseTitle;
	private int targetCauseNumber;
	private String targetCauseOwner;
	private String targetCauseRiskCategoryTitle;
	private String targetCauseRiskLikelihoodTitle;
	// For CauseToHazard transfers
	// ...
	
	public static HazardCauseTransferDT createCauseToCause(Transfers transfer,
			Hazard_Causes originCause, Hazard_Causes targetCause) {
		HazardCauseTransferDT instance = new HazardCauseTransferDT();
		instance.setTransferID(transfer.getID());
		instance.setTransferTargetType(transfer.getTargetType());
		instance.setTargetHazardID(targetCause.getHazards()[0].getID());
		instance.setTargetHazardTitle(targetCause.getHazards()[0].getHazardTitle());
		instance.setTargetHazardNumber(targetCause.getHazards()[0].getHazardNumber());
		instance.setTransferReason(originCause.getDescription());
		instance.setTargetID(transfer.getTargetID()); //
		instance.setOriginID(transfer.getOriginID()); //
		instance.setTargetCauseTitle(targetCause.getTitle());
		instance.setTargetCauseNumber(targetCause.getCauseNumber());
		instance.setTargetCauseOwner(targetCause.getOwner());
		
		Risk_Categories category = targetCause.getRiskCategory();
		if (category == null) {
			instance.setTargetCauseRiskCategoryTitle("Not specified");
		} else {
			instance.setTargetCauseRiskCategoryTitle(targetCause.getRiskCategory().getValue());
		}
		
		Risk_Likelihoods likelihood = targetCause.getRiskLikelihood();
		if (likelihood == null) {
			instance.setTargetCauseRiskLikelihoodTitle("Not specified");
		} else {
			instance.setTargetCauseRiskLikelihoodTitle(targetCause.getRiskLikelihood().getValue());
		}
		
		if (Strings.isNullOrEmpty(targetCause.getDeleteReason())) {
			instance.setDeleted(false);
		} else {
			instance.setDeleted(true);
		}
		
		return instance;
	}
	
	public static HazardCauseTransferDT createCauseToHazard(Transfers transfer,
			Hazard_Causes originCause, Hazards targetHazard) {
		HazardCauseTransferDT instance = new HazardCauseTransferDT();
		instance.setTransferID(transfer.getID());
		instance.setTransferTargetType(transfer.getTargetType());
		instance.setTargetHazardID(targetHazard.getID());
		instance.setTargetHazardTitle(targetHazard.getHazardTitle());
		instance.setTargetHazardNumber(targetHazard.getHazardNumber());
		instance.setTransferReason(originCause.getDescription());
		instance.setTargetID(transfer.getTargetID());
		instance.setOriginID(transfer.getOriginID());
		
		if (targetHazard.getActive() == true) {
			instance.setDeleted(false);
		} else {
			instance.setDeleted(true);
		}
		
		return instance;
	}
	
	public int getTransferID() {
		return transferID;
	}
	
	public void setTransferID(int transferID) {
		this.transferID = transferID;
	}
	
	public String getTransferTargetType() {
		return transferTargetType;
	}

	public void setTransferTargetType(String transferTargetType) {
		this.transferTargetType = transferTargetType;
	}
	
	public int getTargetHazardID() {
		return targetHazardID;
	}
	
	public void setTargetHazardID(int targetHazardID) {
		this.targetHazardID = targetHazardID;
	}
	
	public String getTargetHazardTitle() {
		return targetHazardTitle;
	}
	
	public void setTargetHazardTitle(String targetHazardTitle) {
		this.targetHazardTitle = targetHazardTitle;
	}
	
	public String getTargetHazardNumber() {
		return targetHazardNumber;
	}
	
	public void setTargetHazardNumber(String targetHazardNumber) {
		this.targetHazardNumber = targetHazardNumber;
	}
	
	public String getTransferReason() {
		return transferReason;
	}
	
	public void setTransferReason(String transferReason) {
		this.transferReason = transferReason;
	}
	
	public int getTargetID() {
		return targetID;
	}
	
	public void setTargetID(int targetID) {
		this.targetID = targetID;
	}
	
	public int getOriginID() {
		return originID;
	}
	
	public void setOriginID(int originID) {
		this.originID = originID;
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public String getTargetCauseTitle() {
		return targetCauseTitle;
	}
	
	public void setTargetCauseTitle(String targetCauseTitle) {
		this.targetCauseTitle = targetCauseTitle;
	}
	
	public int getTargetCauseNumber() {
		return targetCauseNumber;
	}

	public void setTargetCauseNumber(int targetCauseNumber) {
		this.targetCauseNumber = targetCauseNumber;
	}
	
	public String getTargetCauseOwner() {
		return targetCauseOwner;
	}

	public void setTargetCauseOwner(String targetCauseOwner) {
		this.targetCauseOwner = targetCauseOwner;
	}
	
	public String getTargetCauseRiskCategoryTitle() {
		return targetCauseRiskCategoryTitle;
	}
	
	public void setTargetCauseRiskCategoryTitle(String targetCauseRiskCategoryTitle) {
		this.targetCauseRiskCategoryTitle = targetCauseRiskCategoryTitle;
	}
	
	public String getTargetCauseRiskLikelihoodTitle() {
		return targetCauseRiskLikelihoodTitle;
	}
	
	public void setTargetCauseRiskLikelihoodTitle(String targetCauseRiskLikelihoodTitle) {
		this.targetCauseRiskLikelihoodTitle = targetCauseRiskLikelihoodTitle;
	}
}