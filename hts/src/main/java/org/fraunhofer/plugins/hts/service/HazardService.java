package org.fraunhofer.plugins.hts.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Strings;

@Transactional
public class HazardService {
	private final ActiveObjects ao;

	public HazardService(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	public Hazards add(String title, String hazardNum, Long projectID, Long issueID) {
		final Hazards hazard = ao.create(Hazards.class, new DBParam("PROJECT_ID", projectID),
				new DBParam("ISSUE_ID", issueID));
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
		Hazards hazard = getHazardById(hazardID);

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
			for (Hazards hazard : getHazardsByProjectId(project.getID())) {
				Project jiraProject = getHazardProject(hazard);
				Issue jiraSubtask = getHazardSubTask(hazard);
				String baseURL = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");

				hazardsMinimal.add(new HazardMinimal(hazard.getID(), hazard.getHazardTitle(), hazard.getHazardNumber(),
						jiraSubtask.getSummary(),
						baseURL + "/browse/" + jiraProject.getKey() + "-" + jiraSubtask.getNumber(),
						jiraProject.getName(), baseURL + "/browse/" + jiraProject.getKey(),
						hazard.getRevisionDate().toString()));
			}
		}
		return hazardsMinimal;
	}

	public List<Hazards> getHazardsByProjectId(long projectId) {
		return newArrayList(ao.find(Hazards.class, Query.select().where("PROJECT_ID=? AND ACTIVE=?", projectId, true)));
	}

	public List<HazardMinimalJSON> getAllHazardsByMissionIDMinimalJson(long missionID) {
		List<Hazards> allHazards = getHazardsByProjectId(missionID);
		List<HazardMinimalJSON> allHazardsByIDMinimal = new ArrayList<HazardMinimalJSON>();
		for (Hazards hazard : allHazards) {
			Project jiraProject = getHazardProject(hazard);
			Issue jiraSubtask = getHazardSubTask(hazard);
			String baseURL = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");

			allHazardsByIDMinimal.add(new HazardMinimalJSON(hazard.getID(), hazard.getHazardTitle(),
					hazard.getHazardNumber(), jiraSubtask.getSummary(),
					baseURL + "/browse/" + jiraProject.getKey() + "-" + jiraSubtask.getNumber(), jiraProject.getName(),
					baseURL + "/browse/" + jiraProject.getKey(), hazard.getRevisionDate().toString()));
		}
		return allHazardsByIDMinimal;
	}

	public Hazards getHazardById(int hazardId) {
		final Hazards[] hazards = ao.find(Hazards.class, Query.select().where("ID=?", hazardId));
		return hazards.length > 0 ? hazards[0] : null;
	}

	public Hazards getHazardById(String hazardId) {
		return getHazardById(Integer.parseInt(hazardId));
	}

	public Hazards getHazardByIssueId(long issueID) {
		final Hazards[] hazards = ao.find(Hazards.class, Query.select().where("ISSUE_ID=?", issueID));
		return hazards.length > 0 ? hazards[0] : null;
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
		String hazardNum;
		if (Strings.isNullOrEmpty(hazard.getHazardNumber()))
			hazardNum = "Hazard id=" + hazard.getID();
		else
			hazardNum = "Hazard " + hazard.getHazardNumber();

		hazard.setHazardNumber(hazardNum + " (DELETED " + deletedTimestampFormat.format(deleteDate) + ")");
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

	/**
	 * Returns a list of all active (non-deleted) hazards for which the user has
	 * permission to see as defined by
	 * {@link HazardService#hasHazardPermission(Long, ApplicationUser)}
	 * 
	 * @param user
	 *            the application user
	 * @return the list of active hazard objects the user has permission to see
	 */
	public List<Hazards> getUserHazards(ApplicationUser user) {
		Hazards[] hazards = ao.find(Hazards.class, Query.select().where("ACTIVE=?", true));

		List<Hazards> allHazards = newArrayList();
		for (Hazards hazard : hazards) {
			if (hasHazardPermission(hazard.getProjectID(), user)) {
				allHazards.add(hazard);
			}
		}

		return allHazards;
	}

	/**
	 * Returns JIRA projects that contain active (non-deleted) hazards for which
	 * the user has permission to see as defined by
	 * {@link HazardService#hasHazardPermission(Long, ApplicationUser)}
	 * 
	 * @param user
	 *            the application user
	 * @return a list of JIRAProject objects representing the JIRA projects the
	 *         user has access to
	 */
	public List<JIRAProject> getUserProjectsWithHazards(ApplicationUser user) {
		// This set operation would be unnecessary if an AO query could be
		// constructed that returns a distinct list of project ids, but AO
		// doesn't seem to be doing this even with the distinct() method.
		// Debugging it has yielded no information.
		Set<Long> uniqueProjects = new HashSet<Long>();
		for (Hazards hazard : getUserHazards(user)) {
			uniqueProjects.add(hazard.getProjectID());
		}

		ProjectManager projectManager = ComponentAccessor.getProjectManager();
		List<JIRAProject> jiraProjectList = newArrayList();
		for (Long projectId : uniqueProjects) {
			jiraProjectList.add(new JIRAProject(projectId, projectManager.getProjectObj(projectId).getName()));
		}
		return jiraProjectList;
	}

	/**
	 * Determine if a user has permission to access a specified JIRA project.
	 * Permission is defined as having {@link ProjectPermissions#CREATE_ISSUES}
	 * {@link ProjectPermission#EDIT_ISSUES}.
	 * 
	 * @param projectID
	 *            the JIRA project id to be checked
	 * @param user
	 *            the ApplicationUser object whose permission is checked.
	 * @return <code>true</code> if the user has create or edit permission on
	 *         the project, <code>false</code> otherwise.
	 */
	public boolean hasHazardPermission(long projectID, ApplicationUser user) {
		boolean hasPermission;
		ProjectManager projectManager = ComponentAccessor.getProjectManager();
		Project jiraProject = projectManager.getProjectObj(projectID);
		PermissionManager permissionManager = ComponentAccessor.getPermissionManager();

		if (permissionManager.hasPermission(ProjectPermissions.CREATE_ISSUES, jiraProject, user)
				|| permissionManager.hasPermission(ProjectPermissions.EDIT_ISSUES, jiraProject, user)) {
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
