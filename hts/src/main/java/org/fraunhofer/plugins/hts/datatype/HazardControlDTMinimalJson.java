package org.fraunhofer.plugins.hts.datatype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "HazardControlDTMinimalJson")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class HazardControlDTMinimalJson {
	private int controlID;
	private int controlNumber;
	private String text;
	private Boolean active;
	private String type;
	
	public HazardControlDTMinimalJson(int controlID, int controlNumber,
			String text, Boolean active, String type) {
		this.controlID = controlID;
		this.controlNumber = controlNumber;
		this.text = text;
		this.active = active;
		this.type = type;
	}

	public int getControlID() {
		return controlID;
	}

	public int getControlNumber() {
		return controlNumber;
	}

	public String getText() {
		return text;
	}

	public Boolean getActive() {
		return active;
	}

	public String getType() {
		return type;
	}
}
