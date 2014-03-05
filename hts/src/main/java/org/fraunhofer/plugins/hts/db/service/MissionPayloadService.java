package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Payload;

public interface MissionPayloadService {
	Mission_Payload add(Hazards hazard, String name);
	Mission_Payload getMissionPayloadByID(String id);
	List<Mission_Payload> all();
	
}
