package org.fraunhofer.plugins.hts.servlet;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.google.common.collect.Maps;

public final class HazardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final HazardGroupService hazardGroupService;
	private final SubsystemService subsystemService;
	private final ReviewPhaseService reviewPhaseService;
	private final MissionPhaseService missionPhaseService;
	private final TemplateRenderer templateRenderer;
	private TransferService transferService;
	private HazardControlService controlService;
	private HazardCauseService causeService;
	private HazardService hazardService;
	HazardCauseService hazardCauseService;

	public HazardServlet(HazardService hazardService,
			HazardGroupService hazardGroupService,
			SubsystemService subsystemService,
			ReviewPhaseService reviewPhaseService,
			MissionPhaseService missionPhaseService,
			TemplateRenderer templateRenderer, TransferService transferService,
			HazardControlService controlService,
			HazardCauseService causeService,
			HazardCauseService hazardCauseService) {
		this.hazardService = checkNotNull(hazardService);
		this.hazardGroupService = checkNotNull(hazardGroupService);
		this.subsystemService = checkNotNull(subsystemService);
		this.reviewPhaseService = checkNotNull(reviewPhaseService);
		this.missionPhaseService = checkNotNull(missionPhaseService);
		this.templateRenderer = checkNotNull(templateRenderer);
		this.transferService = checkNotNull(transferService);
		this.controlService = checkNotNull(controlService);
		this.causeService = checkNotNull(causeService);
		this.hazardService = checkNotNull(hazardService);
		this.hazardCauseService = checkNotNull(hazardCauseService);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO: Look into re-factoring permissions/generating error messages is
		// done - see issue on the Huboard.

		JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor
				.getJiraAuthenticationContext();
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
						if (!hazardService.hasHazardPermission(
								hazard.getProjectID(),
								jiraAuthenticationContext.getUser())) {
							error = true;
							errorMessage = "Either this Hazard Report doesn't exist (it may have been deleted) or you ("
									+ jiraAuthenticationContext.getUser()
											.getUsername()
									+ ") do not have permission to view/edit it.";
						}
					} else {
						error = true;
						errorMessage = "Either this Hazard Report doesn't exist (it may have been deleted) or you ("
								+ jiraAuthenticationContext.getUser()
										.getUsername()
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
				errorList
						.add("where [number] is the unique identifier of the Hazard Report.");
			}

			// Decide which page to render for the user, error-page or
			// hazard-page
			if (error == true) {
				context.put("errorMessage", errorMessage);
				context.put("errorList", errorList);
				templateRenderer.render("templates/error-page.vm", context,
						resp.getWriter());
			} else {
				Project jiraProject = hazardService.getHazardProject(hazard);
				Issue jiraSubtask = hazardService.getHazardSubTask(hazard);
				String baseURL = ComponentAccessor.getApplicationProperties()
						.getString("jira.baseurl");
				String jiraSubTaskSummary = jiraSubtask.getSummary();
				String jiraSubtaskURL = baseURL + "/browse/"
						+ jiraProject.getKey() + "-" + jiraSubtask.getNumber();
				String jiraProjectName = jiraProject.getName();
				String jiraProjectURL = baseURL + "/browse/"
						+ jiraProject.getKey();

				Hazard_Causes[] cause = hazard.getHazardCauses();
				List<TransferRiskValue> transferredToHazard = new ArrayList<TransferRiskValue>();
				List<TransferRiskValue> transferredToACause = new ArrayList<TransferRiskValue>();
				List<TransferRiskValue> transferredInACircle = new ArrayList<TransferRiskValue>();
			
				for (int i = 0; i < hazard.getHazardCauses().length; i++) {

					if (cause[i].getTransfer() != 0) {
						TransferRiskValue transferResult = doGetTransfer(cause[i]
								.getTransfer(), cause[i]);
						System.out.println("transferResult " + transferResult);
						if (transferResult.isCircular()) {
							transferredInACircle.add(transferResult);
						} else if (transferResult.isTransferred()) {
							transferredToACause.add(transferResult);
						} else {
							transferredToHazard.add(transferResult);
						}
						
					}
					
					//just to see if something works
					System.out.println("test" + transferredInACircle.size());
					System.out.println("transferTest1 " + String.valueOf(transferredInACircle));
					System.out.println("transferTest2 " + String.valueOf(transferredInACircle.get(0).getRiskCategory()));
					System.out.println("transferTest3 " + transferredInACircle.get(1).getCauseNumber());
					System.out.println("transferTest4 " + transferredInACircle.get(1).getTransferTargetId());
					System.out.println("transferredToHazard " + transferredToHazard);
					System.out.println("transferredToACause " + transferredToACause.toString());
					System.out.println("transferredInACircle " + transferredInACircle.toString());
				}
				
				

				context.put("hazard", hazard);
				context.put("jiraSubTaskSummary", jiraSubTaskSummary);
				context.put("jiraSubtaskURL", jiraSubtaskURL);
				context.put("jiraProjectName", jiraProjectName);
				context.put("jiraProjectURL", jiraProjectURL);
				context.put("nonAssociatedSubsystems",
						subsystemService.getRemaining(hazard.getSubsystems()));
				context.put("hazardPreparer",
						hazardService.getHazardPreparerInformation(hazard));
				context.put("reviewPhases", reviewPhaseService.all());
				context.put("nonAssociatedMissionPhases", missionPhaseService
						.getRemaining(hazard.getMissionPhases()));
				context.put("nonAssociatedHazardGroups", hazardGroupService
						.getRemaining(hazard.getHazardGroups()));
				context.put("initiationDate",
						removeTimeFromDate(hazard.getInitiationDate()));
				context.put("completionDate",
						removeTimeFromDate(hazard.getCompletionDate()));
				context.put("transferredToHazard", transferredToHazard.toString());
				context.put("transferredToACause", transferredToACause.toString());
				context.put("transferredInACircle", transferredInACircle.toString());
				//context.put("cause", cause);
				templateRenderer.render("templates/hazard-page.vm", context,
						resp.getWriter());
			}
		} else {
			resp.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			String hazardIDStr = req.getParameter("hazardID");
			int hazardID = Integer.parseInt(hazardIDStr);
			String hazardNumber = req.getParameter("hazardNumber");
			String version = req.getParameter("hazardVersionNumber");
			String hazardTitle = req.getParameter("hazardTitle");
			Subsystems[] subsystems = subsystemService
					.getSubsystemsByID(changeStringArray(req
							.getParameterValues("hazardSubsystem")));
			Review_Phases reviewPhase = reviewPhaseService
					.getReviewPhaseByID(req.getParameter("hazardReviewPhase"));
			Mission_Phase[] missionPhases = missionPhaseService
					.getMissionPhasesByID(changeStringArray(req
							.getParameterValues("hazardPhase")));
			Hazard_Group[] hazardGroups = hazardGroupService
					.getHazardGroupsByID(changeStringArray(req
							.getParameterValues("hazardGroup")));
			String safetyRequirements = req
					.getParameter("hazardSafetyRequirements");
			String description = req.getParameter("hazardDescription");
			String justification = req.getParameter("hazardJustification");
			String openWork = req.getParameter("hazardOpenWork");
			Date initiation = changeToDate(req.getParameter("hazardInitation"));
			Date completion = changeToDate(req.getParameter("hazardCompletion"));

			hazardService.update(hazardID, hazardNumber, version, hazardTitle,
					subsystems, reviewPhase, missionPhases, hazardGroups,
					safetyRequirements, description, justification, openWork,
					initiation, completion);

			JSONObject json = new JSONObject();
			createJson(json, "updateSuccess", true);
			createJson(json, "errorMessage", "none");
			resp.setContentType("application/json");
			resp.getWriter().println(json);
		} else {
			resp.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}

	private TransferRiskValue doGetTransfer(int transferId, Hazard_Causes originCause){
		int transferToLookup = 0;
		
			boolean done = false;
			while (!done) {
				Set<Integer> previouslyVisitedTransfers = new HashSet<Integer>();
				Transfers transfer = transferService.getTransferByID(transferId);
				System.out.println("transfer " + transfer);
				//want original causeId
				
				Hazard_Causes cause = hazardCauseService.getHazardCauseByID(String.valueOf(transfer.getTargetID()));
				System.out.println("cause " + cause);

				transferId = cause.getTransfer();
				// Lookup the cause transfer

				previouslyVisitedTransfers.add(transferId);
				transferToLookup = transferId;

				if (transfer.getTargetType().equals("CAUSE")) {

					Hazard_Causes targetCause = causeService
							.getHazardCauseByID(transfer.getTargetID());

					if (previouslyVisitedTransfers.contains(targetCause
							.getTransfer())) {
						// We have already seen this transfer. Thus, thereis a
						// circular reference to transfers. This is bad. Weneed
						// to
						// return a value that the transfers are
						// circular.
						String causeId = String.valueOf(targetCause.getID());
						String riskCategory = String.valueOf(targetCause.getRiskCategory());
						String riskLikeliHood = String.valueOf(targetCause.getRiskLikelihood());
						
						

						return new TransferRiskValue(causeId, "HAZARD", true,
								true, originCause.getID(), originCause.getCauseNumber(), riskCategory, riskLikeliHood);
					} else {
						// We need to loop around again to follow the
						// transfer chain.

						transferToLookup = targetCause.getTransfer();
					}

				} else if (transfer.getTargetType().equals("HAZARD")) {
					// The cause transfers to a hazard, so we need to return
					// something
					// which indicates this. In the template, causes which link
					// to
					// hazards go into a separate list.
					Hazards hazard = hazardService.getHazardByID(String
							.valueOf(transfer.getTargetID()));
					String hazardId = String.valueOf(hazard.getID());
					String riskCategory = String.valueOf(cause.getRiskCategory());
					String riskLikeliHood = String.valueOf(cause.getRiskLikelihood());
					
					
					return new TransferRiskValue(hazardId, "HAZARD", false,
							true, originCause.getID(), originCause.getCauseNumber(), riskCategory, riskLikeliHood);
				}
			}
		throw new IllegalArgumentException("Could not get transfer type.");
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