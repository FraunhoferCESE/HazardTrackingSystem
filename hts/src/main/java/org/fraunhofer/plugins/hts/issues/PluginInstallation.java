package org.fraunhofer.plugins.hts.issues;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntityImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.sal.api.lifecycle.LifecycleAware;

public class PluginInstallation implements LifecycleAware {
	private final IssueTypeManager issueTypeManager;
	private final FieldManager fieldManager;
	private final CustomFieldManager customFieldManager;
	private final FieldScreenManager fieldScreenManager;
	private final FieldScreenSchemeManager fieldScreenSchemeManager;
	private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
	private final ConstantsManager constantsManager;
	
	public PluginInstallation(IssueTypeManager issueTypeManager, FieldManager fieldManager, 
			CustomFieldManager customFieldManager, FieldScreenManager fieldScreenManager, 
			FieldScreenSchemeManager fieldScreenSchemeManager, IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, 
			ConstantsManager constantsManager) {
		this.issueTypeManager = issueTypeManager;
		this.fieldManager = fieldManager;
		this.customFieldManager = customFieldManager;
		this.fieldScreenManager = fieldScreenManager;
		this.fieldScreenSchemeManager = fieldScreenSchemeManager;
		this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
		this.constantsManager = constantsManager;
	}
	
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
	        
			// Create screen
	        FieldScreen htsScreen = new FieldScreenImpl(fieldScreenManager);
			htsScreen.setName("HTS Screen");
			htsScreen.setDescription("This screen is specific to the Hazard Issue Type.");
			htsScreen.store();
			
			// Create tab
			FieldScreenTab htsScreenTab = htsScreen.addTab("Tab 1");
			
			// Add field to tab
			OrderableField orderableField = fieldManager.getOrderableField(cField.getId());
			htsScreenTab.addFieldScreenLayoutItem(orderableField.getId());
			htsScreenTab.store();
		
			// Create screen scheme
			FieldScreenScheme htsScreenScheme = new FieldScreenSchemeImpl(fieldScreenSchemeManager);
			htsScreenScheme.setName("HTS Screen Scheme");
			htsScreenScheme.setDescription("This screen scheme is specific to the Hazard Issue Type.");
			htsScreenScheme.store();

			// Add screen
			FieldScreenSchemeItem htsScreenSchemeItem = new FieldScreenSchemeItemImpl(fieldScreenSchemeManager, fieldScreenManager);
			htsScreenSchemeItem.setIssueOperation(IssueOperations.CREATE_ISSUE_OPERATION);
			htsScreenSchemeItem.setFieldScreen(htsScreen);
			htsScreenScheme.addFieldScreenSchemeItem(htsScreenSchemeItem);
			
			// Get "Default Issue Type Screen Scheme" and config that
			IssueTypeScreenScheme defaultIssueTypeScreenScheme = issueTypeScreenSchemeManager.getDefaultScheme();
			
			// Add "HTS Screen Scheme" to "Default Issue Type Screen Scheme"
			IssueTypeScreenSchemeEntity htsScreenSchemeEntity = 
					new IssueTypeScreenSchemeEntityImpl(issueTypeScreenSchemeManager, (GenericValue) null, fieldScreenSchemeManager, constantsManager);
			htsScreenSchemeEntity.setIssueTypeId(hazardIssueType.getId());
			htsScreenSchemeEntity.setFieldScreenScheme(htsScreenScheme);
			defaultIssueTypeScreenScheme.addEntity(htsScreenSchemeEntity);

		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        System.out.println("========= ON START ENDS =========");
    }
}
