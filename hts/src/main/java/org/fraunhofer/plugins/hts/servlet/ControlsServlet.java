package org.fraunhofer.plugins.hts.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.common.base.Preconditions.checkNotNull;

import org.fraunhofer.plugins.hts.db.ControlGroups;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Payload;
import org.fraunhofer.plugins.hts.db.service.ControlGroupsService;
import org.fraunhofer.plugins.hts.db.service.HazardCauseService;
import org.fraunhofer.plugins.hts.db.service.HazardControlService;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.MissionPayloadService;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ControlsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final TemplateRenderer templateRenderer;
	private final HazardService hazardService;
	private final HazardControlService hazardControlService;
	private final ControlGroupsService controlGroupsService;
	private final HazardCauseService hazardCauseService;
	private final MissionPayloadService missionPayloadService;
	
	public ControlsServlet(TemplateRenderer templateRenderer, HazardService hazardService, 
			HazardControlService hazardControlService, ControlGroupsService controlGroupsService,
			HazardCauseService hazardCauseService, MissionPayloadService missionPayloadService) {
		this.templateRenderer = checkNotNull(templateRenderer);
		this.hazardService = checkNotNull(hazardService);
		this.hazardControlService = checkNotNull(hazardControlService);
		this.controlGroupsService = checkNotNull(controlGroupsService);
		this.hazardCauseService = checkNotNull(hazardCauseService);
		this.missionPayloadService = missionPayloadService;
	}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
    		Map<String, Object> context = Maps.newHashMap();
    		resp.setContentType("text/html;charset=utf-8");
    		context.put("baseUrl", ComponentAccessor.getApplicationProperties().getString("jira.baseurl"));

			Hazards currentHazard = hazardService.getHazardByID(req.getParameter("key"));
			Mission_Payload currentPayload = currentHazard.getMissionPayload();
			
			List<Hazards> allHazardsBelongingToPayload = missionPayloadService.getAllHazardsWithinMission(String.valueOf(currentPayload.getID()));
			context.put("allHazardsBelongingToPayload", allHazardsBelongingToPayload);
    		
    		//context.put("allHazards", hazardService.all());
    		
    		if ("y".equals(req.getParameter("edit"))) {
				context.put("hazardNumber", currentHazard.getHazardNum());
				context.put("hazardTitle", currentHazard.getTitle());
				context.put("hazardID", currentHazard.getID());
				context.put("hazard", currentHazard);
    			context.put("hazardControls", hazardControlService.getAllNonDeletedControlsWithinAHazard(currentHazard));
    			context.put("hazardCauses", hazardCauseService.getAllNonDeletedCausesWithinAHazard(currentHazard));
        		context.put("controlGroups", controlGroupsService.all());
        		context.put("controlTransfers", hazardControlService.getAllTransferredControls(currentHazard));
				templateRenderer.render("templates/EditHazard.vm", context, resp.getWriter());
    		}
