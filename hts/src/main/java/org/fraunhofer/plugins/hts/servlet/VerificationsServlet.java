package org.fraunhofer.plugins.hts.servlet;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.VerificationStatus;
import org.fraunhofer.plugins.hts.db.VerificationType;
import org.fraunhofer.plugins.hts.db.Verifications;
import org.fraunhofer.plugins.hts.db.service.HazardControlService;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.VerificationService;
import org.fraunhofer.plugins.hts.db.service.VerificationStatusService;
import org.fraunhofer.plugins.hts.db.service.VerificationTypeService;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.extras.common.log.Logger.Log;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;

public class VerificationsServlet extends HttpServlet {

	private final Log logger = Logger.getInstance(VerificationsServlet.class);

	private static final long serialVersionUID = 1L;
	private final TemplateRenderer templateRenderer;
	private final HazardService hazardService;
	private final VerificationTypeService verificationTypeService;
	private final VerificationStatusService verificationStatusService;
	private final VerificationService verificationService;
	private final HazardControlService hazardControlService;

	public VerificationsServlet(TemplateRenderer templateRenderer, HazardService hazardService,
			VerificationTypeService verificationTypeService, VerificationStatusService verificationStatusService,
			VerificationService verificationService, HazardControlService hazardControlService) {
		this.templateRenderer = checkNotNull(templateRenderer);
		this.hazardService = checkNotNull(hazardService);
		this.verificationTypeService = checkNotNull(verificationTypeService);
		this.verificationStatusService = checkNotNull(verificationStatusService);
		this.verificationService = checkNotNull(verificationService);
		this.hazardControlService = checkNotNull(hazardControlService);
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
				// Parsing from String to Integer could fail
				try {
					int hazardID = Integer.parseInt(hazardIDStr);
					hazard = hazardService.getHazardByID(hazardID);
					if (hazard != null) {
						// Check user permission
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
				context.put("verifications", verificationService.getAllNonDeletedVerificationsWithinAHazard(hazard));
				context.put("statuses", verificationStatusService.all());
				context.put("types", verificationTypeService.all());
				context.put("controls", hazardControlService.getAllControlsWithinAHazard(hazard));
				context.put("transferredControls", hazardControlService.getAllTransferredControls(hazard));
				templateRenderer.render("templates/verification-page.vm", context, resp.getWriter());
			}
		} else {
			resp.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			String hazardIDStr = req.getParameter("hazardID");
			int hazardID = Integer.parseInt(hazardIDStr);

			String description = req.getParameter("verificationDescription");
			VerificationStatus status;
			if (!req.getParameter("verificationStatus").isEmpty()) {
				status = verificationStatusService.getVerificationStatusByID(req.getParameter("verificationStatus"));
			} else {
				status = null;
			}

			VerificationType type;
			if (!req.getParameter("verificationType").isEmpty()) {
				type = verificationTypeService.getVerificationTypeByID(req.getParameter("verificationType"));
			} else {
				type = null;
			}

			String responsibleParty = req.getParameter("verificationRespParty");
			Date estimatedCompletionDate = changeToDate(req.getParameter("verificationEstComplDate"));

			String[] controlsStr = req.getParameterValues("verificationControls");
			Hazard_Controls[] controls = hazardControlService.getHazardControlsByID(changeStringArray(controlsStr));

			boolean existing = Boolean.parseBoolean(req.getParameter("existing"));
			if (existing == true) {
				String verificationIDStr = req.getParameter("verificationID");
				int verificationID = Integer.parseInt(verificationIDStr);
				verificationService.update(verificationID, description, status, type, responsibleParty,
						estimatedCompletionDate, controls);
			} else {
				verificationService.add(hazardID, description, status, type, responsibleParty, estimatedCompletionDate,
						controls);
			}
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		logger.info("Verification delete requested.");
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			String verificationID = checkNotNull(req.getParameter("id"));
			String reason = checkNotNull(req.getParameter("reason"));
			logger.debug("Verification ID requested for deletion: " + verificationID + ", reason: " + reason);

			Verifications verificationToBeDeleted = verificationService.getVerificationByID(verificationID);
			JSONObject jsonResponse = new JSONObject();
			try {
				if (verificationToBeDeleted != null) {
					verificationService.deleteVerification(verificationToBeDeleted, reason);
					jsonResponse.put("updateSuccess", true);
					jsonResponse.put("errorMessage", "none");
					logger.info("Verification delete completed successfully.");
				} else {
					jsonResponse.put("updateSuccess", false);
					jsonResponse.put("errorMessage", "Could not find Verification to delete.");
					logger.warn("Verification ID " + verificationID
							+ " could not be deleted. Could not find Verification.");
				}
			} catch (JSONException je) {
				logger.error("Could not create JSON response following verification delete.");
				throw new IOException(je);
			}

			res.setContentType("application/json;charset=utf-8");
			res.getWriter().println(jsonResponse);

		} else {
			res.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}

	private Date changeToDate(String date) {
		if (date != null && !date.isEmpty()) {
			SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yyyy");
			try {
				String reformatted = newFormat.format(oldFormat.parse(date));
				Date converted = newFormat.parse(reformatted);
				return converted;
			} catch (ParseException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return null;
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

}