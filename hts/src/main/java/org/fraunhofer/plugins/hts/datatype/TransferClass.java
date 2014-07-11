package org.fraunhofer.plugins.hts.datatype;

public class TransferClass {
	private int transferID;
	private String transferReason;
	private String mainTitle;
	private String hazardNumb;
	private String secondaryTitle;
	private String targetType;
	private int targetID;
	private int hazardID;
	
	public TransferClass(int transferID, String transferReason, String mainTitle, String hazardNumb, String secondaryTitle, String targetType,int hazardID, int targetID) {
		this.transferID = transferID;
		this.transferReason = transferReason;
		this.mainTitle = mainTitle;
		this.hazardNumb = hazardNumb;
		this.secondaryTitle = secondaryTitle;
		this.targetType = targetType;
		this.hazardID = hazardID;
		this.targetID = targetID;
	}
	
	public TransferClass(int transferID, String transferReason, String mainTitle, String hazardNumb, String targetType, int targetID) {
		this.transferID = transferID;
		this.transferReason = transferReason;
		this.mainTitle = mainTitle;
		this.hazardNumb = hazardNumb;
		this.targetType = targetType;
		this.targetID = targetID;
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
	
	public String getTransferType() {
		return this.targetType;
	}
	
	public int getTargetID() {
		return this.targetID;
	}
	
	public int getHazardID() {
		return this.hazardID;
	}
}
