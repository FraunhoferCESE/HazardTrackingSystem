package org.fraunhofer.plugins.hts.db.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.fraunhofer.plugins.hts.datatype.HazardDTMinimal;
import org.fraunhofer.plugins.hts.datatype.HazardDTMinimalJson;
import org.fraunhofer.plugins.hts.datatype.JIRAProject;
import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Phase;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.Subsystems;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

@Transactional
public interface HazardService {
	Hazards add(String title, String hazardNum, Long projectID, Long issueID);
	
	Hazards update(int hazardID, String hazardNumber, String version, String hazardTitle, Subsystems[] subsystems, Review_Phases reviewPhase,
			Mission_Phase[] missionPhases, Hazard_Group[] hazardGroups, String safetyRequirements, String description, String justification,
			String openWork, Date initiation, Date completion);
	
	Hazards update(Hazards hazard, String hazardTitle, String hazardNumber);
	
	List<Hazards> getAllHazards();
	
	List<Hazards> getAllNonDeletedHazards();
	
	List<HazardDTMinimal> getUserHazardsMinimal(List<JIRAProject> projects);
	
	List<HazardDTMinimalJson> getUserHazardsMinimalJson(ApplicationUser user);
	
	List<Hazards> getAllHazardsByMissionID(Long missionID);
	
	List<HazardDTMinimalJson> getAllHazardsByMissionIDMinimalJson(Long missionID);
	
	Hazards getHazardByID(int hazardID);
	
	Hazards getHazardByID(String hazardID);
	
	Hazards getHazardByIssueID(Long issueID);
	
	List<Hazards> getHazardsByMissionPayload(Long id);
	
	List<Hazards> getHazardsByMissionPayload(String string);
	
	String getHazardPreparerInformation(Hazards hazard);
	
	void deleteHazard(Hazards hazard, String reason);
	
	List<Long> getProjectsWithHazards();
	
	List<Long> getProjectsWithHazards(Collection<Project> userProjects);
	
	Boolean hasHazardPermission(Long projectID, ApplicationUser user);
	
	Project getHazardProject(Hazards hazard);
	
	Issue getHazardSubTask(Hazards hazard);
}
