package org.fraunhofer.plugins.hts.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import org.fraunhofer.plugins.hts.view.model.JIRAProject;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;

public class JIRAProjectService {
	private final HazardService hazardService;

	public JIRAProjectService(HazardService hazardService) {
		this.hazardService = checkNotNull(hazardService);
	}

	public List<JIRAProject> getUserProjects(ApplicationUser user) {
		List<Long> projectsWithHazards = hazardService.getProjectsWithHazards();
		ProjectManager projectManager = ComponentAccessor.getProjectManager();
		PermissionManager permissionManager = ComponentAccessor.getPermissionManager();

		List<JIRAProject> jiraProjectList = new ArrayList<JIRAProject>();
		for (Long projectID : projectsWithHazards) {
			Project jiraProject = projectManager.getProjectObj(projectID);
			if (permissionManager.hasPermission(Permissions.CREATE_ISSUE, jiraProject, user)
					|| permissionManager.hasPermission(Permissions.EDIT_ISSUE, jiraProject, user)) {
				jiraProjectList.add(new JIRAProject(jiraProject.getId(), projectManager.getProjectObj(
						jiraProject.getId()).getName()));
			}
		}

		return jiraProjectList;
	}
}
