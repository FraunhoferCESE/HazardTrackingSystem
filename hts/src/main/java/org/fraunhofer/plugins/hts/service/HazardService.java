package org.fraunhofer.plugins.hts.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.java.ao.DBParam;
import net.java.ao.Query;

import org.fraunhofer.plugins.hts.issues.PluginCustomization;
import org.fraunhofer.plugins.hts.model.GroupToHazard;
import org.fraunhofer.plugins.hts.model.Hazard_Causes;
import org.fraunhofer.plugins.hts.model.Hazard_Controls;
import org.fraunhofer.plugins.hts.model.Hazard_Group;
import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.model.Mission_Phase;
import org.fraunhofer.plugins.hts.model.PhaseToHazard;
import org.fraunhofer.plugins.hts.model.Review_Phases;
import org.fraunhofer.plugins.hts.model.SubsystemToHazard;
import org.fraunhofer.plugins.hts.model.Subsystems;
import org.fraunhofer.plugins.hts.model.Verifications;
import org.fraunhofer.plugins.hts.view.model.HazardMinimal;
import org.fraunhofer.plugins.hts.view.model.HazardMinimalJSON;
import org.fraunhofer.plugins.hts.view.model.JIRAProject;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
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

@Transactional
public class HazardService {
	private final ActiveObjects ao;

