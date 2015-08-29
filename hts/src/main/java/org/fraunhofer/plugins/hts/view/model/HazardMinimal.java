package org.fraunhofer.plugins.hts.view.model;

import java.util.Date;

public class HazardMinimal {
	private int hazardID;
	private String hazardTitle;
	private String hazardNumber;
	private String jiraSubtaskSummary;
	private String jiraSubtaskURL;
	private String missionTitle;
	private String jiraProjectURL;
	private Date revisionDate;
	
	public HazardMinimal(int hazardID, String hazardTitle, String hazardNumber, String jiraSubtaskSummary,
			String jiraSubtaskURL, String missionTitle, String jiraProjectURL, Date lastRevision) {
		this.hazardID = hazardID;
		this.hazardTitle = hazardTitle;
		this.hazardNumber = hazardNumber;
		this.jiraSubtaskSummary = jiraSubtaskSummary;
		this.jiraSubtaskURL = jiraSubtaskURL;
		this.missionTitle = missionTitle;
		this.jiraProjectURL = jiraProjectURL;
		this.revisionDate = lastRevision;
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
	
	public String getJiraSubtaskSummary() {
		return jiraSubtaskSummary;
	}
	
	public String getJiraSubtaskURL() {
		return jiraSubtaskURL;
	}

	public String getMissionTitle() {
		return missionTitle;
	}
	
	public String getJiraProjectURL() {
		return jiraProjectURL;
	}

	public Date getRevisionDate() {
		return revisionDate;
	}

}
