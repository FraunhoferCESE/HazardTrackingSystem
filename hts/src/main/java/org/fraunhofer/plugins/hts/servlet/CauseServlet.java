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

import org.fraunhofer.plugins.hts.model.Hazard_Causes;
import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.model.Risk_Categories;
import org.fraunhofer.plugins.hts.model.Risk_Likelihoods;
import org.fraunhofer.plugins.hts.service.CauseService;
import org.fraunhofer.plugins.hts.service.HazardService;
import org.fraunhofer.plugins.hts.service.RiskCategoryService;
import org.fraunhofer.plugins.hts.service.RiskLikelihoodsService;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.extras.common.log.Logger.Log;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class CauseServlet extends HttpServlet {
	final Log logger = Logger.getInstance(CauseServlet.class);

	private static final long serialVersionUID = 1L;
	private final CauseService causeService;
	private final HazardService hazardService;
	private final TemplateRenderer templateRenderer;
	private final RiskCategoryService riskCategoryService;
	private final RiskLikelihoodsService riskLikelihoodService;
	private final DateTimeFormatter dateTimeFormatter;

	public CauseServlet(CauseService hazardCauseService, TemplateRenderer templateRenderer,
			HazardService hazardService, RiskCategoryService riskCategoryService,
			RiskLikelihoodsService riskLikelihoodService, DateTimeFormatter dateTimeFormatter) {
		this.causeService = checkNotNull(hazardCauseService);
		this.templateRenderer = checkNotNull(templateRenderer);
		this.hazardService = checkNotNull(hazardService);
		this.riskCategoryService = checkNotNull(riskCategoryService);
		this.riskLikelihoodService = checkNotNull(riskLikelihoodService);
		this.dateTimeFormatter = dateTimeFormatter.forLoggedInUser();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO: Look into re-factoring permissions/generating error messages is
		// done

		JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
		resp.setContentType("text/html;charset=utf-8");

		if (jiraAuthenticationContext.isLoggedInUser()) {
			Map<String, Object> context = Maps.newHashMap();
			context.put("dateFormatter", dateTimeFormatter);
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
					if (hazard == null
							|| !hazardService.hasHazardPermission(hazard.getProjectID(),
									jiraAuthenticationContext.getUser())) {
						error = true;
						errorMessage = "Either this Hazard Report doesn't exist (it may have been deleted) or you ("
								+ jiraAuthenticationContext.getUser().getUsername()
								+ ") do not have permission to view/edit it.";
					} else {
						context.put("hazard", hazard);
						context.put("causes", causeService.getAllNonDeletedCausesWithinHazard(hazard));
						context.put("transferredCauses", causeService.getAllTransferredCauses(hazard));
						context.put("riskCategories", riskCategoryService.all());
						context.put("riskLikelihoods", riskLikelihoodService.all());
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
				templateRenderer.render("templates/cause-page.vm", context, resp.getWriter());
			}
		} else {
			resp.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			boolean regular = Boolean.parseBoolean(req.getParameter("regular"));
			if (regular == true) {
				String title = req.getParameter("causeTitle");
				String owner = req.getParameter("causeOwner");

				Risk_Categories risk;
				if (!req.getParameter("causeRisk").isEmpty()) {
					risk = riskCategoryService.getRiskByID(req.getParameter("causeRisk"));
				} else {
					risk = null;
				}

				Risk_Likelihoods likelihood;
				if (!Strings.isNullOrEmpty(req.getParameter("causeLikelihood"))) {
					likelihood = riskLikelihoodService.getLikelihoodByID(req.getParameter("causeLikelihood"));
				} else {
					likelihood = null;
				}

				String description = req.getParameter("causeDescription");
				String effects = req.getParameter("causeEffects");
				String safetyFeatures = req.getParameter("causeAdditSafetyFeatures");

				// Regular cause (not a transfer)
				boolean existing = Boolean.parseBoolean(req.getParameter("existing"));
				if (existing == true) {
					// Regular cause update
					String causeIDStr = req.getParameter("causeID");
					int causeID = Integer.parseInt(causeIDStr);
					causeService.updateRegularCause(causeID, title, owner, risk, likelihood, description, effects,
							safetyFeatures);
				} else {
					// Regular cause creation
					String hazardIDStr = req.getParameter("hazardID");
					int hazardID = Integer.parseInt(hazardIDStr);
					causeService.add(hazardID, title, owner, risk, likelihood, description, effects, safetyFeatures);
				}
			} else {
				boolean existing = Boolean.parseBoolean(req.getParameter("existing"));
				if (existing == true) {
					// Cause transfer update
					String causeIDStr = req.getParameter("causeID");
					int causeID = Integer.parseInt(causeIDStr);
					String transferReason = req.getParameter("transferReason");
					causeService.updateTransferredCause(causeID, transferReason);
				} else {
					// Cause transfer creation
					int targetHazardID = Integer.parseInt(req.getParameter("causeHazardList"));
					int targetCauseID = 0;

					if (!Strings.isNullOrEmpty(req.getParameter("causeList"))) {
						targetCauseID = Integer.parseInt(req.getParameter("causeList"));
					}

					int originHazardID = Integer.parseInt(req.getParameter("hazardID"));
					String transferReason = req.getParameter("transferReason");

					if (targetCauseID == 0) {
						causeService.addHazardTransfer(originHazardID, targetHazardID, transferReason);
					} else {
						causeService.addCauseTransfer(originHazardID, targetCauseID, transferReason);
					}
				}
			}

			JSONObject jsonResponse = new JSONObject();
			createJson(jsonResponse, "updateSuccess", true);
			createJson(jsonResponse, "errorMessage", "none");
			resp.setContentType("application/json");
			resp.getWriter().println(jsonResponse);
		} else {
			resp.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.debug("Delete request for Cause received");
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			int causeID = Integer.parseInt(req.getParameter("id"));
			String deleteReason = req.getParameter("reason");
			logger.info("Delete request for Cause id: " + causeID + ", reason: " + deleteReason);
			Hazard_Causes cause = causeService.deleteCause(causeID, deleteReason);

			JSONObject jsonResponse = new JSONObject();
			if (cause != null) {
				createJson(jsonResponse, "updateSuccess", true);
				createJson(jsonResponse, "errorMessage", "none");
				logger.info("Cause id " + causeID + " deleted successfully.");
			} else {
				createJson(jsonResponse, "updateSuccess", false);
				createJson(jsonResponse, "errorMessage", "Could not find Cause.");
				logger.warn("Cause id " + causeID + " could not be deleted: could not find Cause.");
			}
			resp.setContentType("application/json");
			resp.getWriter().println(jsonResponse);
		} else {
			resp.sendRedirect(req.getContextPath() + "/login.jsp");
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