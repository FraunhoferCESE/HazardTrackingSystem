package org.fraunhofer.plugins.hts.datatype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "HazardCauseDTMinimalJson")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class HazardCauseDTMinimalJson {
	private int causeID;
	private int causeNumber;
	private String text;
	private boolean transfer;
	private boolean active;
	private String type;
	
	public HazardCauseDTMinimalJson(int causeID, int causeNumber, String text,
			boolean transfer, boolean active, String type) {
		this.causeID = causeID;
		this.causeNumber = causeNumber;
		this.text = text;
		this.transfer = transfer;
		this.active = active;
		this.type = type;
	}
	
	public int getCauseID() {
		return causeID;
	}

	public int getCauseNumber() {
		return causeNumber;
	}
	
	public String getText() {
		return text;
	}

	public boolean getTransfer() {
		return transfer;
	}
	
	public Boolean getActive() {
		return active;
	}

	public String getType() {
		return type;
	}

}