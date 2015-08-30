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
import org.fraunhofer.plugins.hts.service.HazardService;
import org.fraunhofer.plugins.hts.view.model.HazardMinimalJSON;
import org.fraunhofer.plugins.hts.view.model.JIRAProject;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
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
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			List<HazardMinimalJSON> hazards = hazardService.getAllHazardsByMissionIDMinimalJson(missionID);
			return Response.ok(hazards).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
					.build();
		}
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
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
					.build();
		}
	}
	
	
}
