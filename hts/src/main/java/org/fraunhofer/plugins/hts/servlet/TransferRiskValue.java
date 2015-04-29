package org.fraunhofer.plugins.hts.servlet;

public class TransferRiskValue {
	private final int transferTargetId;
	private final String transferTargetType;
	private final int causeNumber;
	private final int causeId;

	private boolean isDeleted = false;

	private String riskLikeliHood = null;
	private String riskCategory = null;

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

	public int getCauseId() {
		return causeId;
	}

	public String getRiskLikeliHood() {
		return riskLikeliHood;
	}

	public void setRiskLikeliHood(String riskLikeliHood) {
		this.riskLikeliHood = riskLikeliHood;
	}

	public String getRiskCategory() {
		return riskCategory;
	}

	public void setRiskCategory(String riskCategory) {
		this.riskCategory = riskCategory;
	}
}