//    		else {
//    			Hazards newestHazardReport = hazardService.getNewestHazardReport();
//    			// Content for upper part of page; hazard info and list of previously defined controls
//    			context.put("hazardNumber", newestHazardReport.getHazardNum());
//    			context.put("hazardTitle", newestHazardReport.getTitle());
//    			context.put("hazardID", newestHazardReport.getID());
//    			context.put("hazardControls", hazardControlService.getAllNonDeletedControlsWithinAHazard(newestHazardReport));
//    			// Content for lower part of page; creating a new control
//    			context.put("hazardCauses", hazardCauseService.getAllNonDeletedCausesWithinAHazard(newestHazardReport));
//        		context.put("controlGroups", controlGroupsService.all());
//        		context.put("controlTransfers", hazardControlService.getAllTransferredControls(newestHazardReport));
//            	templateRenderer.render("templates/HazardPage.vm", context, resp.getWriter());
//    		}
    	}
    	else {
    		resp.sendRedirect(req.getContextPath() + "/login.jsp");
    	}
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	if ("y".equals(req.getParameter("edit"))) {
    		// Process the editing request
    		final Hazards currentHazard = hazardService.getHazardByID(req.getParameter("hazardID"));
    		String controlID = req.getParameter("controlID");
    		final String description = req.getParameter("controlDescriptionEdit");
        	final ControlGroups controlGroup = controlGroupsService.getControlGroupServicebyID(req.getParameter("controlGroupEdit"));
        	final Hazard_Causes[] causes = hazardCauseService.getHazardCausesByID(changeStringArray(req.getParameterValues("controlCausesEdit")));
        	hazardControlService.update(controlID, description, controlGroup, causes);
        	res.sendRedirect(req.getContextPath() + "/plugins/servlet/controlform?edit=y&key=" + currentHazard.getID());
    	}
    	else if ("y".equals(req.getParameter("editTransfer"))) {
    		String controlID = req.getParameter("originID");
    		String transferReason = req.getParameter("controlTransferReasonEdit");
    		hazardControlService.updateTransferredControl(controlID, transferReason);
    		res.sendRedirect(req.getContextPath() + "/plugins/servlet/controlform");
    	}
    	else if ("y".equals(req.getParameter("transfer"))) {
    		final String hazardID = req.getParameter("hazardID");
    		final Hazards currentHazard = hazardService.getHazardByID(hazardID);
    		final String transferComment = req.getParameter("controlTransferReason");
    		final String causeID = req.getParameter("controlCauseList");   		
    		final String controlID = req.getParameter("controlControlList");
    		if (Strings.isNullOrEmpty(controlID)) {
    			Hazard_Causes targetCause = hazardCauseService.getHazardCauseByID(causeID);
    			if (!checkIfInternalCauseTransfer(currentHazard, targetCause)) {
        			hazardControlService.addCauseTransfer(transferComment, targetCause.getID(), currentHazard);
    			}
    		}
    		else {
    			Hazard_Controls targetControl = hazardControlService.getHazardControlByID(controlID);
    			if (!checkIfInternalControlTransfer(currentHazard, targetControl)) {
    				hazardControlService.addControlTransfer(transferComment, targetControl.getID(), currentHazard);
    			}
    		}
    		res.sendRedirect(req.getContextPath() + "/plugins/servlet/controlform?edit=y&key=" + currentHazard.getID());
    	}
    	else {
    		// Process the new control request
        	final Hazards currentHazard = hazardService.getHazardByID(req.getParameter("hazardID"));
        	final String description = req.getParameter("controlDescriptionNew");
        	final ControlGroups controlGroup = controlGroupsService.getControlGroupServicebyID(req.getParameter("controlGroupNew"));
        	final Hazard_Causes[] causes = hazardCauseService.getHazardCausesByID(changeStringArray(req.getParameterValues("controlCausesNew")));
        	hazardControlService.add(currentHazard, description, controlGroup, causes);
        	
        	res.sendRedirect(req.getContextPath() + "/plugins/servlet/controlform?edit=y&key=" + currentHazard.getID());
//        	JSONObject json = new JSONObject();
//        	createJson(json, "hazardID", currentHazard.getID());
//        	res.setContentType("application/json;charset=utf-8");
//        	res.getWriter().println(json);
    	}
    }
    
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			Hazard_Controls controlToBeDeleted = hazardControlService.getHazardControlByID(req.getParameter("controlID"));
			String reason = req.getParameter("reason");
			String respStr = "{ \"success\" : \"false\", error: \"Couldn't find hazard report\"}";
			
			if (controlToBeDeleted != null) {
				hazardControlService.deleteControl(controlToBeDeleted, reason);
				respStr = "{ \"success\" : \"true\" }";
			}

			res.setContentType("application/json;charset=utf-8");
			// Send the raw output string
			res.getWriter().write(respStr);
			
		} else {
			res.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}
    
	private Integer[] changeStringArray(String[] array) {
		if (array == null) {
			return null;
		} else {
			Integer[] intArray = new Integer[array.length];
			for (int i = 0; i < array.length; i++) {
				intArray[i] = Integer.parseInt(array[i]);
			}
			return intArray;
		}
	}
	
	private Boolean checkIfInternalCauseTransfer(Hazards hazard, Hazard_Causes targetCause) {
		List<Hazard_Causes> allCausesBelongingToHazard = hazardCauseService.getAllCausesWithinAHazard(hazard);
		for (Hazard_Causes cause : allCausesBelongingToHazard) {
			if (cause.getID() == targetCause.getID()) {
				return true;
			}
		}
		return false;
	}
	
	private Boolean checkIfInternalControlTransfer(Hazards hazard, Hazard_Controls targetControl) {
		List<Hazard_Controls> allControlsBelongingToHazard = hazardControlService.getAllControlsWithinAHazard(hazard);
		for (Hazard_Controls control : allControlsBelongingToHazard) {
			if (control.getID() == targetControl.getID()) {
				return true;
			}
		}
		return false;
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