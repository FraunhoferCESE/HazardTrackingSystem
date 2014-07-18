package org.fraunhofer.plugins.hts.datatype;

public class HazardControlTransfers {
	// Joint - general:
	private int transferID;
	private int targetHazardID;
	private int targetID;
	private int originID;
	private String targetType;
	// Joint:
	private String originHazardControlTransferReason;
	private String targetHazardNo;
	private String targetHazardTitle;
	// For single control transfer only:
	private String targetHazardControlNo;
	private String targetHazardControlDescription;
	// For entire cause transfer only;
	private String targetHazardCauseNo;
	private String targetHazardCauseTitle;
	
	public int getTransferID() {
		return this.transferID;
	}
	
	public void setTransferID(int theTransferID) {
		this.transferID = theTransferID;
	}
	
	public int getTargetHazardID() {
		return this.targetHazardID;
	}
	
	public void setTargetHazardID(int theTargetHazardID) {
		this.targetHazardID = theTargetHazardID;
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
	
	public String getTargetType() {
		return this.targetType;
	}
	
	public void setTargetType(String theTargetType) {
		this.targetType = theTargetType;
	}
	
	public String getOriginHazardControlTransferReason() {
		return this.originHazardControlTransferReason;
	}
	
	public void setOriginHazardControlTransferReason(String theOriginHazardControlTransferReason) {
		this.originHazardControlTransferReason = theOriginHazardControlTransferReason;
	}
	
	public String getTargetHazardNo() {
		return this.targetHazardNo;
	}
	
	public void setTargetHazardNo(String theTargetHazardNo) {
		this.targetHazardNo = theTargetHazardNo;
	}
	
	public String getTargetHazardTitle() {
		return this.targetHazardTitle;
	}
	
	public void setTargetHazardTitle(String theTargetHazardTitle) {
		this.targetHazardTitle = theTargetHazardTitle;
	}
	
	public String getTargetHazardControlNo() {
		return this.targetHazardControlNo;
	}
	
	public void setTargetHazardControlNo(String theTargetHazardControlNo) {
		this.targetHazardControlNo = theTargetHazardControlNo;
	}
	
	public String getTargetHazardControlDescription() {
		return this.targetHazardControlDescription;
	}
	
	public void setTargetHazardControlDescription(String theTargetHazardControlDescription) {
		this.targetHazardControlDescription = theTargetHazardControlDescription;
	}
	
	public String getTargetHazardCauseNo() {
		return this.targetHazardCauseNo;
	}
	
	public void setTargetHazardCauseNo(String theTargetHazardCauseNo) {
		this.targetHazardCauseNo = theTargetHazardCauseNo;
	}
	
	public String getTargetHazardCauseTitle() {
		return this.targetHazardCauseTitle;
	}
	
	public void setTargetHazardCauseTitle(String theTargetHazardCauseTitle) {
		this.targetHazardCauseTitle = theTargetHazardCauseTitle;
	}
}
