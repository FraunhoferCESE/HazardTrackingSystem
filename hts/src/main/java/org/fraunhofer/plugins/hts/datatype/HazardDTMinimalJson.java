package org.fraunhofer.plugins.hts.datatype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "HazardJsonDT")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class HazardDTMinimalJson {
	private int hazardID;
	private String hazardTitle;
	private String hazardNumber;
	private String missionTitle;
	private String revisionDate;
	private String jiraURL;
	
	public HazardDTMinimalJson(int hazardID, String hazardTitle, String hazardNumber, 
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
