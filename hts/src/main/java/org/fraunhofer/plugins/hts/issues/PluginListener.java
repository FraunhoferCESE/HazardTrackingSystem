package org.fraunhofer.plugins.hts.issues;

import java.util.Collection;
import java.util.List;

import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.service.HazardService;
import org.ofbiz.core.entity.GenericEntityException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.extras.common.log.Logger.Log;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.ProjectDeletedEvent;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;

public class PluginListener implements InitializingBean, DisposableBean {

	private Log logger = Logger.getInstance(PluginListener.class); 
	
	private final EventPublisher eventPublisher;
	private final HazardService hazardService;

	public PluginListener(EventPublisher eventPublisher, HazardService hazardService) {
		this.eventPublisher = eventPublisher;
		this.hazardService = hazardService;
	}

	@SuppressWarnings("unchecked")
	@EventListener
	public void onIssueEvent(IssueEvent issueEvent) throws GenericEntityException {
		Long eventTypeId = issueEvent.getEventTypeId();
		Issue issue = issueEvent.getIssue();
		PluginCustomization pluginCustomization = PluginCustomization.getInstance();

		if (eventTypeId.equals(EventType.ISSUE_CREATED_ID)) {
			logger.debug("Issuee Created Event");
			if (issue.getIssueTypeObject().getName().equals("Hazard")) {
				Object hazardTitleObj = issue.getCustomFieldValue(pluginCustomization.getHazardTitleField());
				Object hazardNumberObj = issue.getCustomFieldValue(pluginCustomization.getHazardNumberField());
				String hazardTitle;
				String hazardNumber;

				if (hazardTitleObj != null) {
					hazardTitle = hazardTitleObj.toString();
				} else {
					hazardTitle = null;
					CustomField hazardTitleField = pluginCustomization.getHazardTitleField();
					hazardTitleField.getCustomFieldType().updateValue(hazardTitleField, issue, hazardTitle);
				}

				if (hazardNumberObj != null) {
					hazardNumber = hazardNumberObj.toString();
				} else {
					hazardNumber = null;
					CustomField hazardNumberField = pluginCustomization.getHazardNumberField();
					hazardNumberField.getCustomFieldType().updateValue(hazardNumberField, issue, hazardNumber);
				}
				// Create a new Hazard in the HTS representing the newly created
				// Sub-Task:
				Hazards hazard = hazardService.add(hazardTitle, hazardNumber, issue.getProjectId(), issue.getId());

				// Save the HTS URL in the Sub-Task:
				String baseURL = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");
				String htsCompleteURL = baseURL + "/plugins/servlet/hazards?id=" + hazard.getID();
				CustomField hazardURL = pluginCustomization.getHazardURLField();
				hazardURL.getCustomFieldType().updateValue(hazardURL, issue, htsCompleteURL);

			}
		} else if (eventTypeId.equals(EventType.ISSUE_UPDATED_ID)) {
			logger.debug("Issue Updated Event");
			if (issue.getIssueTypeObject().getName().equals("Hazard")) {
				Hazards hazard = hazardService.getHazardByIssueId(issue.getId());
				if (hazard != null) {
					String hazardTitle;
					String hazardNumber;

					Object hazardTitleObj = issue.getCustomFieldValue(pluginCustomization.getHazardTitleField());
					if (hazardTitleObj != null) {
						hazardTitle = hazardTitleObj.toString();
					} else {
						hazardTitle = null;
					}

					Object hazardNumberObj = issue.getCustomFieldValue(pluginCustomization.getHazardNumberField());
					if (hazardNumberObj != null) {
						hazardNumber = hazardNumberObj.toString();
					} else {
						hazardNumber = null;
					}
					hazardService.update(hazard, hazardTitle, hazardNumber);

					// Save the HTS URL in the Sub-Task:
					String baseURL = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");
					String htsCompleteURL = baseURL + "/plugins/servlet/hazards?id=" + hazard.getID();
					CustomField hazardURL = pluginCustomization.getHazardURLField();
					hazardURL.getCustomFieldType().updateValue(hazardURL, issue, htsCompleteURL);

				} else {
					Object hazardTitleObj = issue.getCustomFieldValue(pluginCustomization.getHazardTitleField());
					Object hazardNumberObj = issue.getCustomFieldValue(pluginCustomization.getHazardNumberField());
					String hazardTitle;
					String hazardNumber;

					if (hazardTitleObj != null) {
						hazardTitle = hazardTitleObj.toString();
					} else {
						hazardTitle = null;
						CustomField hazardTitleField = pluginCustomization.getHazardTitleField();
						hazardTitleField.getCustomFieldType().updateValue(hazardTitleField, issue, hazardTitle);
					}

					if (hazardNumberObj != null) {
						hazardNumber = hazardNumberObj.toString();
					} else {
						hazardNumber = null;
						CustomField hazardNumberField = pluginCustomization.getHazardNumberField();
						hazardNumberField.getCustomFieldType().updateValue(hazardNumberField, issue, hazardNumber);
					}
					// Create a new Hazard in the HTS representing the newly
					// created
					// Sub-Task:
					Hazards newHazard = hazardService.add(hazardTitle, hazardNumber, issue.getProjectId(),
							issue.getId());

					// Save the HTS URL in the Sub-Task:
					String baseURL = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");
					String htsCompleteURL = baseURL + "/plugins/servlet/hazards?id=" + newHazard.getID();
					CustomField hazardURL = pluginCustomization.getHazardURLField();
					hazardURL.getCustomFieldType().updateValue(hazardURL, issue, htsCompleteURL);
				}
			}
		} else if (eventTypeId.equals(EventType.ISSUE_DELETED_ID)) {
			logger.debug("Issue Deleted Event");
			// Two scenarios; 1) issue is issue, 2) issue is sub-task
			if (issue.isSubTask() == true) {
				// Sub-task scenario
				if (issue.getIssueTypeObject().getName().equals("Hazard")) {
					Hazards hazard = hazardService.getHazardByIssueId(issue.getId());
					if (hazard != null) {
						String reason = "Hazard with ID = " + hazard.getID() + "was deleted from JIRA by "
								+ issueEvent.getUser() + ". Date: " + issueEvent.getTime();
						hazardService.deleteHazard(hazard, reason);

					}
				}
			} else {
				// Issue scenario
				Collection<Issue> subtasks = issue.getSubTaskObjects();
				for (Issue subtask : subtasks) {
					if (subtask != null) {
						if (subtask.getIssueTypeObject().getName().equals("Hazard")) {
							Hazards hazard = hazardService.getHazardByIssueId(subtask.getId());
							if (hazard != null) {
								String reason = "Hazard with ID = " + hazard.getHazardTitle() + " was deleted by "
										+ issueEvent.getUser() + ". Date: " + issueEvent.getTime();
								hazardService.deleteHazard(hazard, reason);
							}
						}
					}
				}
				Hazards hazardByIssueID = hazardService.getHazardByIssueId(issue.getId());
				if (hazardByIssueID != null && hazardByIssueID.getActive()) {
					hazardService.deleteHazard(hazardByIssueID, "Hazard with ID = " + hazardByIssueID.getHazardTitle()
							+ " was deleted by " + issueEvent.getUser() + ". Date: " + issueEvent.getTime());
				}
			}
		} else if (eventTypeId.equals(EventType.ISSUE_MOVED_ID) && issue.isSubTask()
				&& issue.getIssueTypeObject().getName().equals("Hazard")) {
			logger.debug("Issue Moved Event");
			Hazards hazard = hazardService.getHazardByIssueId(issue.getId());
			hazard.setProjectID(issue.getProjectId());
			hazardService.update(hazard, hazard.getHazardTitle(), hazard.getHazardNumber());

		}
	}

