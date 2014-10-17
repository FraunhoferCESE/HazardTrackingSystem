package org.fraunhofer.plugins.hts.issues;

import java.util.ArrayList;
import java.util.List;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

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
import com.atlassian.sal.api.lifecycle.LifecycleAware;

public class PluginInstallation implements LifecycleAware {

	private final IssueTypeManager issueTypeManager;
	private final CustomFieldManager customFieldManager;
	private final FieldScreenManager fieldScreenManager;
	
	public PluginInstallation(IssueTypeManager issueTypeManager, CustomFieldManager customFieldManager, FieldScreenManager fieldScreenManager) {
		this.issueTypeManager = issueTypeManager;
		this.customFieldManager = customFieldManager;
		this.fieldScreenManager = fieldScreenManager;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
    public void onStart() {
		System.out.println("========= ON START BEGINS =========");
		
		// Create issue type:
    	IssueType hazardIssueType = this.issueTypeManager.createIssueType("Hazard", "A hazard issue type for the HTS plugin.", "/images/icons/issuetypes/genericissue.png");
		
    	// Create custom field:
    	// Create a list of issue types for which the custom field needs to be available  	
    	List<GenericValue> issueTypes = new ArrayList<GenericValue>();
    	issueTypes.add(hazardIssueType.getGenericValue());
    	
        // Create a list of project contexts for which the custom field needs to be available
        List<JiraContextNode> contexts = new ArrayList<JiraContextNode>();
        contexts.add(GlobalIssueContext.getInstance());
        
        CustomFieldType fieldType = this.customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:textfield");        
        CustomFieldSearcher fieldSearcher = this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:textsearcher");
        
        // Add custom field
        CustomField cField = null;
		try {
			cField = this.customFieldManager.createCustomField("Hazard Title", "The title of a Hazard.", fieldType, fieldSearcher, contexts, issueTypes);
			
			// Add field to default Screen
	        FieldScreen defaultScreen = fieldScreenManager.getFieldScreen(FieldScreen.DEFAULT_SCREEN_ID);
	        if (!defaultScreen.containsField(cField.getId())) {
	            FieldScreenTab firstTab = defaultScreen.getTab(0);
	            firstTab.addFieldScreenLayoutItem(cField.getId());
	        }
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
//		FieldScreen screen = new FieldScreenImpl(fieldScreenManager);
//		
//		
//		FieldScreen fs = null;
//		fieldScreenManager.createFieldScreen(fs);
//		fieldScreenManager.get
//		fieldScreenManager.c
        
        System.out.println("========= ON START ENDS =========");
    }
}
