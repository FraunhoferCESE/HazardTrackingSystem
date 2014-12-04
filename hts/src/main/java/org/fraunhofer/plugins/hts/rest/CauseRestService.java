package org.fraunhofer.plugins.hts.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fraunhofer.plugins.hts.datatype.HazardControlDTMinimalJson;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Transfers;
import org.fraunhofer.plugins.hts.db.service.HazardCauseService;
import org.fraunhofer.plugins.hts.db.service.HazardControlService;
import org.fraunhofer.plugins.hts.db.service.TransferService;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;

//String respStr = "{ \"success\" : \"true\" }";
@Path("/cause")
public class CauseRestService {
	private HazardCauseService hazardCauseService;
	private HazardControlService hazardControlService;
	private TransferService transferService;
	
	public CauseRestService(HazardControlService hazardControlService, HazardCauseService hazardCauseService, 
			TransferService transferService) {
		this.hazardControlService = hazardControlService;
		this.hazardCauseService = hazardCauseService;
		this.transferService = transferService;
	}
	
	@GET
	@Path("control/{causeID}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllControlsBelongingToCause(@PathParam("causeID") int causeID) {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			return Response.ok(hazardCauseService.getAllNonDeletedControlsWithinCauseMinimalJson(causeID)).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in")).build();
		}
	}
	
	
	// Rename the path to control/association/{controlIDs}
	@GET
	@Path("controlAssociations/{controlIDs}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response checkControlAssocation(@PathParam("controlIDs") String controlIDs) throws JSONException {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			List<Hazard_Controls> controls = new ArrayList<Hazard_Controls>();
			String[] controlIDsList = controlIDs.split("\\s*,\\s*");
			for (String id : controlIDsList) {
				Hazard_Controls currentControls = hazardControlService.getHazardControlByID(id);
				controls.add(currentControls);
			}
			
			List<JSONObject> associatedControls = new ArrayList<JSONObject>();
			JSONObject controlJsonObject;
			List<Transfers> transfers = transferService.all();
			for (Transfers transfer : transfers) {
				controlJsonObject = checkForControlTransferTargetMatch(controls, transfer);
				if (controlJsonObject.length() != 0) {
					JSONArray controlInfoArrayToAddTo = null;
					JSONArray controlInfoArrayToPullFrom = null;
					JSONObject objectToAdd = null;
					boolean contains = false;
					for (JSONObject hazardJsonObject : associatedControls) {
						if (hazardJsonObject.get("targetControlID") == controlJsonObject.get("targetControlID")) {
							controlInfoArrayToAddTo = hazardJsonObject.getJSONArray("originControls");
							controlInfoArrayToPullFrom = controlJsonObject.getJSONArray("originControls");
							objectToAdd = controlInfoArrayToPullFrom.getJSONObject(0);
							contains = true;
						}
					}
					
					if (contains == true) {
						controlInfoArrayToAddTo.put(objectToAdd);
					}
					else {
						associatedControls.add(controlJsonObject);
					}
				}
			}

			JSONArray jsonArray = new JSONArray(associatedControls);
			String jsonStr = jsonArray.toString();
			return Response.ok(jsonStr, MediaType.APPLICATION_JSON).build();
		}
		else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in")).build();
		}
	}
	
	private JSONObject checkForControlTransferTargetMatch(List<Hazard_Controls> controls, Transfers transfer) {
			JSONObject hazardJsonObject = new JSONObject();
		for (Hazard_Controls control : controls) {
			if (transfer.getTargetID() == control.getID() && transfer.getTargetType().equals("CONTROL")) {
				Hazard_Controls originControl = hazardControlService.getHazardControlByID(String.valueOf(transfer.getOriginID()));
				Hazards originHazard = originControl.getHazard()[0];
				hazardJsonObject = createHazardJsonObject(originHazard, null, null, control, originControl); // change here later!
			}
		}
		return hazardJsonObject;
	}
	
	private JSONObject createHazardJsonObject(Hazards originHazard, Hazard_Causes targetCause, Hazard_Causes originCause,
			Hazard_Controls targetControl, Hazard_Controls originControl) {
		JSONObject hazardJsonObject = new JSONObject();
		// Standard info
		createJson(hazardJsonObject, "hazardNumber", originHazard.getHazardNumber());
		createJson(hazardJsonObject, "hazardOwner", originHazard.getPreparer());
		createJson(hazardJsonObject, "hazardID", originHazard.getID());
		// Cause to cause transfer
		if (targetCause != null && originCause != null) {
			JSONArray jsonArray = new JSONArray();
			JSONObject causeJsonObject = new JSONObject();
			createJson(causeJsonObject, "originCauseID", originCause.getID());
			createJson(causeJsonObject, "originCauseNumber", originCause.getCauseNumber());
			jsonArray.put(causeJsonObject);
			createJson(hazardJsonObject, "originCauses", jsonArray);
			createJson(hazardJsonObject, "targetCauseID", targetCause.getID());
			createJson(hazardJsonObject, "transferType", "CAUSE-CAUSE");
		}
		// Control to control transfer
		if (targetControl != null && originControl != null) {
			JSONArray jsonArray = new JSONArray();
			JSONObject controlJsonObject = new JSONObject();
			createJson(controlJsonObject, "originControlID", originControl.getID());
			createJson(controlJsonObject, "originControlNumber", originControl.getControlNumber());
			jsonArray.put(controlJsonObject);
			createJson(hazardJsonObject, "originControls", jsonArray);
			createJson(hazardJsonObject, "targetControlID", targetControl.getID());
			createJson(hazardJsonObject, "transferType", "CONTROL-CONTROL");
		}
		// Control to cause transfer
		if (targetCause != null && originControl != null) {
			JSONArray jsonArray = new JSONArray();
			JSONObject controlJsonObject = new JSONObject();
			createJson(controlJsonObject, "originControlID", originControl.getID());
			createJson(controlJsonObject, "originControlNumber", originControl.getControlNumber());
			jsonArray.put(controlJsonObject);
			createJson(hazardJsonObject, "originControls", jsonArray);
			createJson(hazardJsonObject, "targetCauseID", targetCause.getID());
			createJson(hazardJsonObject, "transferType", "CONTROL-CAUSE");
		}
		return hazardJsonObject;
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
