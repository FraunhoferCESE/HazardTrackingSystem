package org.fraunhofer.plugins.hts.datatype;

public class TransferClass {
	private int transferID;
	private String transferReason;
	private String mainTitle;
	private String hazardNumb;
	private String secondaryTitle;
	
	public TransferClass(int transferID, String transferReason, String mainTitle, String hazardNumb, String secondaryTitle) {
		this.transferID = transferID;
		this.transferReason = transferReason;
		this.mainTitle = mainTitle;
		this.hazardNumb = hazardNumb;
		this.secondaryTitle = secondaryTitle;
	}
	
	public int getTransferID() {
		return this.transferID;
	}
	
	public String getTransferReason() {
		return this.transferReason;
	}
	
	public String getMainTitle() {
		return this.mainTitle;
	}
	
	public String getHazardNumb() {
		return this.hazardNumb;
	}
	
	public String getSecondaryTitle() {
		return this.secondaryTitle;
	}
}
