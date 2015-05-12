package org.fraunhofer.plugins.hts.view.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "HazardJsonDT")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class HazardMinimalJSON {
	private int hazardID;
	private String hazardTitle;
	private String hazardNumber;
	private String jiraSubtaskSummary;
	private String jiraSubtaskURL;
	private String missionTitle;
	private String jiraProjectURL;
	private String revisionDate;
	
	public HazardMinimalJSON(int hazardID, String hazardTitle, String hazardNumber, String jiraSubtaskSummary,
			String jiraSubtaskURL, String missionTitle, String jiraProjectURL, String lastRevision) {
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

	public String getRevisionDate() {
		return revisionDate;
	}
}