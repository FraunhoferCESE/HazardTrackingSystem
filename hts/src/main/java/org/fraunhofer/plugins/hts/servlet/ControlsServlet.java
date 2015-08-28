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

import org.fraunhofer.plugins.hts.model.ControlGroups;
import org.fraunhofer.plugins.hts.model.Hazard_Causes;
import org.fraunhofer.plugins.hts.model.Hazard_Controls;
import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.service.CauseService;
import org.fraunhofer.plugins.hts.service.ControlGroupsService;
import org.fraunhofer.plugins.hts.service.ControlService;
import org.fraunhofer.plugins.hts.service.HazardService;

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
	private final ControlService controlService;
	private final ControlGroupsService controlGroupsService;
	private final CauseService causeService;

	public ControlsServlet(TemplateRenderer templateRenderer, HazardService hazardService,
			ControlService hazardControlService, ControlGroupsService controlGroupsService,
			CauseService hazardCauseService) {
		this.templateRenderer = checkNotNull(templateRenderer);
		this.hazardService = checkNotNull(hazardService);
		this.controlService = checkNotNull(hazardControlService);
		this.controlGroupsService = checkNotNull(controlGroupsService);
		this.causeService = checkNotNull(hazardCauseService);
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

			String hazardId = req.getParameter("id");
			Hazards hazard = null;
			if (Strings.isNullOrEmpty(hazardId)) {
				error = true;
				errorMessage = "Missing ID parameter in the URL. Valid URLs are of the following type:";
				errorList.add(".../hazards?id=[number]");
				errorList.add(".../causes?id=[number]");
				errorList.add(".../controls?id=[number]");
				errorList.add(".../verifications?id=[number]");
				errorList.add("where [number] is the unique identifier of the Hazard Report.");
			} else {
				try {
					hazard = hazardService.getHazardById(hazardId);
					if (hazard == null || !hazardService.hasHazardPermission(hazard.getProjectID(),
							jiraAuthenticationContext.getUser())) {
						error = true;
						errorMessage = "Either this Hazard Report doesn't exist (it may have been deleted) or you ("
								+ jiraAuthenticationContext.getUser().getUsername()
								+ ") do not have permission to view/edit it.";
					} else {
						context.put("hazard", hazard);
						context.put("controls", controlService.getAllNonDeletedControlsWithinAHazard(hazard));
						context.put("transferredCauses", causeService.getAllTransferredCauses(hazard));
						context.put("transferredControls", controlService.getAllTransferredControls(hazard));
						context.put("orphanControls", controlService.getOrphanControls(hazard));
						context.put("controlGroups", controlGroupsService.all());
						context.put("causes", hazard.getHazardCauses());
						context.put("allHazardsBelongingToMission",
								hazardService.getHazardsByProjectId(hazard.getProjectID()));
					}
				} catch (NumberFormatException e) {
					error = true;
					errorMessage = "ID parameter in the URL is not a valid a number.";
				}
			}

			// Decide which page to render for the user, error-page or
			// cause-page
			if (error == true) {
				context.put("errorMessage", errorMessage);
				context.put("errorList", errorList);
				templateRenderer.render("templates/error-page.vm", context, resp.getWriter());
			} else {
				templateRenderer.render("templates/control-page.vm", context, resp.getWriter());
			}
		} else {
			resp.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			JSONObject jsonResponse = new JSONObject();
			String causeId = req.getParameter("controlCauseAssociation");
			Hazard_Causes associatedCause = null;
			if (!Strings.isNullOrEmpty(causeId))
				associatedCause = causeService
						.getHazardCauseByID(Integer.parseInt(req.getParameter("controlCauseAssociation")));

			boolean regular = Boolean.parseBoolean(req.getParameter("regular"));
			// Regular control (not a transfer)
			if (regular == true) {
				String description = req.getParameter("controlDescription");
				ControlGroups controlGroup;
				if (!req.getParameter("controlGroup").isEmpty()) {
					controlGroup = controlGroupsService.getControlGroupByID(req.getParameter("controlGroup"));
				} else {
					controlGroup = null;
				}

				boolean existing = Boolean.parseBoolean(req.getParameter("existing"));
				if (existing == true) {
					// Regular control update
					String controlIDStr = req.getParameter("controlID");
					int controlID = Integer.parseInt(controlIDStr);
					controlService.updateRegularControl(controlID, description, controlGroup, associatedCause);
				} else {
					// Regular control creation
					String hazardIDStr = req.getParameter("hazardID");
					int hazardID = Integer.parseInt(hazardIDStr);
					Hazard_Controls newControl = controlService.add(hazardID, description, controlGroup, associatedCause);
					createJson(jsonResponse, "newControlID", newControl.getID());
				}
			} else {
				boolean existing = Boolean.parseBoolean(req.getParameter("existing"));
				if (existing == true) {
					// Control transfer update
					String controlIDStr = req.getParameter("controlID");
					int controlID = Integer.parseInt(controlIDStr);
					String transferReason = req.getParameter("transferReason");
					controlService.updateTransferredControl(controlID, transferReason, associatedCause);
				} else {
					// Control transfer creation
					int targetCauseID = Integer.parseInt(req.getParameter("controlCauseList"));
					int targetControlID = 0;

					if (!Strings.isNullOrEmpty(req.getParameter("controlControlList"))) {
						targetControlID = Integer.parseInt(req.getParameter("controlControlList"));
					}

					int originHazardID = Integer.parseInt(req.getParameter("hazardID"));
					String transferReason = req.getParameter("transferReason");

					Hazard_Controls newControl;
					if (targetControlID == 0) {
						newControl = controlService.addCauseTransfer(originHazardID, targetCauseID, transferReason,
								associatedCause);
					} else {
						newControl = controlService.addControlTransfer(originHazardID, targetControlID, transferReason,
								associatedCause);
					}
					createJson(jsonResponse, "newControlID", newControl.getID());
				}
			}

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
			Hazard_Controls control = controlService.deleteControl(controlID, deleteReason);

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