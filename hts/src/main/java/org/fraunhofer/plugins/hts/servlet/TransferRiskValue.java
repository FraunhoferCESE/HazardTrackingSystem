package org.fraunhofer.plugins.hts.servlet;


public class TransferRiskValue {
	private String transferTargetId;
	private String transferTargetType;
	private boolean isCircular;
	private boolean isHazard;
	private int causeNumber;
	private int causeId;
	private String riskLikeliHood;
	private String riskCategory;

	public TransferRiskValue(String transferTargetId, String transferTargetType,
			boolean isCircular, boolean isHazard, int causeNumber, int causeId, String riskCategory, String riskLikeliHood) {
		this.transferTargetId = transferTargetId;
		this.transferTargetType = transferTargetType;
		this.isCircular = isCircular;
		this.isHazard = isHazard;
		this.causeNumber = causeNumber;
		this.causeId = causeId;
		this.riskCategory = riskCategory;
		this.riskLikeliHood = riskLikeliHood;
		
	}


	public String getTransferTargetId() {
		return transferTargetId;
	}

	public void setTransferTargetId(String transferTargetId) {
		this.transferTargetId = transferTargetId;
	}

	public String getTransferTargetType() {
		return transferTargetType;
	}

	public void setTransferTargetType(String transferTargetType) {
		this.transferTargetType = transferTargetType;
	}

	public boolean isCircular() {
		return isCircular;
	}

	public void setCircular(boolean isCircular) {
		this.isCircular = isCircular;
	}

	public boolean isHazard() {
		return isHazard;
	}

	public void setHazard(boolean isTransferred) {
		this.isHazard = isTransferred;
	}


	public int getCauseNumber() {
		return causeNumber;
	}


	public void setCauseNumber(int causeNumber) {
		this.causeNumber = causeNumber;
	}


	public int getCauseId() {
		return causeId;
	}


	public void setCauseId(int causeId) {
		this.causeId = causeId;
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