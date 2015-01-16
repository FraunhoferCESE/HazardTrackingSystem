package org.fraunhofer.plugins.hts.issues;

import java.util.List;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.extras.common.log.Logger.Log;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
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
	private Log logger = Logger.getInstance(PluginInstallation.class);
	
	private final FieldScreenManager fieldScreenManager;
	private final FieldScreenSchemeManager fieldScreenSchemeManager;
	private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
	private final ConstantsManager constantsManager;
	
	public PluginInstallation( FieldScreenManager fieldScreenManager, FieldScreenSchemeManager fieldScreenSchemeManager, 
			IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, ConstantsManager constantsManager) {
		this.fieldScreenManager = fieldScreenManager;
		this.fieldScreenSchemeManager = fieldScreenSchemeManager;
		this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
		this.constantsManager = constantsManager;
	}
	
	@Override
    public void onStart() {
		logger.info("========= HTS Plugin Installation ends =========");
		
		PluginCustomization pluginCustomization;
		try {
			pluginCustomization = PluginCustomization.getInstance();
			IssueType hazardIssueSubType = pluginCustomization.getHazardIssueSubType();
			
	        CustomField hazardNumberField = pluginCustomization.getHazardNumberField();
	        CustomField hazardTitleField = pluginCustomization.getHazardTitleField();
	        CustomField hazardURLField = pluginCustomization.getHazardURLField();
	        
			// Create screens
			// "Create" screen
	        FieldScreen htsCreateScreen = new FieldScreenImpl(fieldScreenManager);
	        htsCreateScreen.setName("HTS Create Screen");
	        htsCreateScreen.setDescription("This screen is specific to the Hazard Issue Type.");
	        htsCreateScreen.store();
	        // "Edit" screen
	        FieldScreen htsEditAndViewScreen = new FieldScreenImpl(fieldScreenManager);
	        htsEditAndViewScreen.setName("HTS Edit & View Screen");
	        htsEditAndViewScreen.setDescription("This screen is specific to the Hazard Issue Type.");
	        htsEditAndViewScreen.store();
			
			// Create tabs
			FieldScreenTab htsCreateScreenTab = htsCreateScreen.addTab("Tab 1");
			FieldScreenTab htsEditScreenTab = htsEditAndViewScreen.addTab("Tab 1");
			
			// Add fields to new tab
			// First add the "default" fields (from "Default Screen")
			FieldScreen defaultScreen = fieldScreenManager.getFieldScreen(FieldScreen.DEFAULT_SCREEN_ID);
			List<FieldScreenLayoutItem> defaultScreenListOfFields = defaultScreen.getTab(0).getFieldScreenLayoutItems();
			for (FieldScreenLayoutItem field : defaultScreenListOfFields) {
				if (field.getOrderableField().getName().equals("Due Date")) {
					// Adding fields to "Create" screen
					htsCreateScreenTab.addFieldScreenLayoutItem(hazardNumberField.getId());
					htsCreateScreenTab.addFieldScreenLayoutItem(hazardTitleField.getId());
					// Adding fields to "Edit" screen
					htsEditScreenTab.addFieldScreenLayoutItem(hazardNumberField.getId());
					htsEditScreenTab.addFieldScreenLayoutItem(hazardTitleField.getId());
					htsEditScreenTab.addFieldScreenLayoutItem(hazardURLField.getId());
				}
				htsCreateScreenTab.addFieldScreenLayoutItem(field.getOrderableField().getId());
				htsEditScreenTab.addFieldScreenLayoutItem(field.getOrderableField().getId());
			}
			// Add the new custom fields
			htsCreateScreenTab.store();
			htsEditScreenTab.store();
			
			// Create screen scheme
			FieldScreenScheme htsScreenScheme = new FieldScreenSchemeImpl(fieldScreenSchemeManager);
			htsScreenScheme.setName("HTS Screen Scheme");
			htsScreenScheme.setDescription("This screen scheme is specific to the Hazard Issue Type.");
			htsScreenScheme.store();

			// Add screen and operation to screen scheme
			// Create operation
			FieldScreenSchemeItem htsScreenSchemeCreateItem = new FieldScreenSchemeItemImpl(fieldScreenSchemeManager, fieldScreenManager);
			htsScreenSchemeCreateItem.setFieldScreen(htsCreateScreen);
			htsScreenSchemeCreateItem.setIssueOperation(IssueOperations.CREATE_ISSUE_OPERATION);
			htsScreenScheme.addFieldScreenSchemeItem(htsScreenSchemeCreateItem);
			// Edit operation
			FieldScreenSchemeItem htsScreenSchemeEditItem = new FieldScreenSchemeItemImpl(fieldScreenSchemeManager, fieldScreenManager);
			htsScreenSchemeEditItem.setFieldScreen(htsEditAndViewScreen);
			htsScreenSchemeEditItem.setIssueOperation(IssueOperations.EDIT_ISSUE_OPERATION);
			htsScreenScheme.addFieldScreenSchemeItem(htsScreenSchemeEditItem);
			// View operation
			FieldScreenSchemeItem htsScreenSchemeViewItem = new FieldScreenSchemeItemImpl(fieldScreenSchemeManager, fieldScreenManager);
			htsScreenSchemeViewItem.setFieldScreen(htsEditAndViewScreen);
			htsScreenSchemeViewItem.setIssueOperation(IssueOperations.VIEW_ISSUE_OPERATION);
			htsScreenScheme.addFieldScreenSchemeItem(htsScreenSchemeViewItem);
			
			// Get "Default Issue Type Screen Scheme" and configure it
			IssueTypeScreenScheme defaultIssueTypeScreenScheme = issueTypeScreenSchemeManager.getDefaultScheme();
			
			// Add "HTS Screen Scheme" to "Default Issue Type Screen Scheme"
			IssueTypeScreenSchemeEntity htsScreenSchemeEntity = 
					new IssueTypeScreenSchemeEntityImpl(issueTypeScreenSchemeManager, (GenericValue) null, fieldScreenSchemeManager, constantsManager);
			htsScreenSchemeEntity.setIssueTypeId(hazardIssueSubType.getId());
			htsScreenSchemeEntity.setFieldScreenScheme(htsScreenScheme);
			defaultIssueTypeScreenScheme.addEntity(htsScreenSchemeEntity);
		}
		catch (GenericEntityException e) {
			logger.error(e);
			// TODO Auto-generated catch block
		}

        logger.info("========= HTS Plugin Installation ends =========");
    }
}
