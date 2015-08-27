package org.fraunhofer.plugins.hts.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fraunhofer.plugins.hts.service.ControlService;

import com.atlassian.jira.component.ComponentAccessor;

//String respStr = "{ \"success\" : \"true\" }";
@Path("/cause")
public class CauseRestService {
	private ControlService hazardControlService;

	public CauseRestService(ControlService hazardControlService) {
		this.hazardControlService = hazardControlService;
	}

	@GET
	@Path("control/{causeID}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllControlsBelongingToCause(@PathParam("causeID") int causeID, @QueryParam("includeTransfers") boolean includeTransfers) {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			return Response.ok(hazardControlService.getAllNonDeletedControlsWithinCauseMinimalJson(causeID, includeTransfers)).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
					.build();
		}
	}
}
