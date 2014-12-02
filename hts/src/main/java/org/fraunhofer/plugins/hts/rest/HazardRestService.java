package org.fraunhofer.plugins.hts.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fraunhofer.plugins.hts.datatype.HazardCauseDTMinimalJson;
import org.fraunhofer.plugins.hts.datatype.HazardDTMinimalJson;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.service.HazardCauseService;
import org.fraunhofer.plugins.hts.db.service.HazardService;

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
			List<HazardDTMinimalJson> hazards = hazardService.getUserHazardsMinimalJson(jiraAuthenticationContext.getUser());
			return Response.ok(hazards).build();
		}
		else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in")).build();
		}
	}
	
	@GET
	@Path("causes/{hazardID}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllCausesBelongingToHazard(@PathParam("hazardID") int hazardID) {
		JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext(); 
		if (jiraAuthenticationContext.isLoggedInUser()) {
			Hazards hazard = hazardService.getHazardByID(hazardID);
			List<HazardCauseDTMinimalJson> causes = new ArrayList<HazardCauseDTMinimalJson>();
			for (Hazard_Causes cause : hazardCauseService.getAllNonDeletedCausesWithinAHazard(hazard)) {
				causes.add(new HazardCauseDTMinimalJson(cause));
			}
			return Response.ok(causes).build();
		}
		else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in")).build();
		}
	}
	
}
