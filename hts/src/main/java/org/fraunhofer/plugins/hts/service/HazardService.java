package org.fraunhofer.plugins.hts.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import net.java.ao.DBParam;
import net.java.ao.Query;

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

	public List<Hazards> getHazardsByProjectId(long projectId) {
		return newArrayList(ao.find(Hazards.class, Query.select().where("PROJECT_ID=? AND ACTIVE=?", projectId, true)));
	}

	public Hazards getHazardById(int hazardId) {
		final Hazards[] hazards = ao.find(Hazards.class, Query.select().where("ID=?", hazardId));
		return hazards.length > 0 ? hazards[0] : null;
	}

	/**
	 * The method renumbers causes, controls, and verifications within a hazard
	 * to make the numbers consecutive. This is useful prior to printing a
	 * report, or to clean up the numbering after much deleting of elements.
	 * 
	 * @param hazardId
	 *            the id of the hazard whose elements should be renumbered
	 */
	public void renumberHazardElements(int hazardId) {
		Hazards hazard = getHazardById(hazardId);
		if (hazard != null) {
			Hazard_Causes[] causes = hazard.getHazardCauses();
			if (causes != null && causes.length > 0) {
//				Arrays.sort(causes, new EntityIdComparator());
				int num = 1;
				for (Hazard_Causes cause : causes) {
					if (Strings.isNullOrEmpty(cause.getDeleteReason())) {
						cause.setCauseNumber(num++);
					} else {
						cause.setCauseNumber(-1);
					}
					cause.save();
				}
			}

			// TODO: Don't forget the orphans!
			Hazard_Controls[] controls = hazard.getHazardControls();
			if (controls != null && controls.length > 0) {
//				Arrays.sort(controls, new EntityIdComparator());
				int num = 1;
				for (Hazard_Controls control : controls) {
					if (Strings.isNullOrEmpty(control.getDeleteReason())) {
						control.setControlNumber(num++);
					} else {
						control.setControlNumber(-1);
					}
					control.save();
				}
			}

			Verifications[] verifications = hazard.getVerifications();
			if (verifications != null && verifications.length > 0) {
//				Arrays.sort(verifications, new EntityIdComparator());
				int num = 1;
				for(Verifications verification : verifications) {
					if(Strings.isNullOrEmpty(verification.getDeleteReason())) {
						verification.setVerificationNumber(num++);
					} else {
						verification.setVerificationNumber(-1);
					}
					verification.save();
				}
			}
		}
	}

	public Hazards getHazardById(String hazardId) {
		return getHazardById(Integer.parseInt(hazardId));
	}

	public Hazards getHazardByIssueId(long issueID) {
		final Hazards[] hazards = ao.find(Hazards.class, Query.select().where("ISSUE_ID=?", issueID));
		return hazards.length > 0 ? hazards[0] : null;
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
		Map<Long, Boolean> permissions = Maps.newHashMap();

		List<Hazards> allHazards = newArrayList();
		for (Hazards hazard : hazards) {
			Boolean hasPermission = permissions.get(hazard.getProjectID());
			if (hasPermission == null) {
				hasPermission = hasHazardPermission(hazard.getProjectID(), user);
				permissions.put(hazard.getProjectID(), hasPermission);
			}

			if (hasPermission) {
				allHazards.add(hazard);
			}
		}

		return allHazards;
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
		checkNotNull(user);
		boolean hasPermission = false;
		ProjectManager projectManager = ComponentAccessor.getProjectManager();
		Project jiraProject = projectManager.getProjectObj(projectID);
		if (jiraProject != null) {
			PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
			if (permissionManager.hasPermission(Permissions.CREATE_ISSUE, jiraProject, user)
					|| permissionManager.hasPermission(Permissions.EDIT_ISSUE, jiraProject, user)) {
				hasPermission = true;
			}

		}
		return hasPermission;
	}
}
