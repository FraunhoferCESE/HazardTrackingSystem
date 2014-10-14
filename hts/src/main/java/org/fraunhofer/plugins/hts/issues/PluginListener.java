package org.fraunhofer.plugins.hts.issues;

import java.util.ArrayList;
import java.util.List;

import org.ofbiz.core.entity.GenericValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.issuetype.IssueType;

public class PluginListener implements InitializingBean, DisposableBean {

	private final IssueTypeManager issueTypeManager;
	private final CustomFieldManager customFieldManager;
	private final FieldScreenManager fieldScreenManager;
	
	public PluginListener(IssueTypeManager issueTypeManager, CustomFieldManager customFieldManager, FieldScreenManager fieldScreenManager) {
		this.issueTypeManager = issueTypeManager;
		this.customFieldManager = customFieldManager;
		this.fieldScreenManager = fieldScreenManager;
	}
	
    @Override
    public void destroy() throws Exception {
        // Handle plugin disabling or un-installation here
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Handle plugin enabling or installation here    	
    	
    	// Create issue type:
    	IssueType issueType = this.issueTypeManager.createIssueType("TheType", "TheDescription", "/images/icons/issuetypes/genericissue.png");
    	
    	// Create custom field:
    	// Create a list of issue types for which the custom field needs to be available  	
    	List<GenericValue> issueTypes = new ArrayList<GenericValue>();
    	issueTypes.add(null);
    	
        // Create a list of project contexts for which the custom field needs to be available
        List<JiraContextNode> contexts = new ArrayList<JiraContextNode>();
        contexts.add(GlobalIssueContext.getInstance());
        
        CustomFieldType fieldType = this.customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:textfield");        
        CustomFieldSearcher fieldSearcher = this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:textsearcher");
        
        // Add custom field
        final CustomField cField = this.customFieldManager.createCustomField("FOO", "BAR", fieldType, fieldSearcher, contexts, issueTypes);
        
        // Add field to default Screen
        FieldScreen defaultScreen = fieldScreenManager.getFieldScreen(FieldScreen.DEFAULT_SCREEN_ID);
        if (!defaultScreen.containsField(cField.getId())) {
            FieldScreenTab firstTab = defaultScreen.getTab(0);
            firstTab.addFieldScreenLayoutItem(cField.getId());
        }
    }
}

