package org.fraunhofer.plugins.hts.servlet;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fraunhofer.plugins.hts.db.ControlGroups;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.service.ControlGroupsService;
import org.fraunhofer.plugins.hts.db.service.HazardCauseService;
import org.fraunhofer.plugins.hts.db.service.HazardControlService;
import org.fraunhofer.plugins.hts.db.service.HazardService;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.extras.common.log.Logger.Log;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class ControlsServlet extends HttpServlet {
	private final Log logger = Logger.getInstance(ControlsServlet.class);

	private static final long serialVersionUID = 1L;
	private final TemplateRenderer templateRenderer;
	private final HazardService hazardService;
	private final HazardControlService hazardControlService;
	private final ControlGroupsService controlGroupsService;
	private final HazardCauseService hazardCauseService;

	public ControlsServlet(TemplateRenderer templateRenderer, HazardService hazardService,
			HazardControlService hazardControlService, ControlGroupsService controlGroupsService,
			HazardCauseService hazardCauseService) {
		this.templateRenderer = checkNotNull(templateRenderer);
		this.hazardService = checkNotNull(hazardService);
		this.hazardControlService = checkNotNull(hazardControlService);
		this.controlGroupsService = checkNotNull(controlGroupsService);
		this.hazardCauseService = checkNotNull(hazardCauseService);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO: Look into re-factoring permissions/generating error messages is
		// done - see issue on the Huboard.

		JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
		resp.setContentType("text/html;charset=utf-8");

		if (jiraAuthenticationContext.isLoggedInUser()) {
			Map<String, Object> context = Maps.newHashMap();
			boolean error = false;
			String errorMessage = null;
			List<String> errorList = new ArrayList<String>();

			boolean contains = req.getParameterMap().containsKey("id");
			Hazards hazard = null;
			if (contains == true) {
				String hazardIDStr = req.getParameter("id");
				try {
					int hazardID = Integer.parseInt(hazardIDStr);
					hazard = hazardService.getHazardByID(hazardID);
					if (hazard != null) {
						if (!hazardService.hasHazardPermission(hazard.getProjectID(),
								jiraAuthenticationContext.getUser())) {
							error = true;
							errorMessage = "Either this Hazard Report doesn't exist (it may have been deleted) or you ("
									+ jiraAuthenticationContext.getUser().getUsername()
									+ ") do not have permission to view/edit it.";
						}

					} else {
						error = true;
						errorMessage = "Either this Hazard Report doesn't exist (it may have been deleted) or you ("
								+ jiraAuthenticationContext.getUser().getUsername()
								+ ") do not have permission to view/edit it.";
					}
				} catch (NumberFormatException e) {
					error = true;
					errorMessage = "ID parameter in the URL is not a valid a number.";
				}
			} else {
				error = true;
				errorMessage = "Missing ID parameter in the URL. Valid URLs are of the following type:";
				errorList.add(".../hazards?id=[number]");
				errorList.add(".../causes?id=[number]");
				errorList.add(".../controls?id=[number]");
				errorList.add(".../verifications?id=[number]");
				errorList.add("where [number] is the unique identifier of the Hazard Report.");
			}

			// Decide which page to render for the user, error-page or
			// cause-page
			if (error == true) {
				context.put("errorMessage", errorMessage);
				context.put("errorList", errorList);
				templateRenderer.render("templates/error-page.vm", context, resp.getWriter());
			} else {
				context.put("hazard", hazard);
				context.put("controls", hazardControlService.getAllNonDeletedControlsWithinAHazard(hazard));
				context.put("transferredControls", hazardControlService.getAllTransferredControls(hazard));
				context.put("controlGroups", controlGroupsService.all());
				context.put("causes", hazardCauseService.getAllCausesWithinAHazard(hazard));
				context.put("allHazardsBelongingToMission",
						hazardService.getHazardsByMissionPayload(hazard.getProjectID()));
				templateRenderer.render("templates/control-page.vm", context, resp.getWriter());
			}
		} else {
			resp.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			boolean regular = Boolean.parseBoolean(req.getParameter("regular"));
			if (regular == true) {
				String description = req.getParameter("controlDescription");
				ControlGroups controlGroup;
				if (req.getParameter("controlGroup") != "") {
					controlGroup = controlGroupsService.getControlGroupByID(req.getParameter("controlGroup"));
				} else {
					controlGroup = null;
				}

				String[] causesStr = req.getParameterValues("controlCauses");
				Hazard_Causes[] causes = hazardCauseService.getHazardCausesByID(changeStringArray(causesStr));

				// Regular control (not a transfer)
				boolean existing = Boolean.parseBoolean(req.getParameter("existing"));
				if (existing == true) {
					// Regular control update
					String controlIDStr = req.getParameter("controlID");
					int controlID = Integer.parseInt(controlIDStr);
					hazardControlService.updateRegularControl(controlID, description, controlGroup, causes);
				} else {
					// Regular control creation
					String hazardIDStr = req.getParameter("hazardID");
					int hazardID = Integer.parseInt(hazardIDStr);
					hazardControlService.add(hazardID, description, controlGroup, causes);
				}
			} else {
				boolean existing = Boolean.parseBoolean(req.getParameter("existing"));
				if (existing == true) {
					// Control transfer update
					String controlIDStr = req.getParameter("controlID");
					int controlID = Integer.parseInt(controlIDStr);
					String transferReason = req.getParameter("transferReason");
					hazardControlService.updateTransferredControl(controlID, transferReason);
				} else {
					// Control transfer creation
					int targetCauseID = Integer.parseInt(req.getParameter("controlCauseList"));
					int targetControlID = 0;

					if (!Strings.isNullOrEmpty(req.getParameter("controlControlList"))) {
						targetControlID = Integer.parseInt(req.getParameter("controlControlList"));
					}

					int originHazardID = Integer.parseInt(req.getParameter("hazardID"));
					String transferReason = req.getParameter("transferReason");

					if (targetControlID == 0) {
						hazardControlService.addCauseTransfer(originHazardID, targetCauseID, transferReason);
					} else {
						hazardControlService.addControlTransfer(originHazardID, targetControlID, transferReason);
					}
				}
			}

			JSONObject jsonResponse = new JSONObject();
			createJson(jsonResponse, "updateSuccess", true);
			createJson(jsonResponse, "errorMessage", "none");
			res.setContentType("application/json");
			res.getWriter().println(jsonResponse);
		} else {
			res.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		logger.debug("Delete request for Control received");
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			int controlID = Integer.parseInt(req.getParameter("id"));
			String deleteReason = req.getParameter("reason");
			logger.info("Delete request for Control id: " + controlID + ", reason: " + deleteReason);
			Hazard_Controls control = hazardControlService.deleteControl(controlID, deleteReason);

			JSONObject jsonResponse = new JSONObject();
			if (control != null) {
				createJson(jsonResponse, "updateSuccess", true);
				createJson(jsonResponse, "errorMessage", "none");
				logger.info("Control id " + controlID + " deleted successfully.");
			} else {
				createJson(jsonResponse, "updateSuccess", false);
				createJson(jsonResponse, "errorMessage", "Could not find Control.");
				logger.warn("Control id " + controlID + " could not be deleted: could not find Control.");
			}
			res.setContentType("application/json");
			res.getWriter().println(jsonResponse);
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