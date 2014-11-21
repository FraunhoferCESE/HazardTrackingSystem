package org.fraunhofer.plugins.hts.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.fraunhofer.plugins.hts.datatype.JIRAProject;

@XmlRootElement(name = "hazardMissionList")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class MissionResponse {
	private Long payloadID;
	private String name;

	public Long getPayloadID() {
		return payloadID;
	}

	public String getTitle() {
		return name;
	}

	public static MissionResponse createMissionReponse(JIRAProject payload) {
		MissionResponse list = new MissionResponse();
		list.payloadID = payload.getID();
		list.name = payload.getName();
		return list;
	}
}
