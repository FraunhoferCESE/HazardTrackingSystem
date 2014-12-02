package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fraunhofer.plugins.hts.datatype.JIRAProject;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.MissionPayloadService;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;

public class JIRAProjectServiceImpl implements MissionPayloadService {
	private final HazardService hazardService;
	
	public JIRAProjectServiceImpl(HazardService hazardService) {
		this.hazardService = checkNotNull(hazardService);
	}

	@Override
	public List<JIRAProject> all() {
		List<Long> projectsWithHazards = hazardService.getProjectsWithHazards();
		ProjectManager projectManager = ComponentAccessor.getProjectManager();
		
		List<JIRAProject> jiraProjectList = new ArrayList<JIRAProject>();
		for (Long projectID : projectsWithHazards) {
			jiraProjectList.add(new JIRAProject(projectID, projectManager.getProjectObj(projectID).getName()));
		}
		return jiraProjectList;
	}
	
	@Override
	public List<JIRAProject> getUserProjects(ApplicationUser user) { 
		ProjectManager projectManager = ComponentAccessor.getProjectManager();
		PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
		Collection<Project> userProjectsColl1 = permissionManager.getProjects(Permissions.CREATE_ISSUE, user);
		Collection<Project> userProjectsColl2 = permissionManager.getProjects(Permissions.EDIT_ISSUE, user);
			
		Set<Project> userProjectsSet = new HashSet<Project>(userProjectsColl1);
		userProjectsSet.addAll(userProjectsColl2);
		
		// Create a new list that contains only the project ID and project name
		List<JIRAProject> jiraProjectList = new ArrayList<JIRAProject>();
		for (Project project : userProjectsSet) {
			jiraProjectList.add(new JIRAProject(project.getId(), projectManager.getProjectObj(project.getId()).getName()));
		}
		return jiraProjectList;
	}
}
