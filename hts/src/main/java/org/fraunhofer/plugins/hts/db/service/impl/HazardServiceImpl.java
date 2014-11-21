package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.java.ao.DBParam;
import net.java.ao.Query;

import org.fraunhofer.plugins.hts.datatype.HazardDTMinimal;
import org.fraunhofer.plugins.hts.datatype.HazardDTMinimalJson;
import org.fraunhofer.plugins.hts.db.GroupToHazard;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Phase;
import org.fraunhofer.plugins.hts.db.PhaseToHazard;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.SubsystemToHazard;
import org.fraunhofer.plugins.hts.db.Subsystems;
import org.fraunhofer.plugins.hts.db.Verifications;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.issues.PluginCustomization;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;

public class HazardServiceImpl implements HazardService {
	private final ActiveObjects ao;

	public HazardServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}
	
	@Override
	public Hazards add(String title, String hazardNum, String jiraURL, Long projectID, Long issueID) {
		final Hazards hazard = ao.create(Hazards.class, new DBParam("PROJECT_ID", projectID), new DBParam("ISSUE_ID", issueID));
		hazard.setHazardTitle(title);
		hazard.setHazardNumber(hazardNum);
		hazard.setJiraURL(jiraURL);
		hazard.setRevisionDate(new Date());
		hazard.setActive(true);
		hazard.save();
		return hazard;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Hazards update(int hazardID, String hazardNumber, String version, String hazardTitle, Subsystems[] subsystems, Review_Phases reviewPhase, 
			Mission_Phase[] missionPhases, Hazard_Group[] hazardGroups, String safetyRequirements, String description, String justification, 
			String openWork, Date initiation, Date completion) {
		Hazards hazard = getHazardByID(hazardID);

		PluginCustomization pluginCustomization = null;
		try {
			// Update issue in JIRA ITS
			pluginCustomization = PluginCustomization.getInstance();
			IssueManager issueManager = ComponentAccessor.getIssueManager();
			CustomField hazardNumberField = pluginCustomization.getHazardNumberField();
			hazardNumberField.getCustomFieldType().updateValue(hazardNumberField, issueManager.getIssueObject(hazard.getIssueID()), hazardNumber);
			CustomField hazardTitleField = pluginCustomization.getHazardTitleField();
			hazardTitleField.getCustomFieldType().updateValue(hazardTitleField, issueManager.getIssueObject(hazard.getIssueID()), hazardTitle);
		} catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Update hazard in HTS
		hazard.setHazardNumber(hazardNumber);
		hazard.setHazardVersionNumber(version);
		hazard.setHazardTitle(hazardTitle);
		hazard.setRevisionDate(new Date());
		
		removeSubsystems(hazard.getID());
		if (subsystems != null) {
			for (Subsystems subsystem : subsystems) {
				try {
					associateSubsystemToHazard(subsystem, hazard);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		hazard.setReviewPhase(reviewPhase);
		
		if (missionPhases != null) {
			for (Mission_Phase phase : missionPhases) {
				try {
					associateMissionPhaseToHazard(phase, hazard);
				} catch (SQLException e) {
					// TODO: handle exception
				}
			}
		}
		
		if (hazardGroups != null) {
			for (Hazard_Group group : hazardGroups) {
				try {
					associateHazardGroupToHazard(group, hazard);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		hazard.setHazardSafetyRequirements(safetyRequirements);
		hazard.setHazardDescription(description);
		hazard.setHazardJustification(justification);
		hazard.setHazardOpenWork(openWork);
		hazard.setInitiationDate(initiation);
		hazard.setCompletionDate(completion);
		
		hazard.save();
		return hazard;
	}

	@Override
	public Hazards update(Hazards hazard, String hazardTitle, String hazardNumber) {
		hazard.setHazardTitle(hazardTitle);
		hazard.setHazardNumber(hazardNumber);
		hazard.save();
		return hazard;
	}

	@Override
	public List<Hazards> getAllHazards() {
		List<Hazards> hazards = newArrayList(ao.find(Hazards.class));
		return hazards;
	}
	
	@Override
	public List<Hazards> getAllNonDeletedHazards() {
		List<Hazards> hazards = newArrayList(ao.find(Hazards.class, Query.select().where("ACTIVE=?", true)));
		return hazards;
	}
	
	@Override
	public List<HazardDTMinimal> getAllHazardsMinimal() {
		List<Hazards> allHazards = getAllNonDeletedHazards();
		List<HazardDTMinimal> allHazardsMinimal = new ArrayList<HazardDTMinimal>();
		ProjectManager projectManager = ComponentAccessor.getProjectManager();
		for (Hazards hazard : allHazards) {
			Project jiraProject = projectManager.getProjectObj(hazard.getProjectID());
			allHazardsMinimal.add(new HazardDTMinimal(
						hazard.getID(),
						hazard.getHazardTitle(),
						hazard.getHazardNumber(),
						jiraProject.getName(),
						hazard.getRevisionDate().toString(),
						hazard.getJiraURL()
					));
		}
		return allHazardsMinimal;
	}
	
	@Override
	public List<HazardDTMinimalJson> getAllHazardsMinimalJson() {
		List<Hazards> allHazards = getAllNonDeletedHazards();
		List<HazardDTMinimalJson> allHazardsMinimal = new ArrayList<HazardDTMinimalJson>();
		ProjectManager projectManager = ComponentAccessor.getProjectManager();
		for (Hazards hazard : allHazards) {
			Project jiraProject = projectManager.getProjectObj(hazard.getProjectID());
			allHazardsMinimal.add(new HazardDTMinimalJson(
						hazard.getID(),
						hazard.getHazardTitle(),
						hazard.getHazardNumber(),
						jiraProject.getName(),
						hazard.getRevisionDate().toString(),
						hazard.getJiraURL()
					));
		}
		return allHazardsMinimal;
	}
	
	@Override
	public List<Hazards> getAllHazardsByMissionID(Long missionID) {
		List<Hazards> hazards = newArrayList(ao.find(Hazards.class, Query.select().where("PROJECT_ID=?", missionID)));
		return hazards;
	}
	
	@Override
	public List<HazardDTMinimalJson> getAllHazardsByMissionIDMinimalJson(Long missionID) {
		List<Hazards> allHazards = getAllHazardsByMissionID(missionID);
		List<HazardDTMinimalJson> allHazardsByIDMinimal = new ArrayList<HazardDTMinimalJson>();
		ProjectManager projectManager = ComponentAccessor.getProjectManager();
		for (Hazards hazard : allHazards) {
			Project jiraProject = projectManager.getProjectObj(hazard.getProjectID());
			allHazardsByIDMinimal.add(new HazardDTMinimalJson(
						hazard.getID(),
						hazard.getHazardTitle(),
						hazard.getHazardNumber(),
						jiraProject.getName(),
						hazard.getRevisionDate().toString(),
						hazard.getJiraURL()
					));
		}
		return allHazardsByIDMinimal;
	}
	
	@Override
	public Hazards getHazardByID(int hazardID) {
		final Hazards[] hazards = ao.find(Hazards.class, Query.select().where("ID=?", hazardID));
		return hazards.length > 0 ? hazards[0] : null;
	}
	
	@Override
	public Hazards getHazardByID(String hazardID) {
		return getHazardByID(Integer.parseInt(hazardID));
	}
	
	@Override
	public Hazards getHazardByIssueID(Long issueID) {
		final Hazards[] hazards = ao.find(Hazards.class, Query.select().where("ISSUE_ID=?", issueID));
		return hazards.length > 0 ? hazards[0] : null;
	}
	
	@Override
	public List<Hazards> getHazardsByMissionPayload(Long id) {
		List<Hazards> allActiveHazards = newArrayList(ao.find(Hazards.class, Query.select().where("PROJECT_ID=? AND ACTIVE=?", id, true)));
		return allActiveHazards;
	}
	
	@Override
	public List<Hazards> getHazardsByMissionPayload(String id) {
		return getHazardsByMissionPayload(Long.parseLong(id));
	}
	
	@Override
	public String getHazardPreparerInformation(Hazards hazard) {
		IssueManager issueManager = ComponentAccessor.getIssueManager();
		MutableIssue mutableIssue = issueManager.getIssueObject(hazard.getIssueID());
		String information = mutableIssue.getReporter().getDisplayName() + " (" +  mutableIssue.getReporter().getEmailAddress() + ")";
		return information;
	}
	
	@Override
	public void deleteHazard(Hazards hazard) {
		// Mark hazard as inactive
		hazard.setActive(false);
		Date deleteDate = new Date();
		SimpleDateFormat deletedTimestampFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		hazard.setHazardNumber(hazard.getHazardNumber() + " (DELETED " + deletedTimestampFormat.format(deleteDate) + ")");
		hazard.save();
		
		// Mark all non-deleted causes as deleted
		for (Hazard_Causes current : hazard.getHazardCauses()) {
			current.setDeleteReason("HAZARD_DELETED");
			current.save();
		}

		// Mark all non-deleted controls as deleted
		for (Hazard_Controls current : hazard.getHazardControls()) {
			current.setDeleteReason("HAZARD_DELETED");
			current.save();
		}
		
		// Mark all non-deleted verifications as delete
		for (Verifications current : hazard.getVerifications()) {
			current.setDeleteReason("HAZARD_DELETED");
			current.save();
		}
	}
	
	// OLD FUNCTIONS

	@Override
	public Hazards getHazardByHazardNum(String hazardNum) {
		final Hazards[] hazards = ao.find(Hazards.class, Query.select().where("HAZARD_NUM=?", hazardNum));
		return hazards.length > 0 ? hazards[0] : null;
	}

	@Override
	public Hazards update(String id, String title, String safetyRequirements, String description, String justification, String openWork, String preparer, 
			String email, String hazardNum, String hazardVersionNum, Date initationDate, Date completionDate, Date revisionDate, Hazard_Group[] groups, 
			Review_Phases reviewPhase, Subsystems[] subsystems, Mission_Phase[] missionPhase) {
		Hazards updated = getHazardByID(id);
		if (updated != null) {
			updated.setHazardTitle(title);
			updated.setHazardNumber(hazardNum);
			updated.setHazardVersionNumber(hazardVersionNum);
			updated.setHazardSafetyRequirements(safetyRequirements);
			updated.setHazardDescription(description);
			updated.setHazardJustification(justification);
			updated.setHazardOpenWork(openWork);
			updated.setPreparer(preparer);
			updated.setEmail(email);
			updated.setInitiationDate(initationDate);
			updated.setCompletionDate(completionDate);
			updated.setRevisionDate(revisionDate);
			updated.setReviewPhase(reviewPhase);
			updated.save();
			removeSubsystems(updated.getID());
			removeHazardGroups(updated.getID());
			removeMissionPhase(updated.getID());
			if (subsystems != null) {
				for (Subsystems subsystem : subsystems) {
					try {
						associateSubsystemToHazard(subsystem, updated);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if (groups != null) {
				for (Hazard_Group group : groups) {
					try {
						associateHazardGroupToHazard(group, updated);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			if (missionPhase != null) {
				for (Mission_Phase phase : missionPhase) {
					try {
						associateMissionPhaseToHazard(phase, updated);
					} catch (SQLException e) {
						// TODO: handle exception
					}
				}
			}
		}
		return updated;
	}
	
	@Override
	public List<Long> getProjectsWithHazards() {
		List<Long> ids = new ArrayList<Long>();
		Hazards[] hazards = ao.find(Hazards.class, Query.select("PROJECT_ID").where("ACTIVE=?", true).distinct());
		if (hazards != null) {
			for (Hazards hazard : hazards) {
				ids.add(new Long(hazard.getID()));
			}
		}
		return ids;
	}

	
	
	
	private void associateSubsystemToHazard(Subsystems subsystems, Hazards hazard) throws SQLException {
		final SubsystemToHazard subsystemToHazard = ao.create(SubsystemToHazard.class);
		subsystemToHazard.setSubsystem(subsystems);
		subsystemToHazard.setHazard(hazard);
		subsystemToHazard.save();
	}

	private void associateHazardGroupToHazard(Hazard_Group hazardGroup, Hazards hazard) throws SQLException {
		final GroupToHazard hazardGroupToHazard = ao.create(GroupToHazard.class);
		hazardGroupToHazard.setHazardGroup(hazardGroup);
		hazardGroupToHazard.setHazard(hazard);
		hazardGroupToHazard.save();
	}

	private void associateMissionPhaseToHazard(Mission_Phase phase, Hazards hazard) throws SQLException {
		final PhaseToHazard phaseToHazard = ao.create(PhaseToHazard.class);
		phaseToHazard.setMissionPhase(phase);
		phaseToHazard.setHazard(hazard);
		phaseToHazard.save();
	}

	private void removeMissionPhase(int id) {
		ao.delete(ao.find(PhaseToHazard.class, Query.select().where("HAZARD_ID=?", id)));
	}

	private void removeSubsystems(int id) {
		ao.delete(ao.find(SubsystemToHazard.class, Query.select().where("HAZARD_ID=?", id)));
	}

	private void removeHazardGroups(int id) {
		ao.delete(ao.find(GroupToHazard.class, Query.select().where("HAZARD_ID=?", id)));
	}
	
}
