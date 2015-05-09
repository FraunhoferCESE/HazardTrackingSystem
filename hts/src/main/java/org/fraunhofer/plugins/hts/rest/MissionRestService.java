package org.fraunhofer.plugins.hts.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fraunhofer.plugins.hts.service.HazardService;
import org.fraunhofer.plugins.hts.service.JIRAProjectService;
import org.fraunhofer.plugins.hts.view.model.HazardMinimalJSON;
import org.fraunhofer.plugins.hts.view.model.JIRAProject;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;

//String respStr = "{ \"success\" : \"true\" }";
@Path("/mission")
public class MissionRestService {
	private final HazardService hazardService;
	private final JIRAProjectService missionService;

	public MissionRestService(HazardService hazardService, JIRAProjectService missionService) {
		this.hazardService = hazardService;
		this.missionService = missionService;
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
		System.err.println("isLoggedInUser: " + jiraAuthenticationContext.isLoggedInUser());
		if (jiraAuthenticationContext.isLoggedInUser()) {
			List<JIRAProject> userProjects = missionService.getUserProjects(jiraAuthenticationContext.getUser());
			System.err.println("userProjects: "+ userProjects + " "+userProjects.size());
			return Response.ok(missionService.getUserProjects(jiraAuthenticationContext.getUser())).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
					.build();
		}
	}
	
	
}
