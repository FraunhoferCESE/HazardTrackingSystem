package org.fraunhofer.plugins.hts.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;
import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Payload;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.Risk_Categories;
import org.fraunhofer.plugins.hts.db.Risk_Likelihoods;
import org.fraunhofer.plugins.hts.db.Subsystems;
import org.fraunhofer.plugins.hts.db.service.HazardGroupService;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.MissionPayloadService;
import org.fraunhofer.plugins.hts.db.service.ReviewPhaseService;
import org.fraunhofer.plugins.hts.db.service.RiskCategoryService;
import org.fraunhofer.plugins.hts.db.service.RiskLikelihoodsService;
import org.fraunhofer.plugins.hts.db.service.SubsystemService;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

/**
 * Takes care of handling the requests. Loading the Hazard form and allowing
 * user to save to the database.
 * 
 * @author ASkulason
 * 
 */

public final class HazardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final HazardService hazardService;
	private final HazardGroupService hazardGroupService;
	private final RiskCategoryService riskCategoryService;
	private final RiskLikelihoodsService riskLikelihoodService;
	private final SubsystemService subsystemService;
	private final ReviewPhaseService reviewPhaseService;
	private final MissionPayloadService missionPayloadService;
	private final TemplateRenderer templateRenderer;

	public HazardServlet(HazardService hazardService, HazardGroupService hazardGroupService,
			TemplateRenderer templateRenderer, RiskCategoryService riskCategoryService,
			RiskLikelihoodsService riskLikelihoodService, SubsystemService subsystemService,
			ReviewPhaseService reviewPhaseService, MissionPayloadService missionPayloadService) {
		this.hazardService = checkNotNull(hazardService);
		this.hazardGroupService = checkNotNull(hazardGroupService);
		this.riskCategoryService = checkNotNull(riskCategoryService);
		this.riskLikelihoodService = checkNotNull(riskLikelihoodService);
		this.subsystemService = checkNotNull(subsystemService);
		this.reviewPhaseService = checkNotNull(reviewPhaseService);
		this.missionPayloadService = checkNotNull(missionPayloadService);
		this.templateRenderer = checkNotNull(templateRenderer);
	}

	//TODO
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			Map<String, Object> context = Maps.newHashMap();
			context.put("hazardGroups", hazardGroupService.all());
			context.put("riskCategories", riskCategoryService.all());
			context.put("riskLikelihoods", riskLikelihoodService.all());
			context.put("reviewPhases", reviewPhaseService.all());
			context.put("PreparerName", ComponentAccessor.getJiraAuthenticationContext().getUser().getName());
			context.put("PreparerEmail", ComponentAccessor.getJiraAuthenticationContext().getUser().getEmailAddress());

			res.setContentType("text/html;charset=utf-8");
			templateRenderer.render("templates/HazardPage.vm", context, res.getWriter());

		} 
		else {
			res.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		final String hazardNum = req.getParameter("hazardNumber");
		final String title = req.getParameter("hazardTitle");
		final String description = req.getParameter("hazardDescription");
		final String preparer = ComponentAccessor.getJiraAuthenticationContext().getUser().getName();
		final String email = ComponentAccessor.getJiraAuthenticationContext().getUser().getEmailAddress();
		final Review_Phases reviewPhase = reviewPhaseService.getReviewPhaseByID(req.getParameter("hazardReviewPhase"));
		final Risk_Categories risk = riskCategoryService.getRiskByID(req.getParameter("hazardRisk"));
		final Risk_Likelihoods likelihood = riskLikelihoodService.getLikelihoodByID(req.getParameter("hazardLikelihood"));
		final Hazard_Group group = hazardGroupService.getHazardGroupByID(req.getParameter("hazardGroup"));
		final Date revisionDate = new Date();
		final String payloadName = req.getParameter("hazardPayload");
		//TODO FIX when multiple select is available
		final Subsystems[] subsystems = null;//req.getParameterValues("hazardSubsystem");
		final Date created = changeToDate(req.getParameter("hazardInitation"));
		final Date completed = changeToDate(req.getParameter("hazardCompletion"));
		final String addNew = req.getParameter("hazardSaveAdd");
		JSONObject json = new JSONObject();
		
		// TODO see if they want to pull in the jira project name as payload
		if ("y".equals(req.getParameter("edit"))) {
			String id = req.getParameter("key");
			Hazards updated = hazardService.update(id, title, description, preparer, email, hazardNum, created,
					completed, revisionDate, risk, likelihood, group, reviewPhase, subsystems);
			Mission_Payload payloadToUpdate = updated.getMissionPayload();
			missionPayloadService.update(payloadToUpdate, payloadName);
			
			createJson(json, "hazardID", updated.getID());
			createJson(json, "hazardNumber", updated.getHazardNum());		
		} 
		else {
			Hazards hazard = hazardService.add(title, description, preparer, email, hazardNum, created, completed,
					revisionDate, risk, likelihood, group, reviewPhase, subsystems);
			missionPayloadService.add(hazard, payloadName);
			
			createJson(json, "hazardID", hazard.getID());
			createJson(json, "hazardNumber", hazard.getHazardNum());
		}
		//If Save and create another was pressed addNew should not contain null
		if(addNew != null) {
			createJson(json, "redirect", req.getContextPath() + "/plugins/servlet/hazardform");
		}
		
		res.getWriter().println(json);
	}

	private Date changeToDate(String date) {
		if (date != null) {
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
}