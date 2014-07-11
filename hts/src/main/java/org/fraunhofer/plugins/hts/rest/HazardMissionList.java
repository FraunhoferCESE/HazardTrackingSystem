package org.fraunhofer.plugins.hts.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.fraunhofer.plugins.hts.db.Mission_Payload;

@XmlRootElement(name = "hazardMissionList")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class HazardMissionList {
	private int payloadID;
	private String name;

	public int getPayloadID() {
		return payloadID;
	}

	public String getTitle() {
		return name;
	}

	public static HazardMissionList missionPayloads(Mission_Payload payload) {
		HazardMissionList list = new HazardMissionList();
		list.payloadID = payload.getID();
		list.name = payload.getName();
		return list;
	}
}
