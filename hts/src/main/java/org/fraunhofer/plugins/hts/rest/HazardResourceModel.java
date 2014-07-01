package org.fraunhofer.plugins.hts.rest;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public class HazardResourceModel {

	@XmlElement(name = "value")
	private String message;

	public HazardResourceModel() {
	}

	public HazardResourceModel(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
