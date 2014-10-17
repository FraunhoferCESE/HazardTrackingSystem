package org.fraunhofer.plugins.hts.issues;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
 
public class PluginListener implements InitializingBean, DisposableBean {
	
	private final EventPublisher eventPublisher;
	
	public PluginListener(EventPublisher eventPublisher) {
	    this.eventPublisher = eventPublisher;
	}
	
	@EventListener
	public void onIssueEvent(IssueEvent issueEvent) {
	   Long eventTypeId = issueEvent.getEventTypeId();
	   Issue issue = issueEvent.getIssue();
	 
	   if (eventTypeId.equals(EventType.ISSUE_CREATED_ID)) {
		   System.out.println("========= ISSUE CREATED =========");
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
