package org.fraunhofer.plugins.hts.issues;

import java.util.Collection;
import java.util.Objects;

import org.fraunhofer.plugins.hts.db.Mission_Payload;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.MissionPayloadService;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
 
public class PluginListener implements InitializingBean, DisposableBean {
	
	private final EventPublisher eventPublisher;
	private final MissionPayloadService missionPayloadService;
	private final HazardService hazardService;
	
	public PluginListener(EventPublisher eventPublisher, MissionPayloadService missionPayloadService, 
			HazardService hazardService) {
	    this.eventPublisher = eventPublisher;
	    this.missionPayloadService = missionPayloadService;
	    this.hazardService = hazardService;
	}
	
	@EventListener
	public void onIssueEvent(IssueEvent issueEvent) throws GenericEntityException {
	   Long eventTypeId = issueEvent.getEventTypeId();
	   Issue issue = issueEvent.getIssue();
	   PluginCustomization pluginCustomization = PluginCustomization.getInstance();
	   
	   if (eventTypeId.equals(EventType.ISSUE_CREATED_ID)) {
		   System.out.println("========= ISSUE CREATED =========");
		   if (issue.getIssueTypeObject().getName() == "Hazard") {
			   System.out.println("========= HAZARD =========");
			   Object hazardTitleObj = issue.getCustomFieldValue(pluginCustomization.getHazardTitleField());
			   Object hazardNumberObj = issue.getCustomFieldValue(pluginCustomization.getHazardNumberField());
			   if (hazardTitleObj != null && hazardNumberObj != null) {
				   // Check if JIRA project exists as HTS mission/payload
				   String jiraProjectIDStr = Objects.toString(issue.getProjectId(), "0");
				   Mission_Payload htsProject = missionPayloadService.getMissionPayloadByID(jiraProjectIDStr);
				   if (htsProject == null) {
					   // Mission does not exist, need to create it before creating the hazard
					   htsProject = missionPayloadService.add(issue.getProjectObject().getName());
				   }
				   // Create the hazard				   
				   hazardService.addFromJira(hazardTitleObj.toString(), hazardNumberObj.toString(), htsProject);
				   // Add HTS link to issue
				   Object hazardLinkObj = issue.getCustomFieldValue(pluginCustomization.getHazardURLField());
				   //issue.g
			   }
			   else {
				   // TODO: Hazard Title and/or Hazard Number = null, assign default value
				   // 
			   }
		   }
	   }
	   else if (eventTypeId.equals(EventType.ISSUE_RESOLVED_ID)) {
		   System.out.println("========= ISSUE RESOLVED =========");
	   }
	   else if (eventTypeId.equals(EventType.ISSUE_CLOSED_ID)) {
		   System.out.println("========= ISSUE CLOSED =========");
	   }
	}

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
