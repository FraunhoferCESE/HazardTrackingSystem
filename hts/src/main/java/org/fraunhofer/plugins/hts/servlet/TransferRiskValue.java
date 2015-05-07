package org.fraunhofer.plugins.hts.servlet;

import org.fraunhofer.plugins.hts.model.Risk_Categories;
import org.fraunhofer.plugins.hts.model.Risk_Likelihoods;

public class TransferRiskValue {
	private final int transferTargetId;
	private final String transferTargetType;
	private final int causeNumber;
	private final int causeId;

	private boolean isDeleted = false;

	private Risk_Likelihoods riskLikelihood ;
	private Risk_Categories riskCategory;

	public Risk_Likelihoods getRiskLikelihood() {
		return riskLikelihood;
	}

	public void setRiskLikelihood(Risk_Likelihoods riskLikelihood) {
		this.riskLikelihood = riskLikelihood;
	}

	public Risk_Categories getRiskCategory() {
		return riskCategory;
	}

	public void setRiskCategory(Risk_Categories riskCategory) {
		this.riskCategory = riskCategory;
	}

	public TransferRiskValue(int transferTargetId, String transferTargetType, int causeNumber, int causeId) {
		this.transferTargetId = transferTargetId;
		this.transferTargetType = transferTargetType;
		this.causeNumber = causeNumber;
		this.causeId = causeId;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public int getTransferTargetId() {
		return transferTargetId;
	}

	public String getTransferTargetType() {
		return transferTargetType;
	}

	public int getCauseNumber() {
		return causeNumber;
	}

	public int getID() {
		return causeId;
	}
}