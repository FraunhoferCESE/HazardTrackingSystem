
/*
 * TODO:
 * This is the original REST Service java file. It contained all the REST service functions.
 * What needs to be done:
 * 		Put in the correct java file
 * 			MissionRestService.java, HazardRestService.java, etc. 
 * 		Add permission checks to each
 * 		Re-factor based on updated methods in services and new datatypes in the datatype package.
 */



//package org.fraunhofer.plugins.hts.rest;
//
//import static com.google.common.base.Preconditions.checkNotNull;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.ws.rs.GET;
//import javax.ws.rs.Path;
//import javax.ws.rs.PathParam;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Response;
//
//import org.fraunhofer.plugins.hts.datatype.JIRAProject;
//import org.fraunhofer.plugins.hts.db.Hazard_Causes;
//import org.fraunhofer.plugins.hts.db.Hazard_Controls;
//import org.fraunhofer.plugins.hts.db.Hazards;
//import org.fraunhofer.plugins.hts.db.Transfers;
//import org.fraunhofer.plugins.hts.db.service.HazardCauseService;
//import org.fraunhofer.plugins.hts.db.service.HazardControlService;
//import org.fraunhofer.plugins.hts.db.service.HazardService;
//import org.fraunhofer.plugins.hts.db.service.MissionPayloadService;
//import org.fraunhofer.plugins.hts.db.service.TransferService;
//
//import com.atlassian.jira.component.ComponentAccessor;
//import com.atlassian.jira.util.json.JSONArray;
//import com.atlassian.jira.util.json.JSONException;
//import com.atlassian.jira.util.json.JSONObject;
//import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
//
///**
// * A resource of message.
// */
//@Path("/report")
//public class HazardResource {
//	private final HazardService hazardService;
//	private final MissionPayloadService missionPayloadService;
//	private final HazardCauseService hazardCauseService;
//	private final TransferService transferService;
//	private final HazardControlService hazardControlService;
//
//	public HazardResource(HazardService hazardService, MissionPayloadService missionPayloadService,
//			HazardCauseService hazardCauseService, TransferService transferService, 
//			HazardControlService hazardControlService) {
//		this.hazardService = checkNotNull(hazardService);
//		this.missionPayloadService = checkNotNull(missionPayloadService);
//		this.hazardCauseService = checkNotNull(hazardCauseService);
//		this.transferService = checkNotNull(transferService);
//		this.hazardControlService = checkNotNull(hazardControlService);
//	}
//
////	@GET
////	@Path("hazardnumber/{hazardNumber}")
////	@AnonymousAllowed
////	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
////	public Response checkHazardNum(@PathParam("hazardNumber") String hazardNumber) {
////		if (!hazardService.hazardNumberExists(hazardNumber)) {
////			return Response.ok(new HazardResourceModel("Hazard # is available")).build();
////		} else {
////			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
////					.entity(new HazardResourceModel("Hazard # exists")).build();
////		}
////
////	}
//
////	@GET
////	@Path("hazardlist/{payloadName}")
////	@AnonymousAllowed
////	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
////	public Response checkPayloadName(@PathParam("payloadName") String payloadName) {
////		if (!missionPayloadService.payloadNameExists(payloadName)) {
////			return Response.ok(new HazardResourceModel("Payload name is available")).build();
////		} else {
////			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
////					.entity(new HazardResourceModel("Payload name exists")).build();
////		}
////	}
//	
//	@GET
//	@Path("hazardAssociations/{hazardID}")
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	public Response checkHazardAssocation(@PathParam("hazardID") String hazardID) throws JSONException {
//		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
//			List<JSONObject> associatedHazards = new ArrayList<JSONObject>();
//			
//			Hazards hazard = hazardService.getHazardByID(hazardID);
//			List<Hazard_Causes> causes = hazardCauseService.getAllNonDeletedCausesWithinAHazard(hazard);
//			List<Hazard_Controls> controls = hazardControlService.getAllNonDeletedControlsWithinAHazard(hazard);
//			List<Transfers> transfers = transferService.all();
//			for (Transfers transfer : transfers) {
//				// Check hazard association
//				if (hazardID.equals(String.valueOf(transfer.getTargetID())) && transfer.getTargetType().equals("HAZARD")) {
//					Hazard_Causes originCause = hazardCauseService.getHazardCauseByID(String.valueOf(transfer.getOriginID()));
//					Hazards originHazard = originCause.getHazards()[0];
//					if (originHazard.getActive() == true) {
//						JSONObject hazardJsonObject = new JSONObject();
//						hazardJsonObject = createHazardJsonObject(originHazard, null, null, null, null);
//						if (!associatedHazardsArrayContains(associatedHazards, hazardJsonObject)) { 
//							associatedHazards.add(hazardJsonObject);
//						}
//					}
//				}
//				
//				JSONObject hazardJsonObject;
//				// Check hazard cause association
//				hazardJsonObject = checkForCauseTransferTargetMatch(causes, transfer);
//				if (hazardJsonObject.length() != 0) {
//					if (!associatedHazardsArrayContains(associatedHazards, hazardJsonObject)) { 
//						associatedHazards.add(hazardJsonObject);
//					}
//				}
//
//				// Check hazard control association
//				hazardJsonObject = checkForControlTransferTargetMatch(controls, transfer);
//				if (hazardJsonObject.length() != 0) {
//					if (!associatedHazardsArrayContains(associatedHazards, hazardJsonObject)) { 
//						associatedHazards.add(hazardJsonObject);
//					}
//				}
//			}
//
//			JSONArray jsonArray = new JSONArray(associatedHazards);
//			String jsonStr = jsonArray.toString();
//			return Response.ok(jsonStr, MediaType.APPLICATION_JSON).build();	
//		}
//		else {
//			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in")).build();
//		}
//	}
//	
//	private boolean associatedHazardsArrayContains(List<JSONObject> associatedHazards, JSONObject hazardJsonObject) throws JSONException {
//		boolean rtn = false;
//		for (JSONObject currentHazardJsonObject : associatedHazards) {
//			if (currentHazardJsonObject.get("hazardID") == hazardJsonObject.get("hazardID")) {
//				rtn = true;
//				break;
//			}
//		}
//		return rtn;
//	}
//	
//	@GET
//	@Path("causeAssociations/{causeIDs}")
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	public Response checkCauseAssocation(@PathParam("causeIDs") String causeIDs) throws JSONException {
//		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
//			List<Hazard_Causes> causes = new ArrayList<Hazard_Causes>();
//			String[] causeIDsList = causeIDs.split("\\s*,\\s*");
//			for (String id : causeIDsList) {
//				Hazard_Causes currentCause = hazardCauseService.getHazardCauseByID(id);
//				causes.add(currentCause);
//			}
//			
//			List<JSONObject> associatedCauses = new ArrayList<JSONObject>();
//			JSONObject causeJsonObject;
//			List<Transfers> transfers = transferService.all();
//			for (Transfers transfer : transfers) {
//				causeJsonObject = checkForCauseTransferTargetMatch(causes, transfer);
//				if (causeJsonObject.length() != 0) {
//					boolean contains = false;
//					for (JSONObject hazardJsonObject : associatedCauses) {
//						if (hazardJsonObject.get("targetCauseID") == causeJsonObject.get("targetCauseID")) {
//							if (causeJsonObject.get("transferType") == "CAUSE-CAUSE") {
//								JSONArray causeArrayToAddTo = hazardJsonObject.getJSONArray("originCauses");
//								JSONArray causeArrayToPullFrom = causeJsonObject.getJSONArray("originCauses");
//								JSONObject objectToAdd = causeArrayToPullFrom.getJSONObject(0);
//								causeArrayToAddTo.put(objectToAdd);
//							}
//							if (causeJsonObject.get("transferType") == "CONTROL-CAUSE") {
//								if (hazardJsonObject.get("transferType") == "BOTH") {
//									JSONArray controlArrayToAddTo = hazardJsonObject.getJSONArray("originControls");
//									JSONArray controlArrayToPullFrom = causeJsonObject.getJSONArray("originControls");
//									JSONObject objectToAdd = controlArrayToPullFrom.getJSONObject(0);
//									controlArrayToAddTo.put(objectToAdd);
//								}
//								else {
//									JSONArray controlsArrayToAdd = causeJsonObject.getJSONArray("originControls");
//									createJson(hazardJsonObject, "originControls", controlsArrayToAdd);
//									createJson(hazardJsonObject, "transferType", "BOTH");
//								}
//							}
//							contains = true;
//						}
//					}
//					
//					if (!contains) {
//						associatedCauses.add(causeJsonObject);
//					}
//				}
//			}
//
//			JSONArray jsonArray = new JSONArray(associatedCauses);
//			String jsonStr = jsonArray.toString();
//			return Response.ok(jsonStr, MediaType.APPLICATION_JSON).build();
//		}
//		else {
//			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in")).build();
//		}
//	}
//	
//	@GET
//	@Path("controlAssociations/{controlIDs}")
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	public Response checkControlAssocation(@PathParam("controlIDs") String controlIDs) throws JSONException {
//		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
//			List<Hazard_Controls> controls = new ArrayList<Hazard_Controls>();
//			String[] controlIDsList = controlIDs.split("\\s*,\\s*");
//			for (String id : controlIDsList) {
//				Hazard_Controls currentControls = hazardControlService.getHazardControlByID(id);
//				controls.add(currentControls);
//			}
//			
//			List<JSONObject> associatedControls = new ArrayList<JSONObject>();
//			JSONObject controlJsonObject;
//			List<Transfers> transfers = transferService.all();
//			for (Transfers transfer : transfers) {
//				controlJsonObject = checkForControlTransferTargetMatch(controls, transfer);
//				if (controlJsonObject.length() != 0) {
//					JSONArray controlInfoArrayToAddTo = null;
//					JSONArray controlInfoArrayToPullFrom = null;
//					JSONObject objectToAdd = null;
//					boolean contains = false;
//					for (JSONObject hazardJsonObject : associatedControls) {
//						if (hazardJsonObject.get("targetControlID") == controlJsonObject.get("targetControlID")) {
//							controlInfoArrayToAddTo = hazardJsonObject.getJSONArray("originControls");
//							controlInfoArrayToPullFrom = controlJsonObject.getJSONArray("originControls");
//							objectToAdd = controlInfoArrayToPullFrom.getJSONObject(0);
//							contains = true;
//						}
//					}
//					
//					if (contains == true) {
//						controlInfoArrayToAddTo.put(objectToAdd);
//					}
//					else {
//						associatedControls.add(controlJsonObject);
//					}
//				}
//			}
//
//			JSONArray jsonArray = new JSONArray(associatedControls);
//			String jsonStr = jsonArray.toString();
//			return Response.ok(jsonStr, MediaType.APPLICATION_JSON).build();
//		}
//		else {
//			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in")).build();
//		}
//	}
//
//	@GET
//	@Path("allhazards")
//	@Produces({ MediaType.APPLICATION_JSON })
//	public Response getAllHazardReports() {
//		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
//			List<HazardResponse> hazardList = new ArrayList<HazardResponse>();
//			for (Hazards hazard : hazardService.getAllHazards()) {
//				hazardList.add(HazardResponse.createHazardResponse(hazard));
//			}
//			return Response.ok(hazardList).build();
//		}
//		else {
//			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in")).build();
//		}
//	}
//	
//	@GET
//	@Path("allpayloads")
//	@Produces({ MediaType.APPLICATION_JSON })
//	public Response getAllMissionPayloads() {
//		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
//			List<MissionResponse> hazardList = new ArrayList<MissionResponse>();
//			for (JIRAProject payload : missionPayloadService.all()) {
//				hazardList.add(MissionResponse.createMissionReponse(payload));
//			}
//			return Response.ok(hazardList).build();
//		} else {
//			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
//					.build();
//		}
//	}
//	
//	@GET
//	@Path("allpayloads/{payloadID}")
//	@Produces({ MediaType.APPLICATION_JSON })
//	public Response getAllHazardsWithinPayload(@PathParam("payloadID") String payloadID) {
//		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
//			List<HazardResponse> hazardList = new ArrayList<HazardResponse>();
//			for (Hazards hazard : hazardService.getHazardsByMissionPayload(payloadID)) {
//				hazardList.add(HazardResponse.createHazardResponse(hazard));
//			}
//			return Response.ok(hazardList).build();
//		} else {
//			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
//					.build();
//		}
//	}
//
//	@GET
//	@Path("allcauses/{hazardID}")
//	@Produces({ MediaType.APPLICATION_JSON })
//	public Response getAllCausesLinkedToHazard(@PathParam("hazardID") String hazardID) {
//		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
//			Hazards hazard = hazardService.getHazardByID(hazardID);
//			List<HazardCauseResponseList> causeList = new ArrayList<HazardCauseResponseList>();
//			for (Hazard_Causes cause : hazardCauseService.getAllNonDeletedCausesWithinAHazard(hazard)) {
//				causeList.add(HazardCauseResponseList.causes(cause));
//			}
//			return Response.ok(causeList).build();
//		}
//		else {
//			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in")).build();
//		}
//	}
//
//	@GET
//	@Path("transfercause/{causeID}")
//	@Produces({ MediaType.APPLICATION_JSON })
//	public Response getTransferCauseInfo(@PathParam("causeID") String causeID) {
//		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
//			Hazard_Causes transferCause = hazardCauseService.getHazardCauseByID(causeID);
//			return Response.ok(HazardCauseResponseList.causes(transferCause)).build();
//		} else {
//			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
//					.build();
//		}
//	}
//		
//	@GET
//	@Path("cause/allcontrols/{causeID}")
//	@Produces({ MediaType.APPLICATION_JSON })
//	public Response getAllControlsLinkedToCause(@PathParam("causeID") String causeID) {
//		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
//			Hazard_Causes cause = hazardCauseService.getHazardCauseByID(causeID);
//			List<HazardControlResponseList> controlList = new ArrayList<HazardControlResponseList>();
//			for (Hazard_Controls control : hazardCauseService.getAllNonDeletedControlsWithinACause(cause)) {
//				controlList.add(HazardControlResponseList.control(control));
//			}
//			return Response.ok(controlList).build();
//		} else {
//			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
//					.build();
//		}
//	}
//	
//	@GET
//	@Path("transfers/{transferID}")
//	@Produces({ MediaType.APPLICATION_JSON })
//	public Response getTargetProperty(@PathParam("transferID") String transferID) {
//		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
//			int transferIDInt = Integer.parseInt(transferID);
//			Transfers transfer = transferService.getTransferByID(transferIDInt);
//			String transferType = transfer.getTargetType();
//			if (transferType.equals("HAZARD")) {
//				Hazards hazard = hazardService.getHazardByID(String.valueOf(transfer.getTargetID()));
//				return Response.ok(HazardResponse.createHazardResponse(hazard)).build();	
//			}
//			else if (transferType.equals("CAUSE")) {
//				Hazard_Causes cause = hazardCauseService.getHazardCauseByID(String.valueOf(transfer.getTargetID()));
//				return Response.ok(HazardCauseResponseList.causes(cause)).build();
//			}
//			else {
//				Hazard_Controls control = hazardControlService.getHazardControlByID(String.valueOf(transfer.getTargetID()));
//				return Response.ok(HazardControlResponseList.control(control)).build();
//			}
//		} else {
//			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
//					.build();
//		}
//	}
//	
//	private JSONObject createJson(JSONObject json, String key, Object value) {
//		try {
//			json.put(key, value);
//		} catch (JSONException e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}
//		return json;
//	}
//	
//	private JSONObject createHazardJsonObject(Hazards originHazard, Hazard_Causes targetCause, Hazard_Causes originCause,
//			Hazard_Controls targetControl, Hazard_Controls originControl) {
//		JSONObject hazardJsonObject = new JSONObject();
//		// Standard info
//		createJson(hazardJsonObject, "hazardNumber", originHazard.getHazardNumber());
//		createJson(hazardJsonObject, "hazardOwner", originHazard.getPreparer());
//		createJson(hazardJsonObject, "hazardID", originHazard.getID());
//		// Cause to cause transfer
//		if (targetCause != null && originCause != null) {
//			JSONArray jsonArray = new JSONArray();
//			JSONObject causeJsonObject = new JSONObject();
//			createJson(causeJsonObject, "originCauseID", originCause.getID());
//			createJson(causeJsonObject, "originCauseNumber", originCause.getCauseNumber());
//			jsonArray.put(causeJsonObject);
//			createJson(hazardJsonObject, "originCauses", jsonArray);
//			createJson(hazardJsonObject, "targetCauseID", targetCause.getID());
//			createJson(hazardJsonObject, "transferType", "CAUSE-CAUSE");
//		}
//		// Control to control transfer
//		if (targetControl != null && originControl != null) {
//			JSONArray jsonArray = new JSONArray();
//			JSONObject controlJsonObject = new JSONObject();
//			createJson(controlJsonObject, "originControlID", originControl.getID());
//			createJson(controlJsonObject, "originControlNumber", originControl.getControlNumber());
//			jsonArray.put(controlJsonObject);
//			createJson(hazardJsonObject, "originControls", jsonArray);
//			createJson(hazardJsonObject, "targetControlID", targetControl.getID());
//			createJson(hazardJsonObject, "transferType", "CONTROL-CONTROL");
//		}
//		// Control to cause transfer
//		if (targetCause != null && originControl != null) {
//			JSONArray jsonArray = new JSONArray();
//			JSONObject controlJsonObject = new JSONObject();
//			int foo = originControl.getID();
//			int troll = originControl.getControlNumber();
//			createJson(controlJsonObject, "originControlID", originControl.getID());
//			createJson(controlJsonObject, "originControlNumber", originControl.getControlNumber());
//			jsonArray.put(controlJsonObject);
//			createJson(hazardJsonObject, "originControls", jsonArray);
//			createJson(hazardJsonObject, "targetCauseID", targetCause.getID());
//			createJson(hazardJsonObject, "transferType", "CONTROL-CAUSE");
//		}
//		return hazardJsonObject;
//	}
//	
//	private JSONObject checkForCauseTransferTargetMatch(List<Hazard_Causes> causes, Transfers transfer) {
//		JSONObject hazardJsonObject = new JSONObject();
//		for (Hazard_Causes cause : causes) {
//			if (transfer.getTargetID() == cause.getID() && transfer.getTargetType().equals("CAUSE")) {
//				if (transfer.getOriginType().equals("CAUSE")) {
//					Hazard_Causes originCause = hazardCauseService.getHazardCauseByID(String.valueOf(transfer.getOriginID()));
//					Hazards originHazard = originCause.getHazards()[0];
//					hazardJsonObject = createHazardJsonObject(originHazard, cause, originCause, null, null);
//				}
//				if (transfer.getOriginType() == "CONTROL") {
//					Hazard_Controls originControl = hazardControlService.getHazardControlByID(String.valueOf(transfer.getOriginID()));
//					Hazards originHazard = originControl.getHazard()[0];
//					hazardJsonObject = createHazardJsonObject(originHazard, cause, null, null, originControl);
//				}
//			}
//		}
//		return hazardJsonObject;
//	}
//	
//	private JSONObject checkForControlTransferTargetMatch(List<Hazard_Controls> controls, Transfers transfer) {
//		JSONObject hazardJsonObject = new JSONObject();
//		for (Hazard_Controls control : controls) {
//			if (transfer.getTargetID() == control.getID() && transfer.getTargetType().equals("CONTROL")) {
//				Hazard_Controls originControl = hazardControlService.getHazardControlByID(String.valueOf(transfer.getOriginID()));
//				Hazards originHazard = originControl.getHazard()[0];
//				hazardJsonObject = createHazardJsonObject(originHazard, null, null, control, originControl); // change here later!
//			}
//		}
//		return hazardJsonObject;
//	}
//}