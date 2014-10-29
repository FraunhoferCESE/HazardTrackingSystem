package org.fraunhofer.plugins.hts.issues;

import java.util.ArrayList;
import java.util.List;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;

public class PluginCustomization {
    private final CustomField hazardNumberField;
    private final CustomField hazardTitleField;
    private final CustomField hazardURLField;
    private final IssueType hazardIssueSubType;
    
    private static PluginCustomization instance = null;
    
    @SuppressWarnings("rawtypes")
	private PluginCustomization() throws GenericEntityException {
    	CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
    	
        List<JiraContextNode> contexts = new ArrayList<JiraContextNode>();
        contexts.add(GlobalIssueContext.getInstance());
        
        IssueTypeManager issueTypeManager = ComponentAccessor.getComponent(IssueTypeManager.class);
        // TODO: check if this issue type already exists
        this.hazardIssueSubType = issueTypeManager.createSubTaskIssueType("Hazard", "A Hazard sub-task issue type for the HTS plugin.", "/images/icons/issuetypes/subtask_alternate.png");
        
        List<GenericValue> issueTypes = new ArrayList<GenericValue>();
    	issueTypes.add(hazardIssueSubType.getGenericValue());
    	
    	CustomFieldType textFieldType = customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:textfield");
    	CustomFieldType urlFieldType = customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:url");
    	CustomFieldSearcher fieldSearcher = customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:textsearcher");
    	
		this.hazardNumberField = customFieldManager.createCustomField("Hazard Number", null, 
				textFieldType, fieldSearcher, contexts, issueTypes);
		this.hazardTitleField = customFieldManager.createCustomField("Hazard Title", null, 
				textFieldType, fieldSearcher, contexts, issueTypes);
		this.hazardURLField = customFieldManager.createCustomField("Hazard URL", null, 
				urlFieldType, fieldSearcher, contexts, issueTypes);
    }
    
    public static synchronized PluginCustomization getInstance() throws GenericEntityException {
    	if (instance == null) {
    		instance = new PluginCustomization();
    	}
    	return instance;
    }
    
    public CustomField getHazardNumberField() {
    	return this.hazardNumberField;
    }
    
    public CustomField getHazardTitleField() {
    	return this.hazardTitleField;
    }
    
    public CustomField getHazardURLField() {
    	return this.hazardURLField;
    }
    
    public IssueType getHazardIssueSubType() {
    	return this.hazardIssueSubType;
    }
	
}
