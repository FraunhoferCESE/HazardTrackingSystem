package org.fraunhofer.plugins.hts.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.fraunhofer.plugins.hts.db.Hazard_Controls;

@XmlRootElement(name = "controlResponseList")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class HazardControlResponseList {
	private int controlID;
	private String controlNumber;
	private String description;
	
	public int getControlID() {
		return controlID;
	}
	
	public String getControlNumber() {
		return controlNumber;
	}
	
	public String getDescription() {
		return description;
	}
	
	public static HazardControlResponseList control(Hazard_Controls control) {
		HazardControlResponseList list = new HazardControlResponseList();
		list.controlID = control.getID();
		list.controlNumber = control.getControlNumber();
		list.description = control.getDescription();
		return list;
	}
}
