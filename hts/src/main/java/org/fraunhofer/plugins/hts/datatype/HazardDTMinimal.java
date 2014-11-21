package org.fraunhofer.plugins.hts.datatype;

public class HazardDTMinimal {
	private int hazardID;
	private String hazardTitle;
	private String hazardNumber;
	private String missionTitle;
	private String revisionDate;
	private String jiraURL;
	
	public HazardDTMinimal(int hazardID, String hazardTitle, String hazardNumber, 
			String missionTitle, String lastRevision, String jiraLink) {
		this.hazardID = hazardID;
		this.hazardTitle = hazardTitle;
		this.hazardNumber = hazardNumber;
		this.missionTitle = missionTitle;
		this.revisionDate = lastRevision;
		this.jiraURL = jiraLink;
	}
	
	public int getHazardID() {
		return hazardID;
	}

	public String getHazardTitle() {
		return hazardTitle;
	}

	public String getHazardNumber() {
		return hazardNumber;
	}

	public String getMissionTitle() {
		return missionTitle;
	}

	public String getRevisionDate() {
		return revisionDate;
	}

	public String getJiraURL() {
		return jiraURL;
	}

}
