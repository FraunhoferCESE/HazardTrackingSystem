package org.fraunhofer.plugins.hts.datatype;

public class TransferClass {
	private int transferID;
	private String transferReason;
	private String hazardTitle;
	private String hazardNumb;
	private String causeTitle;
	
	public TransferClass(int transferID, String transferReason, String hazardTitle, String hazardNumb, String causeTitle) {
		this.transferID = transferID;
		this.transferReason = transferReason;
		this.hazardTitle = hazardTitle;
		this.hazardNumb = hazardNumb;
		this.causeTitle = causeTitle;
	}
	
	public int getTransferID() {
		return this.transferID;
	}
	
	public String getTransferReason() {
		return this.transferReason;
	}
	
	public String getHazardTitle() {
		return this.hazardTitle;
	}
	
	public String getHazardNumb() {
		return this.hazardNumb;
	}
	
	public String getCauseTitle() {
		return this.causeTitle;
	}
}
