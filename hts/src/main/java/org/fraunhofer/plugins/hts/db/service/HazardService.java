package org.fraunhofer.plugins.hts.db.service;

import java.util.Date;
import java.util.List;

import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Payload;
import org.fraunhofer.plugins.hts.db.Mission_Phase;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.Subsystems;

import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public interface HazardService {
	Hazards add(String title, String safetyRequirements, String description, String justification, String openWork, String preparer, String email, 
			String hazardNum, String hazardVersionNum, Date initationDate, Date completionDate, Date lastEdit, Hazard_Group[] groups,  
			Review_Phases reviewPhase, Subsystems[] subsystems, Mission_Phase[] missionPhase, Mission_Payload missionPayload);
	
	Hazards addFromJira(String title, String hazardNum, Mission_Payload missionPayload);

	Hazards getHazardByID(String id);

	Hazards update(String id, String title, String safetyRequirements, String description, String justification, String openWork, String preparer,
			String email, String hazardNum, String hazardVersionNum, Date initationDate, Date completionDate, Date revisionDate, Hazard_Group[] groups,  
			Review_Phases reviewPhase, Subsystems[] subsystems, Mission_Phase[] missionPhase, Mission_Payload missionPayload);

	List<Hazards> all();
	
	List<Hazards> getAllNonDeletedHazards();

	Boolean hazardNumberExists(String hazardNumber);

	List<Hazards> getHazardsByMissionPayload(String string);

	void deleteHazard(Hazards hazardToDelete);

	Hazards getNewestHazardReport();

	Hazards getHazardByHazardNum(String hazardNum);
}
