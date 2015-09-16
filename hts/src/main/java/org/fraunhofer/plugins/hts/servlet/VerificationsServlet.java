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

import org.fraunhofer.plugins.hts.model.Hazard_Controls;
import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.model.VerificationStatus;
import org.fraunhofer.plugins.hts.model.VerificationType;
import org.fraunhofer.plugins.hts.model.Verifications;
import org.fraunhofer.plugins.hts.service.CauseService;
import org.fraunhofer.plugins.hts.service.ControlService;
import org.fraunhofer.plugins.hts.service.HazardService;
import org.fraunhofer.plugins.hts.service.VerificationService;
import org.fraunhofer.plugins.hts.service.VerificationStatusService;
import org.fraunhofer.plugins.hts.service.VerificationTypeService;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.extras.common.log.Logger.Log;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class VerificationsServlet extends HttpServlet {

	private final Log logger = Logger.getInstance(VerificationsServlet.class);

	private static final long serialVersionUID = 1L;
	private final TemplateRenderer templateRenderer;
	private final HazardService hazardService;
	private final VerificationTypeService verificationTypeService;
	private final VerificationStatusService verificationStatusService;
	private final VerificationService verificationService;
	private final ControlService controlService;
	private final CauseService causeService;
	private final DateTimeFormatter dateTimeFormatter;

	public VerificationsServlet(TemplateRenderer templateRenderer, HazardService hazardService,
			VerificationTypeService verificationTypeService, VerificationStatusService verificationStatusService,
			VerificationService verificationService, ControlService hazardControlService,
			DateTimeFormatter dateTimeFormatter, CauseService causeService) {
		this.templateRenderer = checkNotNull(templateRenderer);
		this.hazardService = checkNotNull(hazardService);
		this.verificationTypeService = checkNotNull(verificationTypeService);
		this.verificationStatusService = checkNotNull(verificationStatusService);
		this.verificationService = checkNotNull(verificationService);
		this.controlService = checkNotNull(hazardControlService);
		this.dateTimeFormatter = dateTimeFormatter;
		this.causeService = causeService;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
		resp.setContentType("text/html;charset=utf-8");

		if (jiraAuthenticationContext.isLoggedInUser()) {
			Map<String, Object> context = Maps.newHashMap();
			context.put("dateFormatter", dateTimeFormatter.forLoggedInUser());

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
						context.put("causes", hazard.getHazardCauses());
						context.put("causesForPrinting", hazard.getHazardCauses());
						context.put("transferredCauses", causeService.getAllTransferredCauses(hazard));
						context.put("transferredControls", controlService.getAllTransferredControls(hazard));
						context.put("orphanControls", hazardService.getOrphanControls(hazard));

						int numVerifications = 0;
						Verifications[] verifications = hazard.getVerifications();
						List<Verifications> nonDeletedVerfications = Lists.newArrayList();
						if (verifications != null) {
							for (Verifications verification : hazard.getVerifications()) {
								if (Strings.isNullOrEmpty(verification.getDeleteReason())) {
									nonDeletedVerfications.add(verification);
									numVerifications++;
								}
							}
						}
						context.put("numVerifications", numVerifications);
						context.put("verifications", nonDeletedVerfications);

						context.put("transferredVerifications",
								verificationService.getAllTransferredVerifications(hazard));
						context.put("orphanVerifications", hazardService.getOrphanVerifications(hazard));
						context.put("statuses", verificationStatusService.all());
						context.put("types", verificationTypeService.all());
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
				templateRenderer.render("templates/verification-page.vm", context, resp.getWriter());
			}
		} else {
			resp.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			JSONObject jsonResponse = new JSONObject();

			Hazard_Controls associatedControl = null;
			String controlId = req.getParameter("verificationControlAssociation");
			if (!Strings.isNullOrEmpty(controlId))
				associatedControl = controlService.getHazardControlByID(Integer.parseInt(controlId));

			boolean isRegular = true;
			if (!Strings.isNullOrEmpty(req.getParameter("regular")))
				isRegular = Boolean.parseBoolean(req.getParameter("regular"));

			boolean existing = false;
			if (!Strings.isNullOrEmpty(req.getParameter("existing"))) {
				existing = Boolean.parseBoolean(req.getParameter("existing"));
			}

			if (isRegular) {
				String description = req.getParameter("verificationDescription");
				VerificationStatus status = null;
				if (!Strings.isNullOrEmpty(req.getParameter("verificationStatus"))) {
					status = verificationStatusService
							.getVerificationStatusByID(req.getParameter("verificationStatus"));
				}

				VerificationType type = null;
				if (!Strings.isNullOrEmpty(req.getParameter("verificationType"))) {
					type = verificationTypeService.getVerificationTypeByID(req.getParameter("verificationType"));
				}

				String responsibleParty = req.getParameter("verificationRespParty");

				Date estimatedCompletionDate = null;
				String date = req.getParameter("verificationEstComplDate");
				if (!Strings.isNullOrEmpty(date)) {
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					try {
						estimatedCompletionDate = format.parse(date);
					} catch (ParseException e) {
						logger.warn("Unparseable Estimated Completion Date received");
					}
				}

				if (!existing) {
					String hazardIDStr = req.getParameter("hazardID");
					int hazardID = Integer.parseInt(hazardIDStr);
					Verifications newVerification = verificationService.add(hazardID, description, status, type,
							responsibleParty, estimatedCompletionDate, associatedControl);
					createJson(jsonResponse, "newVerificationId", newVerification.getID());
				} else {
					String verificationIDStr = req.getParameter("verificationID");
					int verificationID = Integer.parseInt(verificationIDStr);
					verificationService.update(verificationID, description, status, type, responsibleParty,
							estimatedCompletionDate, associatedControl);
				}
			} else {
				String transferReason = req.getParameter("transferReason");

				if (!existing) {
					// transfer
					String transferTarget = req.getParameter("transferVerificationList");
					int targetID = Integer.parseInt(transferTarget);
					String hazardIDStr = req.getParameter("hazardID");
					int hazardID = Integer.parseInt(hazardIDStr);
					Verifications newTransferredVerification = verificationService.addVerificationTransfer(hazardID,
							targetID, transferReason, associatedControl);
					createJson(jsonResponse, "newVerificationID", newTransferredVerification.getID());
				} else {
					String verificationIDStr = req.getParameter("verificationID");
					int verificationID = Integer.parseInt(verificationIDStr);
					verificationService.updateTransferredVerification(verificationID, transferReason,
							associatedControl);
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

	private JSONObject createJson(JSONObject json, String key, Object value) {
		try {
			json.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}