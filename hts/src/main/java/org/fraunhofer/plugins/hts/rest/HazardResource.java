package org.fraunhofer.plugins.hts.rest;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.MissionPayloadService;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A resource of message.
 */
@Path("/report")
public class HazardResource {
	private final HazardService hazardService;
	private final MissionPayloadService missionPayloadService;

	public HazardResource(HazardService hazardService, MissionPayloadService missionPayloadService) {
		this.hazardService = checkNotNull(hazardService);
		this.missionPayloadService = checkNotNull(missionPayloadService);
	}

	@GET
	@Path("hazardnumber/{hazardNumber}")
	@AnonymousAllowed
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response checkHazardNum(@PathParam("hazardNumber") String hazardNumber) {
		if (!hazardService.hazardNumberExists(hazardNumber)) {
			return Response.ok(new HazardResourceModel("Hazard # is available")).build();
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(new HazardResourceModel("Hazard # exists")).build();
		}

	}
	
	@GET
	@Path("hazardlist/{payloadName}")
	@AnonymousAllowed
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response checkPayloadName(@PathParam("payloadName") String payloadName) {
		if (!missionPayloadService.payloadNameExists(payloadName)) {
			return Response.ok(new HazardResourceModel("Payload name is available")).build();
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(new HazardResourceModel("Payload name exists")).build();
		}

	}
	
	@GET
	@Path("allhazards")
	@AnonymousAllowed
	@Produces({ MediaType.APPLICATION_JSON})
	public Response getAllHazardReports() {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			List<HazardResponseList> hazardList = new ArrayList<HazardResponseList>();
			for(Hazards hazard : hazardService.all()){
				hazardList.add(HazardResponseList.hazards(hazard));
			}
			return Response.ok(hazardList).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN)
					.entity(new HazardResourceModel("User is not logged in")).build();
		}

	}
}