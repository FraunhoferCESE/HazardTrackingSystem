package org.fraunhofer.plugins.hts.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fraunhofer.plugins.hts.service.HazardCauseService;
import org.fraunhofer.plugins.hts.service.HazardService;
import org.fraunhofer.plugins.hts.view.model.HazardMinimalJSON;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;

//String respStr = "{ \"success\" : \"true\" }";
@Path("/hazard")
public class HazardRestService {
	private HazardService hazardService;
	private HazardCauseService hazardCauseService;
	
	public HazardRestService(HazardService hazardService, HazardCauseService hazardCauseService) {
		this.hazardService = hazardService;
		this.hazardCauseService = hazardCauseService;
	}
	
	@GET
	@Path("all")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllHazards() {
		JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
		if (jiraAuthenticationContext.isLoggedInUser()) {
			List<HazardMinimalJSON> hazards = hazardService.getUserHazardsMinimalJson(jiraAuthenticationContext.getUser());
			return Response.ok(hazards).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in")).build();
		}
	}
	
	@GET
	@Path("cause/{hazardID}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllCausesBelongingToHazard(@PathParam("hazardID") int hazardID) {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			return Response.ok(hazardCauseService.getAllNonDeletedCausesWithinHazardMinimalJson(hazardID)).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in")).build();
		}
	}
}