	@EventListener
	public void onProjectEvent(ProjectDeletedEvent projectEvent) {
		logger.debug("Project Deleted Event");
		Long projectID = projectEvent.getProject().getId();
		List<Hazards> hazards = hazardService.getHazardsByProjectId(projectID);
		for (Hazards hazard : hazards) {
			String reason = "Project containing Hazard with ID = " + hazard.getID() + " was deleted by "
					+ projectEvent.getUser().getDisplayName() + ". Email: " + projectEvent.getUser().getEmailAddress();
			hazardService.deleteHazard(hazard, reason);
		}
	}

//	@EventListener
//	public void onHazardEvent(ProjectDeletedEvent projectEvent) {
//		Long projectID = projectEvent.getProject().getId();
//		List<Hazards> hazards = hazardService.getAllHazardsByMissionID(projectID);
//		for (Hazards hazard : hazards) {
//			String reason = "Hazard with ID = " + projectEvent.getId() + " was deleted by "
//					+ projectEvent.getUser().getDisplayName() + ". Email: " + projectEvent.getUser().getEmailAddress();
//			hazardService.deleteHazard(hazard, reason);
//		}
//	}

	@Override
	public void destroy() throws Exception {
		// Unregister ourselves with the EventPublisher
		eventPublisher.unregister(this);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// Register ourselves with the EventPublisher
		eventPublisher.register(this);
	}
}