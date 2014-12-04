package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.java.ao.DBParam;
import net.java.ao.Query;

import org.fraunhofer.plugins.hts.datatype.HazardDTMinimal;
import org.fraunhofer.plugins.hts.datatype.HazardDTMinimalJson;
import org.fraunhofer.plugins.hts.datatype.JIRAProject;
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
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.Lists;

public class HazardServiceImpl implements HazardService {
	private final ActiveObjects ao;

	public HazardServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}
	
	@Override
	public Hazards add(String title, String hazardNum, Long projectID, Long issueID) {
		final Hazards hazard = ao.create(Hazards.class, new DBParam("PROJECT_ID", projectID), new DBParam("ISSUE_ID", issueID));
		hazard.setHazardTitle(title);
		hazard.setHazardNumber(hazardNum);
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
	public List<HazardDTMinimal> getUserHazardsMinimal(List<JIRAProject> projects) {
		ProjectManager projectManager = ComponentAccessor.getProjectManager();
		List<HazardDTMinimal> hazardsMinimal = new ArrayList<HazardDTMinimal>();
		for (JIRAProject project : projects) {
			for (Hazards hazard : getHazardsByMissionPayload(project.getID())) {
				Project jiraProject = getHazardProject(hazard);
				Issue jiraSubtask = getHazardSubTask(hazard);
				String baseURL = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");
				
				hazardsMinimal.add(new HazardDTMinimal(
							hazard.getID(),
							hazard.getHazardTitle(),
							hazard.getHazardNumber(),
							jiraSubtask.getSummary(),
							baseURL + "/browse/" + jiraProject.getKey() + "-" + jiraSubtask.getNumber(),
							jiraProject.getName(),
							baseURL + "/browse/" + jiraProject.getKey(),
							hazard.getRevisionDate().toString()
						));		
			}
		}
		return hazardsMinimal;
	}
	
	@Override
	public List<HazardDTMinimalJson> getUserHazardsMinimalJson(ApplicationUser user) {
		List<Hazards> allHazards = getAllNonDeletedHazards();
		List<HazardDTMinimalJson> allHazardsMinimal = new ArrayList<HazardDTMinimalJson>();
		ProjectManager projectManager = ComponentAccessor.getProjectManager();
		for (Hazards hazard : allHazards) {
			if (hasHazardPermission(hazard.getProjectID(), user)) {
				Project jiraProject = getHazardProject(hazard);
				Issue jiraSubtask = getHazardSubTask(hazard);
				String baseURL = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");
				
				allHazardsMinimal.add(new HazardDTMinimalJson(
							hazard.getID(),
							hazard.getHazardTitle(),
							hazard.getHazardNumber(),
							jiraSubtask.getSummary(),
							baseURL + "/browse/" + jiraProject.getKey() + "-" + jiraSubtask.getNumber(),
							jiraProject.getName(),
							baseURL + "/browse/" + jiraProject.getKey(),
							hazard.getRevisionDate().toString()
						));
			}
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
		for (Hazards hazard : allHazards) {
			Project jiraProject = getHazardProject(hazard);
			Issue jiraSubtask = getHazardSubTask(hazard);
			String baseURL = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");
			
			allHazardsByIDMinimal.add(new HazardDTMinimalJson(
						hazard.getID(),
						hazard.getHazardTitle(),
						hazard.getHazardNumber(),
						jiraSubtask.getSummary(),
						baseURL + "/browse/" + jiraProject.getKey() + "-" + jiraSubtask.getNumber(),
						jiraProject.getName(),
						baseURL + "/browse/" + jiraProject.getKey(),
						hazard.getRevisionDate().toString()
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
		return newArrayList(ao.find(Hazards.class, Query.select().where("PROJECT_ID=? AND ACTIVE=?", id, true)));
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
	
	@Override
	public List<Long> getProjectsWithHazards(Collection<Project> userProjects) {
		List<Long> ids = new ArrayList<Long>();
		Hazards[] hazards = ao.find(Hazards.class, Query.select("PROJECT_ID").where("ACTIVE=?", true).distinct());
		if (hazards != null) {
			for (Hazards hazard : hazards) {
				for (Project project : userProjects) {
					if (hazard.getProjectID().equals(project.getId())) {
						ids.add(new Long(hazard.getID()));
					}
				}
			}
		}
		return ids;
	}
	
	@Override
	public Boolean hasHazardPermission(Long projectID, ApplicationUser user) {
		boolean hasPermission;
		ProjectManager projectManager = ComponentAccessor.getProjectManager();
		Project jiraProject = projectManager.getProjectObj(projectID);
		PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
		if (permissionManager.hasPermission(Permissions.CREATE_ISSUE, jiraProject, user) ||
			permissionManager.hasPermission(Permissions.EDIT_ISSUE, jiraProject, user)) {
			hasPermission = true;
		} else {
			hasPermission = false;
		}
		return hasPermission;
	}

	@Override
	public Project getHazardProject(Hazards hazard) {
		return ComponentAccessor.getProjectManager().getProjectObj(hazard.getProjectID());
	}
	
	@Override
	public Issue getHazardSubTask(Hazards hazard) {
		return ComponentAccessor.getIssueManager().getIssueObject(hazard.getIssueID());
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
