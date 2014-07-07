package org.fraunhofer.plugins.hts.rest;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Payload;
import org.fraunhofer.plugins.hts.db.service.HazardCauseService;
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
	private final HazardCauseService hazardCauseService;

	public HazardResource(HazardService hazardService, MissionPayloadService missionPayloadService,
			HazardCauseService hazardCauseService) {
		this.hazardService = checkNotNull(hazardService);
		this.missionPayloadService = checkNotNull(missionPayloadService);
		this.hazardCauseService = checkNotNull(hazardCauseService);
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
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllHazardReports() {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			List<HazardResponseList> hazardList = new ArrayList<HazardResponseList>();
			for (Hazards hazard : hazardService.all()) {
				hazardList.add(HazardResponseList.hazards(hazard));
			}
			return Response.ok(hazardList).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
					.build();
		}

	}
	
	@GET
	@Path("allpayloads")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllMissionPayloads() {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			List<HazardMissionList> hazardList = new ArrayList<HazardMissionList>();
			for (Mission_Payload payload : missionPayloadService.all()) {
				hazardList.add(HazardMissionList.missionPayloads(payload));
			}
			return Response.ok(hazardList).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
					.build();
		}

	}

	@GET
	@Path("allcauses/{hazardID}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllCausesLinkedToHazard(@PathParam("hazardID") String hazardID) {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			Hazards hazard = hazardService.getHazardByID(hazardID);
			List<HazardCauseResponseList> causeList = new ArrayList<HazardCauseResponseList>();
			for (Hazard_Causes cause : hazardCauseService.getAllNonDeletedCausesWithinAHazard(hazard)) {
				causeList.add(HazardCauseResponseList.causes(cause));
			}
			return Response.ok(causeList).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
					.build();
		}

	}

	@GET
	@Path("transfercause/{causeID}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getTransferCauseInfo(@PathParam("causeID") String causeID) {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			Hazard_Causes transferCause = hazardCauseService.getHazardCauseByID(causeID);
			return Response.ok(HazardCauseResponseList.causes(transferCause)).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
					.build();
		}

	}
}