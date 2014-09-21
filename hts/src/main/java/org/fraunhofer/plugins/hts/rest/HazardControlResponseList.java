package org.fraunhofer.plugins.hts.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.fraunhofer.plugins.hts.db.Hazard_Controls;

import com.google.common.base.Strings;

@XmlRootElement(name = "controlResponseList")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class HazardControlResponseList {
	private int controlID;
	private int controlNumber;
	private String description;
	private Boolean active;
	private String type;
	
	public int getControlID() {
		return controlID;
	}
	
	public int getControlNumber() {
		return controlNumber;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Boolean getActive() {
		return active;
	}
	
	public String getType() {
		return type;
	}
	
	public static HazardControlResponseList control(Hazard_Controls control) {
		HazardControlResponseList list = new HazardControlResponseList();
		list.controlID = control.getID();
		list.controlNumber = control.getControlNumber();
		list.description = control.getDescription();
		
		if (Strings.isNullOrEmpty(control.getDeleteReason())) {
			list.active = true;
		}
		else {
			list.active = false;
		}
		
		list.type = "CONTROL";
		return list;
	}
}
