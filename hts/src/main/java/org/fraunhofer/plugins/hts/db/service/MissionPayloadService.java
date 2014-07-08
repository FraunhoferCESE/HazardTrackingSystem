package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.Mission_Payload;

public interface MissionPayloadService {
	Mission_Payload add(String name);

	Mission_Payload getMissionPayloadByID(String id);

	Mission_Payload update(Mission_Payload payloadToUpdate, String name);

	Mission_Payload getMissionPayloadByName(String name);

	List<Mission_Payload> all();

	Boolean payloadNameExists(String payloadName);

	void deleteMissionPayload(int id);
}
