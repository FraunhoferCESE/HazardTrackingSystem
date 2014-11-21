package org.fraunhofer.plugins.hts.datatype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.fraunhofer.plugins.hts.db.Hazard_Causes;

@XmlRootElement(name = "HazardCauseDTMinimalJson")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class HazardCauseDTMinimalJson {
	private int causeID;
	private String title;
	private int causeNumber;
	private boolean transfer;
	
	public HazardCauseDTMinimalJson(Hazard_Causes cause) {
		this.causeID = cause.getID();
		this.title = cause.getTitle();
		this.causeNumber = cause.getCauseNumber();
		if (cause.getTransfer() == 0) {
			this.transfer = false;
		} else {
			this.transfer = true;
		}
	}
	
	public int getCauseID() {
		return causeID;
	}

	public String getTitle() {
		return title;
	}

	public int getCauseNumber() {
		return causeNumber;
	}

	public boolean isTransfer() {
		return transfer;
	}
}