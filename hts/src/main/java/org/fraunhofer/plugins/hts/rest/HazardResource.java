package org.fraunhofer.plugins.hts.rest;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Payload;
import org.fraunhofer.plugins.hts.db.Transfers;
import org.fraunhofer.plugins.hts.db.service.HazardCauseService;
import org.fraunhofer.plugins.hts.db.service.HazardControlService;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.MissionPayloadService;
import org.fraunhofer.plugins.hts.db.service.TransferService;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A resource of message.
 */
@Path("/report")
public class HazardResource {
	private final HazardService hazardService;
	private final MissionPayloadService missionPayloadService;
	private final HazardCauseService hazardCauseService;
	private final TransferService transferService;
	private final HazardControlService hazardControlService;

	public HazardResource(HazardService hazardService, MissionPayloadService missionPayloadService,
			HazardCauseService hazardCauseService, TransferService transferService, 
			HazardControlService hazardControlService) {
		this.hazardService = checkNotNull(hazardService);
		this.missionPayloadService = checkNotNull(missionPayloadService);
		this.hazardCauseService = checkNotNull(hazardCauseService);
		this.transferService = checkNotNull(transferService);
		this.hazardControlService = checkNotNull(hazardControlService);
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
	
//	----
	@GET
	@Path("hazardAssociations/{hazardID}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response checkHazardAssocation(@PathParam("hazardID") String hazardID) {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			JSONObject json = new JSONObject();
			
			Hazards hazard = hazardService.getHazardByID(hazardID);
			List<Transfers> transfers = transferService.all();
			for (Transfers transfer : transfers) {
				if (hazardID.equals(String.valueOf(transfer.getTargetID())) && transfer.getTargetType().equals("HAZARD")) {
					if (transfer.getActive()) {
						createJson(json, "hasAssociations", true);
						createJson(json, "hazard", hazard);
						String jsonStr = json.toString();
						return Response.ok(jsonStr, MediaType.APPLICATION_JSON).build();
					}
				}
			}

//			List<Hazard_Causes> causes = hazardCauseService.getAllNonDeletedCausesWithinAHazard(hazard);
			// need to do lookup based on transfer table
//			for (Transfers transfer : transfers) {
//				for (Hazard_Causes cause : causes) {
//					if () {
//						
//					}
//				}
//			}
			
			createJson(json, "hasAssociations", false);
			String jsonStr = json.toString();
			return Response.ok(jsonStr, MediaType.APPLICATION_JSON).build();
		}
		else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in")).build();
		}
	}
//	----

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
	@Path("allpayloads/{payloadID}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllHazardsWithinPayload(@PathParam("payloadID") String payloadID) {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			List<HazardResponseList> hazardList = new ArrayList<HazardResponseList>();
			for (Hazards hazard : missionPayloadService.getAllHazardsWithinMission(payloadID)) {
				hazardList.add(HazardResponseList.hazards(hazard));
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
		}
		else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in")).build();
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
		
	@GET
	@Path("cause/allcontrols/{causeID}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllControlsLinkedToCause(@PathParam("causeID") String causeID) {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			Hazard_Causes cause = hazardCauseService.getHazardCauseByID(causeID);
			List<HazardControlResponseList> controlList = new ArrayList<HazardControlResponseList>();
			for (Hazard_Controls control : hazardCauseService.getAllNonDeletedControlsWithinACause(cause)) {
				controlList.add(HazardControlResponseList.control(control));
			}
			return Response.ok(controlList).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
					.build();
		}
	}
	
	@GET
	@Path("transfers/{transferID}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getTargetProperty(@PathParam("transferID") String transferID) {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			int transferIDInt = Integer.parseInt(transferID);
			Transfers transfer = transferService.getTransferByID(transferIDInt);
			String transferType = transfer.getTargetType();
			if (transferType.equals("HAZARD")) {
				Hazards hazard = hazardService.getHazardByID(String.valueOf(transfer.getTargetID()));
				return Response.ok(HazardResponseList.hazards(hazard)).build();	
			}
			else if (transferType.equals("CAUSE")) {
				Hazard_Causes cause = hazardCauseService.getHazardCauseByID(String.valueOf(transfer.getTargetID()));
				return Response.ok(HazardCauseResponseList.causes(cause)).build();
			}
			else {
				Hazard_Controls control = hazardControlService.getHazardControlByID(String.valueOf(transfer.getTargetID()));
				return Response.ok(HazardControlResponseList.control(control)).build();
			}
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
					.build();
		}
	}
	
	private JSONObject createJson(JSONObject json, String key, Object value) {
		try {
			json.put(key, value);
		} catch (JSONException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return json;
	}
}