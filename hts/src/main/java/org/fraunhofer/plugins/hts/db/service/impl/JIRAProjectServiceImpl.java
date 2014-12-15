package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

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
		List<Long> projectsWithHazards = hazardService.getProjectsWithHazards();
		ProjectManager projectManager = ComponentAccessor.getProjectManager();
		PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
		
		List<JIRAProject> jiraProjectList = new ArrayList<JIRAProject>();
		for (Long projectID : projectsWithHazards) {
			Project jiraProject = projectManager.getProjectObj(projectID);
			if (permissionManager.hasPermission(Permissions.CREATE_ISSUE, jiraProject, user) || 
				permissionManager.hasPermission(Permissions.EDIT_ISSUE, jiraProject, user)) {
					jiraProjectList.add(new JIRAProject(jiraProject.getId(), projectManager.getProjectObj(jiraProject.getId()).getName()));
			}
		}
		
		return jiraProjectList;
	}
}