	public HazardService(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	public Hazards add(String title, String hazardNum, Long projectID, Long issueID) {
		final Hazards hazard = ao.create(Hazards.class, new DBParam("PROJECT_ID", projectID), new DBParam("ISSUE_ID",
				issueID));
		hazard.setHazardTitle(title);
		hazard.setHazardNumber(hazardNum);
		hazard.setRevisionDate(new Date());
		hazard.setActive(true);
		hazard.save();
		return hazard;
	}

	@SuppressWarnings("unchecked")
	public Hazards update(int hazardID, String hazardNumber, String version, String hazardTitle,
			Subsystems[] subsystems, Review_Phases reviewPhase, Mission_Phase[] missionPhases,
			Hazard_Group[] hazardGroups, String safetyRequirements, String description, String justification,
			String openWork, Date initiation, Date completion) {
		Hazards hazard = getHazardByID(hazardID);

		PluginCustomization pluginCustomization = null;
		try {
			// Update issue in JIRA ITS
			pluginCustomization = PluginCustomization.getInstance();
			IssueManager issueManager = ComponentAccessor.getIssueManager();
			CustomField hazardNumberField = pluginCustomization.getHazardNumberField();
			hazardNumberField.getCustomFieldType().updateValue(hazardNumberField,
					issueManager.getIssueObject(hazard.getIssueID()), hazardNumber);
			CustomField hazardTitleField = pluginCustomization.getHazardTitleField();
			hazardTitleField.getCustomFieldType().updateValue(hazardTitleField,
					issueManager.getIssueObject(hazard.getIssueID()), hazardTitle);
		} catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Update hazard in HTS
		hazard.setHazardNumber(hazardNumber);
		hazard.setHazardVersionNumber(version);
		hazard.setHazardTitle(hazardTitle);
		hazard.setRevisionDate(new Date());

		ao.delete(ao.find(SubsystemToHazard.class, Query.select().where("HAZARD_ID=?", hazard.getID())));
		if (subsystems != null) {
			for (Subsystems subsystem : subsystems) {
				final SubsystemToHazard subsystemToHazard = ao.create(SubsystemToHazard.class);
				subsystemToHazard.setSubsystem(subsystem);
				subsystemToHazard.setHazard(hazard);
				subsystemToHazard.save();
			}
		}

		hazard.setReviewPhase(reviewPhase);

		ao.delete(ao.find(PhaseToHazard.class, Query.select().where("HAZARD_ID=?", hazard.getID())));
		if (missionPhases != null) {
			for (Mission_Phase phase : missionPhases) {
				final PhaseToHazard phaseToHazard = ao.create(PhaseToHazard.class);
				phaseToHazard.setMissionPhase(phase);
				phaseToHazard.setHazard(hazard);
				phaseToHazard.save();
			}
		}

		ao.delete(ao.find(GroupToHazard.class, Query.select().where("HAZARD_ID=?", hazard.getID())));
		if (hazardGroups != null) {
			for (Hazard_Group group : hazardGroups) {
				final GroupToHazard hazardGroupToHazard = ao.create(GroupToHazard.class);
				hazardGroupToHazard.setHazardGroup(group);
				hazardGroupToHazard.setHazard(hazard);
				hazardGroupToHazard.save();
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

	public Hazards update(Hazards hazard, String hazardTitle, String hazardNumber) {
		hazard.setHazardTitle(hazardTitle);
		hazard.setHazardNumber(hazardNumber);
		hazard.save();
		return hazard;
	}

	public List<HazardMinimal> getUserHazardsMinimal(List<JIRAProject> projects) {
		List<HazardMinimal> hazardsMinimal = new ArrayList<HazardMinimal>();
		for (JIRAProject project : projects) {
			for (Hazards hazard : getHazardsByMissionPayload(project.getID())) {
				Project jiraProject = getHazardProject(hazard);
				Issue jiraSubtask = getHazardSubTask(hazard);
				String baseURL = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");

				hazardsMinimal.add(new HazardMinimal(hazard.getID(), hazard.getHazardTitle(), hazard.getHazardNumber(),
						jiraSubtask.getSummary(), baseURL + "/browse/" + jiraProject.getKey() + "-"
								+ jiraSubtask.getNumber(), jiraProject.getName(), baseURL + "/browse/"
								+ jiraProject.getKey(), hazard.getRevisionDate().toString()));
			}
		}
		return hazardsMinimal;
	}

	public List<HazardMinimalJSON> getUserHazardsMinimalJson(ApplicationUser user) {
		List<Hazards> allHazards = newArrayList(ao.find(Hazards.class, Query.select().where("ACTIVE=?", true)));
		List<HazardMinimalJSON> allHazardsMinimal = new ArrayList<HazardMinimalJSON>();
		for (Hazards hazard : allHazards) {
			if (hasHazardPermission(hazard.getProjectID(), user)) {
				Project jiraProject = getHazardProject(hazard);
				Issue jiraSubtask = getHazardSubTask(hazard);
				String baseURL = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");

				allHazardsMinimal.add(new HazardMinimalJSON(hazard.getID(), hazard.getHazardTitle(), hazard
						.getHazardNumber(), jiraSubtask.getSummary(), baseURL + "/browse/" + jiraProject.getKey() + "-"
						+ jiraSubtask.getNumber(), jiraProject.getName(), baseURL + "/browse/" + jiraProject.getKey(),
						hazard.getRevisionDate().toString()));
			}
		}
		return allHazardsMinimal;
	}

	public List<Hazards> getAllHazardsByMissionID(Long missionID) {
		List<Hazards> hazards = newArrayList(ao.find(Hazards.class, Query.select().where("PROJECT_ID=?", missionID)));
		return hazards;
	}

	public List<HazardMinimalJSON> getAllHazardsByMissionIDMinimalJson(Long missionID) {
		List<Hazards> allHazards = getAllHazardsByMissionID(missionID);
		List<HazardMinimalJSON> allHazardsByIDMinimal = new ArrayList<HazardMinimalJSON>();
		for (Hazards hazard : allHazards) {
			Project jiraProject = getHazardProject(hazard);
			Issue jiraSubtask = getHazardSubTask(hazard);
			String baseURL = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");

			allHazardsByIDMinimal.add(new HazardMinimalJSON(hazard.getID(), hazard.getHazardTitle(), hazard
					.getHazardNumber(), jiraSubtask.getSummary(), baseURL + "/browse/" + jiraProject.getKey() + "-"
					+ jiraSubtask.getNumber(), jiraProject.getName(), baseURL + "/browse/" + jiraProject.getKey(),
					hazard.getRevisionDate().toString()));
		}
		return allHazardsByIDMinimal;
	}

	public Hazards getHazardByID(int hazardID) {
		final Hazards[] hazards = ao.find(Hazards.class, Query.select().where("ID=?", hazardID));
		return hazards.length > 0 ? hazards[0] : null;
	}

	public Hazards getHazardByID(String hazardID) {
		return getHazardByID(Integer.parseInt(hazardID));
	}

	public Hazards getHazardByIssueID(Long issueID) {
		final Hazards[] hazards = ao.find(Hazards.class, Query.select().where("ISSUE_ID=?", issueID));
		return hazards.length > 0 ? hazards[0] : null;
	}

	public List<Hazards> getHazardsByMissionPayload(Long id) {
		return newArrayList(ao.find(Hazards.class, Query.select().where("PROJECT_ID=? AND ACTIVE=?", id, true)));
	}

	public String getHazardPreparerInformation(Hazards hazard) {
		IssueManager issueManager = ComponentAccessor.getIssueManager();
		MutableIssue mutableIssue = issueManager.getIssueObject(hazard.getIssueID());
		String information = mutableIssue.getReporter().getDisplayName() + " ("
				+ mutableIssue.getReporter().getEmailAddress() + ")";
		return information;
	}

	public void deleteHazard(Hazards hazard, String reason) {
		// Mark hazard as inactive
		hazard.setActive(false);
		Date deleteDate = new Date();
		SimpleDateFormat deletedTimestampFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		hazard.setHazardNumber(hazard.getHazardNumber() + " (DELETED " + deletedTimestampFormat.format(deleteDate)
				+ ")");
		hazard.save();

		// Mark all non-deleted causes as deleted
		for (Hazard_Causes current : hazard.getHazardCauses()) {
			current.setDeleteReason(reason);
			current.save();
		}

		// Mark all non-deleted controls as deleted
		for (Hazard_Controls current : hazard.getHazardControls()) {
			current.setDeleteReason(reason);
			current.save();
		}

		// Mark all non-deleted verifications as delete
		for (Verifications current : hazard.getVerifications()) {
			current.setDeleteReason(reason);
			current.save();
		}
	}

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

	public Boolean hasHazardPermission(Long projectID, ApplicationUser user) {
		boolean hasPermission;
		ProjectManager projectManager = ComponentAccessor.getProjectManager();
		Project jiraProject = projectManager.getProjectObj(projectID);
		PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
		if (permissionManager.hasPermission(Permissions.CREATE_ISSUE, jiraProject, user)
				|| permissionManager.hasPermission(Permissions.EDIT_ISSUE, jiraProject, user)) {
			hasPermission = true;
		} else {
			hasPermission = false;
		}
		return hasPermission;
	}

	public Project getHazardProject(Hazards hazard) {
		return ComponentAccessor.getProjectManager().getProjectObj(hazard.getProjectID());
	}

	public Issue getHazardSubTask(Hazards hazard) {
		return ComponentAccessor.getIssueManager().getIssueObject(hazard.getIssueID());
	}
}
