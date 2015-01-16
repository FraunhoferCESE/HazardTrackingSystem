package org.fraunhofer.plugins.hts.issues;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.extras.common.log.Logger.Log;
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

	static final String ISSUE_TYPE_NAME = "Hazard";
	static final String HAZARD_NUMBER_FIELD_NAME = "Hazard Number";
	static final String HAZARD_TITLE_FIELD_NAME = "Hazard Title";
	static final String HAZARD_URL_FIELD_NAME = "Hazard URL";

	private final CustomField hazardNumberField;
	private final CustomField hazardTitleField;
	private final CustomField hazardURLField;
	private final IssueType hazardIssueSubType;

	private static PluginCustomization instance = null;

	private Log logger = Logger.getInstance(PluginCustomization.class);

	@SuppressWarnings("rawtypes")
	private PluginCustomization() throws GenericEntityException {

		CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();

		List<JiraContextNode> contexts = new ArrayList<JiraContextNode>();
		contexts.add(GlobalIssueContext.getInstance());

		// Check if this issue type already exists.
		IssueTypeManager issueTypeManager = ComponentAccessor.getComponent(IssueTypeManager.class);
		IssueType found = null;
		Iterator<IssueType> iterator = issueTypeManager.getIssueTypes().iterator();
		while (found == null && iterator.hasNext()) {
			IssueType next = iterator.next();
			if (next.getName().equals(ISSUE_TYPE_NAME)) {
				found = next;
				logger.info(ISSUE_TYPE_NAME + " already exists.");
			}
		}

		if (found == null) {
			logger.info(ISSUE_TYPE_NAME + " does not yet exist. Creating new issue type for HTS hazards.");
			this.hazardIssueSubType = issueTypeManager.createSubTaskIssueType(ISSUE_TYPE_NAME,
					"A Hazard sub-task issue type for the HTS plugin.",
					"/images/icons/issuetypes/subtask_alternate.png");

			List<GenericValue> issueTypes = new ArrayList<GenericValue>();
			issueTypes.add(hazardIssueSubType.getGenericValue());

			CustomFieldType textFieldType = customFieldManager
					.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:textfield");
			CustomFieldType urlFieldType = customFieldManager
					.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:url");
			CustomFieldSearcher fieldSearcher = customFieldManager
					.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:textsearcher");

			this.hazardNumberField = customFieldManager.createCustomField(HAZARD_NUMBER_FIELD_NAME, null,
					textFieldType, fieldSearcher, contexts, issueTypes);
			this.hazardTitleField = customFieldManager.createCustomField(HAZARD_TITLE_FIELD_NAME, null, textFieldType,
					fieldSearcher, contexts, issueTypes);
			this.hazardURLField = customFieldManager.createCustomField(HAZARD_URL_FIELD_NAME, null, urlFieldType,
					fieldSearcher, contexts, issueTypes);
		} else {
			this.hazardIssueSubType = found;
			this.hazardNumberField = customFieldManager.getCustomFieldObjectByName(HAZARD_NUMBER_FIELD_NAME);
			this.hazardTitleField = customFieldManager.getCustomFieldObjectByName(HAZARD_TITLE_FIELD_NAME);
			this.hazardURLField = customFieldManager.getCustomFieldObjectByName(HAZARD_URL_FIELD_NAME);
		}

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
