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

import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Phase;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.Subsystems;
import org.fraunhofer.plugins.hts.db.Transfers;
import org.fraunhofer.plugins.hts.db.service.HazardCauseService;
import org.fraunhofer.plugins.hts.db.service.HazardControlService;
import org.fraunhofer.plugins.hts.db.service.HazardGroupService;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.MissionPhaseService;
import org.fraunhofer.plugins.hts.db.service.ReviewPhaseService;
import org.fraunhofer.plugins.hts.db.service.SubsystemService;
import org.fraunhofer.plugins.hts.db.service.TransferService;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public final class HazardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final HazardGroupService hazardGroupService;
	private final SubsystemService subsystemService;
	private final ReviewPhaseService reviewPhaseService;
	private final MissionPhaseService missionPhaseService;
	private final TemplateRenderer templateRenderer;
	private TransferService transferService;
	private HazardCauseService causeService;
	private HazardService hazardService;
	HazardCauseService hazardCauseService;

	public HazardServlet(HazardService hazardService, HazardGroupService hazardGroupService,
			SubsystemService subsystemService, ReviewPhaseService reviewPhaseService,
			MissionPhaseService missionPhaseService, TemplateRenderer templateRenderer,
			TransferService transferService, HazardControlService controlService, HazardCauseService causeService,
			HazardCauseService hazardCauseService) {
		this.hazardService = checkNotNull(hazardService);
		this.hazardGroupService = checkNotNull(hazardGroupService);
		this.subsystemService = checkNotNull(subsystemService);
		this.reviewPhaseService = checkNotNull(reviewPhaseService);
		this.missionPhaseService = checkNotNull(missionPhaseService);
		this.templateRenderer = checkNotNull(templateRenderer);
		this.transferService = checkNotNull(transferService);
		this.causeService = checkNotNull(causeService);
		this.hazardService = checkNotNull(hazardService);
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
			// hazard-page
			if (error == true) {
				context.put("errorMessage", errorMessage);
				context.put("errorList", errorList);
				templateRenderer.render("templates/error-page.vm", context, resp.getWriter());
			} else {
				Project jiraProject = hazardService.getHazardProject(hazard);
				Issue jiraSubtask = hazardService.getHazardSubTask(hazard);
				String baseURL = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");
				String jiraSubTaskSummary = jiraSubtask.getSummary();
				String jiraSubtaskURL = baseURL + "/browse/" + jiraProject.getKey() + "-" + jiraSubtask.getNumber();
				String jiraProjectName = jiraProject.getName();
				String jiraProjectURL = baseURL + "/browse/" + jiraProject.getKey();

				Hazard_Causes[] cause = hazard.getHazardCauses();
				List<TransferRiskValue> transferredToHazard = new ArrayList<TransferRiskValue>();
				List<TransferRiskValue> transferredToACause = new ArrayList<TransferRiskValue>();
				List<TransferRiskValue> transferIsDeletedList = new ArrayList<TransferRiskValue>();
				List<TransferRiskValue> transferMissingRiskValue = new ArrayList<TransferRiskValue>();

				for (int i = 0; i < hazard.getHazardCauses().length; i++) {

					if (cause[i].getTransfer() != 0) {
						TransferRiskValue transferResult = doGetTransfer(cause[i], cause[i].getTransfer());
						if (transferResult.isDeleted())
							transferIsDeletedList.add(transferResult);
						if (transferResult.getTransferTargetType().equals("HAZARD")) {
							transferredToHazard.add(transferResult);
						} else {
							if (transferResult.getRiskCategory() == null || transferResult.getRiskLikelihood() == null)
								transferMissingRiskValue.add(transferResult);
							else
								transferredToACause.add(transferResult);
						}
					}
				}

				context.put("hazard", hazard);
				context.put("jiraSubTaskSummary", jiraSubTaskSummary);
				context.put("jiraSubtaskURL", jiraSubtaskURL);
				context.put("jiraProjectName", jiraProjectName);
				context.put("jiraProjectURL", jiraProjectURL);
				context.put("nonAssociatedSubsystems", subsystemService.getRemaining(hazard.getSubsystems()));
				context.put("hazardPreparer", hazardService.getHazardPreparerInformation(hazard));
				context.put("reviewPhases", reviewPhaseService.all());
				context.put("nonAssociatedMissionPhases", missionPhaseService.getRemaining(hazard.getMissionPhases()));
				context.put("nonAssociatedHazardGroups", hazardGroupService.getRemaining(hazard.getHazardGroups()));
				context.put("initiationDate", removeTimeFromDate(hazard.getInitiationDate()));
				context.put("completionDate", removeTimeFromDate(hazard.getCompletionDate()));
				context.put("transferredToHazard", transferredToHazard);
				context.put("transferredToACause", transferredToACause);
				context.put("transferIsDeletedList", transferIsDeletedList);
				context.put("transferMissingRiskValue", transferMissingRiskValue);
				// context.put("cause", cause);
				templateRenderer.render("templates/hazard-page.vm", context, resp.getWriter());
			}
		} else {
			resp.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			String hazardIDStr = req.getParameter("hazardID");
			int hazardID = Integer.parseInt(hazardIDStr);
			String hazardNumber = req.getParameter("hazardNumber");
			String version = req.getParameter("hazardVersionNumber");
			String hazardTitle = req.getParameter("hazardTitle");
			Subsystems[] subsystems = subsystemService.getSubsystemsByID(changeStringArray(req
					.getParameterValues("hazardSubsystem")));
			Review_Phases reviewPhase = reviewPhaseService.getReviewPhaseByID(req.getParameter("hazardReviewPhase"));
			Mission_Phase[] missionPhases = missionPhaseService.getMissionPhasesByID(changeStringArray(req
					.getParameterValues("hazardPhase")));
			Hazard_Group[] hazardGroups = hazardGroupService.getHazardGroupsByID(changeStringArray(req
					.getParameterValues("hazardGroup")));
			String safetyRequirements = req.getParameter("hazardSafetyRequirements");
			String description = req.getParameter("hazardDescription");
			String justification = req.getParameter("hazardJustification");
			String openWork = req.getParameter("hazardOpenWork");
			Date initiation = changeToDate(req.getParameter("hazardInitation"));
			Date completion = changeToDate(req.getParameter("hazardCompletion"));

			hazardService.update(hazardID, hazardNumber, version, hazardTitle, subsystems, reviewPhase, missionPhases,
					hazardGroups, safetyRequirements, description, justification, openWork, initiation, completion);

			JSONObject json = new JSONObject();
			createJson(json, "updateSuccess", true);
			createJson(json, "errorMessage", "none");
			resp.setContentType("application/json");
			resp.getWriter().println(json);
		} else {
			resp.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}

	private TransferRiskValue doGetTransfer(Hazard_Causes originCause, int transferId) {
		Transfers transfer = transferService.getTransferByID(transferId);

		switch (transfer.getTargetType()) {
		case "CAUSE":
			Hazard_Causes targetCause = causeService.getHazardCauseByID(transfer.getTargetID());

			if (targetCause.getTransfer() == 0) {
				// Transfer target is a non-transferred cause.
				TransferRiskValue value = new TransferRiskValue(targetCause.getID(), "CAUSE",
						originCause.getCauseNumber(), originCause.getID());

				value.setRiskCategory(targetCause.getRiskCategory());
				value.setRiskLikelihood(targetCause.getRiskLikelihood());
				value.setDeleted(!Strings.isNullOrEmpty(targetCause.getDeleteReason()));

				return value;
			} else {
				// Recursively follow the transfer chain.
				return doGetTransfer(originCause, targetCause.getTransfer());
			}
		case "HAZARD":
			// The cause transfers to a hazard.
			Hazards targetHazard = hazardService.getHazardByID(transfer.getTargetID());

			TransferRiskValue value = new TransferRiskValue(targetHazard.getID(), "HAZARD",
					originCause.getCauseNumber(), originCause.getID());
			value.setDeleted(!targetHazard.getActive());

			return value;

		default:
			throw new IllegalArgumentException("Unhandled transfer target type. TransferId: " + transferId
					+ ", transferTargetType: " + transfer.getTargetType());
		}

	}

	private String removeTimeFromDate(Date date) {
		if (date != null) {
			return date.toString().substring(0, 10);
		} else {
			return null;
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

	private JSONObject createJson(JSONObject json, String key, Object value) {
		try {
			json.put(key, value);
		} catch (JSONException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return json;
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