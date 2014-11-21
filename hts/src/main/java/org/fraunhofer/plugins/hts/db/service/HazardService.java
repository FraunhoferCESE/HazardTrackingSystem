package org.fraunhofer.plugins.hts.db.service;

import java.util.Date;
import java.util.List;

import org.fraunhofer.plugins.hts.datatype.HazardDTMinimal;
import org.fraunhofer.plugins.hts.datatype.HazardDTMinimalJson;
import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Phase;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.Subsystems;

import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public interface HazardService {
	Hazards add(String title, String hazardNum, String jiraURL, Long projectID, Long issueID);
	
	Hazards update(int hazardID, String hazardNumber, String version, String hazardTitle, Subsystems[] subsystems, Review_Phases reviewPhase,
			Mission_Phase[] missionPhases, Hazard_Group[] hazardGroups, String safetyRequirements, String description, String justification,
			String openWork, Date initiation, Date completion);
	
	Hazards update(Hazards hazard, String hazardTitle, String hazardNumber);
	
	List<Hazards> getAllHazards();
	
	List<Hazards> getAllNonDeletedHazards();
	
	List<HazardDTMinimal> getAllHazardsMinimal();
	
	List<HazardDTMinimalJson> getAllHazardsMinimalJson();
	
	List<Hazards> getAllHazardsByMissionID(Long missionID);
	
	List<HazardDTMinimalJson> getAllHazardsByMissionIDMinimalJson(Long missionID);
	
	Hazards getHazardByID(int hazardID);
	
	Hazards getHazardByID(String hazardID);
	
	Hazards getHazardByIssueID(Long issueID);
	
	List<Hazards> getHazardsByMissionPayload(Long id);
	
	List<Hazards> getHazardsByMissionPayload(String string);
	
	String getHazardPreparerInformation(Hazards hazard);
	
	void deleteHazard(Hazards hazard);
		
	
	// OLD FUNCTIONS
	Hazards update(String id, String title, String safetyRequirements, String description, String justification, String openWork, String preparer,
			String email, String hazardNum, String hazardVersionNum, Date initationDate, Date completionDate, Date revisionDate, Hazard_Group[] groups,  
			Review_Phases reviewPhase, Subsystems[] subsystems, Mission_Phase[] missionPhase);

	Hazards getHazardByHazardNum(String hazardNum);
	
	List<Long> getProjectsWithHazards();

	
	
	
}
