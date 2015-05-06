package org.fraunhofer.plugins.hts.rest;

import static com.google.common.base.Preconditions.checkArgument;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fraunhofer.plugins.hts.db.service.HazardCauseService;
import org.fraunhofer.plugins.hts.db.service.HazardControlService;

import com.atlassian.jira.component.ComponentAccessor;

//String respStr = "{ \"success\" : \"true\" }";
@Path("/cause")
public class CauseRestService {
	private HazardControlService hazardControlService;
	private HazardCauseService hazardCauseService;

	public CauseRestService(HazardControlService hazardControlService, HazardCauseService hazardCauseService) {
		this.hazardControlService = hazardControlService;
		this.hazardCauseService = hazardCauseService;
	}

	@GET
	@Path("control/{causeID}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllControlsBelongingToCause(@PathParam("causeID") int causeID) {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			return Response.ok(hazardControlService.getAllNonDeletedControlsWithinCauseMinimalJson(causeID)).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
					.build();
		}
	}

	@GET
	@Path("{causeID}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getIncomingTransfersForElement(@PathParam("causeID") int id) {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			checkArgument(id > 0);

			hazardCauseService.getHazardCauseByID(id);
			// List<TransferJSON> values =
			// Lists.transform(transferService.getOriginsForId(type.toUpperCase(),
			// elementId),
			// new Function<Transfers, TransferJSON>() {
			// public TransferJSON apply(Transfers t) {
			// return new TransferJSON(t);
			// }
			// });

			return Response.ok().build();
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
					.build();
		}
	}

}
