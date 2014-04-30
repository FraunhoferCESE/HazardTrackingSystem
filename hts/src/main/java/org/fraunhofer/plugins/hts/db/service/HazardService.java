package org.fraunhofer.plugins.hts.db.service;

import java.util.Date;
import java.util.List;

import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Payload;
import org.fraunhofer.plugins.hts.db.Mission_Phase;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.Risk_Categories;
import org.fraunhofer.plugins.hts.db.Risk_Likelihoods;
import org.fraunhofer.plugins.hts.db.Subsystems;

import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public interface HazardService {
	Hazards add(String title, String description, String preparer, String email, String hazardNum, Date initationDate,
			Date completionDate, Date lastEdit, Risk_Categories risk, Risk_Likelihoods likelihood, Hazard_Group[] groups,
			Review_Phases reviewPhase, Subsystems[] subsystems, Mission_Phase[] missionPhase, Mission_Payload missionPayload);

	Hazards getHazardByID(String id);

	Hazards update(String id, String title, String description, String preparer, String email, String hazardNum,
			Date initationDate, Date completionDate, Date revisionDate, Risk_Categories risk,
			Risk_Likelihoods likelihood, Hazard_Group[] groups, Review_Phases reviewPhase, Subsystems[] subsystems, Mission_Phase[] missionPhase, Mission_Payload missionPayload);

	List<Hazards> all();

	Boolean hazardNumberExists(String hazardNumber);
	
	List<Hazards> getHazardsByMissionPayload(String string);
	
	void deleteHazard(int id);
	
}
