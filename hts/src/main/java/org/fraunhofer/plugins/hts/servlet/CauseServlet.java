package org.fraunhofer.plugins.hts.servlet;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Risk_Categories;
import org.fraunhofer.plugins.hts.db.Risk_Likelihoods;
import org.fraunhofer.plugins.hts.db.service.HazardCauseService;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.MissionPayloadService;
import org.fraunhofer.plugins.hts.db.service.RiskCategoryService;
import org.fraunhofer.plugins.hts.db.service.RiskLikelihoodsService;
import org.fraunhofer.plugins.hts.db.service.TransferService;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class CauseServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final HazardCauseService hazardCauseService;
	private final HazardService hazardService;
	private final MissionPayloadService missionPayloadService;
	private final TemplateRenderer templateRenderer;
	private final RiskCategoryService riskCategoryService;
	private final RiskLikelihoodsService riskLikelihoodService;
	private final TransferService transferService;

	public CauseServlet(HazardCauseService hazardCauseService, TemplateRenderer templateRenderer, 
			HazardService hazardService, MissionPayloadService missionPayloadService, 
			RiskCategoryService riskCategoryService, RiskLikelihoodsService riskLikelihoodService, 
			TransferService transferService) {
		this.hazardCauseService = checkNotNull(hazardCauseService);
		this.templateRenderer = checkNotNull(templateRenderer);
		this.hazardService = checkNotNull(hazardService);
		this.missionPayloadService = checkNotNull(missionPayloadService);
		this.riskCategoryService = checkNotNull(riskCategoryService);
		this.riskLikelihoodService = checkNotNull(riskLikelihoodService);
		this.transferService = checkNotNull(transferService);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			Map<String, Object> context = Maps.newHashMap();
			boolean contains = req.getParameterMap().containsKey("id");
			if (contains == true) {
				String hazardIDStr = req.getParameter("id");
				// TODO: Parsing could fail, will throw NumberFormatException.
				int hazardID =  Integer.parseInt(hazardIDStr);
				// TODO: CurrentHazard could be returned as null.
				Hazards currentHazard = hazardService.getHazardByID(hazardID);
				context.put("hazard", currentHazard);
				context.put("causes", hazardCauseService.getAllNonDeletedCausesWithinAHazard(currentHazard));
				context.put("transferredCauses", hazardCauseService.getAllTransferredCauses(currentHazard));
				context.put("riskCategories", riskCategoryService.all());
				context.put("riskLikelihoods", riskLikelihoodService.all());
				List<Hazards> allHazardsBelongingToMission = hazardService.getHazardsByMissionPayload(currentHazard.getProjectID());
				context.put("allHazardsBelongingToMission", allHazardsBelongingToMission);
				resp.setContentType("text/html;charset=utf-8");
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
				if (req.getParameter("causeRisk") != "") {
					risk = riskCategoryService.getRiskByID(req.getParameter("causeRisk"));
				} else {
					risk = null;
				}
				
				Risk_Likelihoods likelihood;
				if (req.getParameter("causeLikelihood") != "") {
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
					hazardCauseService.updateRegularCause(causeID, title, owner, risk, likelihood, description, effects, safetyFeatures);
				} else {
					// Regular cause creation
					String hazardIDStr = req.getParameter("hazardID");
					int hazardID = Integer.parseInt(hazardIDStr);
					hazardCauseService.add(hazardID, title, owner, risk, likelihood, description, effects, safetyFeatures);
				}
			} else {
				boolean existing = Boolean.parseBoolean(req.getParameter("regular"));
				if (existing == true) {
					// Cause transfer update
					String causeIDStr = req.getParameter("causeID");
					int causeID = Integer.parseInt(causeIDStr);
					String transferReason = req.getParameter("transferReason");
					hazardCauseService.updateTransferredCause(causeID, transferReason);
				} else {
					// Cause transfer creation				
					int targetHazardID = Integer.parseInt(req.getParameter("hazardList"));
					int targetCauseID = 0;
					boolean contains = req.getParameterMap().containsKey("causeList");
					if (contains == true) {
						targetCauseID = Integer.parseInt(req.getParameter("causeList"));
					}
					
					int originHazardID = Integer.parseInt(req.getParameter("hazardID"));
					String transferReason = req.getParameter("transferReason");
					
					if (targetCauseID == 0) {
						hazardCauseService.addHazardTransfer(originHazardID, targetHazardID, transferReason);
					} else {
						hazardCauseService.addCauseTransfer(originHazardID, targetCauseID, transferReason);
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
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			int causeID = Integer.parseInt(req.getParameter("id"));
			String deleteReason = req.getParameter("reason");
			Hazard_Causes cause = hazardCauseService.deleteCause(causeID, deleteReason);
			
			JSONObject jsonResponse = new JSONObject();
			if (cause != null) {
				createJson(jsonResponse, "updateSuccess", true);
				createJson(jsonResponse, "errorMessage", "none");
			} else {
				createJson(jsonResponse, "updateSuccess", false);
				createJson(jsonResponse, "errorMessage", "Could not find Cause.");	
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