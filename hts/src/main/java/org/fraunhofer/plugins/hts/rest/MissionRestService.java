package org.fraunhofer.plugins.hts.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fraunhofer.plugins.hts.datatype.HazardDTMinimalJson;
import org.fraunhofer.plugins.hts.db.service.HazardService;

import com.atlassian.jira.component.ComponentAccessor;

//String respStr = "{ \"success\" : \"true\" }";
@Path("/mission")
public class MissionRestService {
	private HazardService hazardService;

	public MissionRestService(HazardService hazardService) {
		this.hazardService = hazardService;
	}

	@GET
	@Path("hazards/{missionID}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllHazardsByMissionID(
			@PathParam("missionID") Long missionID) {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			List<HazardDTMinimalJson> hazards = hazardService
					.getAllHazardsByMissionIDMinimalJson(missionID);
			return Response.ok(hazards).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN)
					.entity(new HazardResourceModel("User is not logged in"))
					.build();
		}
	}

}
