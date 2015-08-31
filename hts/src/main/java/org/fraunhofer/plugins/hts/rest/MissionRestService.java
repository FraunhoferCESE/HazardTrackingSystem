package org.fraunhofer.plugins.hts.rest;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.response.ResponseHelper;
import org.fraunhofer.plugins.hts.service.HazardService;
import org.fraunhofer.plugins.hts.view.model.HazardMinimal;
import org.fraunhofer.plugins.hts.view.model.JIRAProject;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

//String respStr = "{ \"success\" : \"true\" }";
@Path("/mission")
public class MissionRestService {
	private final HazardService hazardService;

	public MissionRestService(HazardService hazardService) {
		this.hazardService = hazardService;
	}

	@GET
	@Path("hazards/{missionID}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllHazardsByMissionID(@PathParam("missionID") Long missionID) {

		ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getUser();
		if (user == null) {
			return ResponseHelper.notLoggedIn();
		}

		if (missionID == null) {
			return ResponseHelper.badRequest("Invalid missionID");
		}

		if (!hazardService.hasHazardPermission(missionID, user)) {
			return ResponseHelper.forbidden("User does not have permission to access hazard reports for that project");
		}

		List<HazardMinimal> hazards = Lists.newArrayList();
		ProjectManager projectManager = ComponentAccessor.getProjectManager();
		IssueManager issueManager = ComponentAccessor.getIssueManager();
		for (Hazards hazard : hazardService.getHazardsByProjectId(missionID)) {
			hazards.add(HazardMinimal.create(hazard, projectManager.getProjectObj(hazard.getProjectID()),
					issueManager.getIssueObject(hazard.getIssueID())));
		}

		return Response.ok(hazards).build();

	}

	@GET
	@Path("/user")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getProjectsForUser() {
		JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
		if (jiraAuthenticationContext.isLoggedInUser()) {
			Map<Long, JIRAProject> userProjects = Maps.newHashMap();
			ProjectManager projectManager = ComponentAccessor.getProjectManager();

			for (Hazards hazard : hazardService.getUserHazards(jiraAuthenticationContext.getUser())) {
				if (userProjects.get(hazard.getProjectID()) == null) {
					Project project = projectManager.getProjectObj(hazard.getProjectID());
					userProjects.put(hazard.getProjectID(), JIRAProject.create(project));
				}
			}

			return Response.ok(Lists.newArrayList(userProjects.values())).build();
		} else {
			return ResponseHelper.notLoggedIn();
		}
	}
}
