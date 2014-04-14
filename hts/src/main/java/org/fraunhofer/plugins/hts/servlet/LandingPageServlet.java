package org.fraunhofer.plugins.hts.servlet;

import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Payload;
import org.fraunhofer.plugins.hts.db.service.HazardGroupService;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.MissionPayloadService;
import org.fraunhofer.plugins.hts.db.service.ReviewPhaseService;
import org.fraunhofer.plugins.hts.db.service.RiskCategoryService;
import org.fraunhofer.plugins.hts.db.service.RiskLikelihoodsService;
import org.fraunhofer.plugins.hts.db.service.SubsystemService;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

public class LandingPageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final HazardService hazardService;
	private final HazardGroupService hazardGroupService;
	private final RiskCategoryService riskCategoryService;
	private final RiskLikelihoodsService riskLikelihoodService;
	private final ReviewPhaseService reviewPhaseService;
	private final TemplateRenderer templateRenderer;
	private final SubsystemService subsystemService;
	private final MissionPayloadService missionPayloadService;

	public LandingPageServlet(HazardService hazardService, HazardGroupService hazardGroupService,
			TemplateRenderer templateRenderer, RiskCategoryService riskCategoryService,
			RiskLikelihoodsService riskLikelihoodService, ReviewPhaseService reviewPhaseService,
			MissionPayloadService missionPayloadService, SubsystemService subsystemService) {
		this.hazardService = checkNotNull(hazardService);
		this.hazardGroupService = checkNotNull(hazardGroupService);
		this.riskCategoryService = checkNotNull(riskCategoryService);
		this.riskLikelihoodService = checkNotNull(riskLikelihoodService);
		this.reviewPhaseService = checkNotNull(reviewPhaseService);
		this.templateRenderer = checkNotNull(templateRenderer);
		this.subsystemService = checkNotNull(subsystemService);
		this.missionPayloadService = checkNotNull(missionPayloadService);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			if ("y".equals(req.getParameter("edit"))) {
				Hazards hazard = hazardService.getHazardByID(req.getParameter("key"));
				Map<String, Object> context = Maps.newHashMap();
				context.put("hazard", hazard);
				context.put("hazardGroups", hazardGroupService.all());
				context.put("riskCategories", riskCategoryService.all());
				context.put("riskLikelihoods", riskLikelihoodService.all());
				context.put("reviewPhases", reviewPhaseService.all());
				context.put("subsystems", subsystemService.getRemainingGroups(hazard.getSubsystems()));
				context.put("initDate", removeTimeFromDate(hazard.getInitiationDate()));
				context.put("compDate", removeTimeFromDate(hazard.getCompletionDate()));
				res.setContentType("text/html");
				templateRenderer.render("templates/EditHazard.vm", context, res.getWriter());
			} else {
				Map<String, Object> context = Maps.newHashMap();
				context.put("hazardReports", hazardService.all());
				res.setContentType("text/html");
				templateRenderer.render("templates/LandingPage.vm", context, res.getWriter());
			}
		} else {
			res.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		Hazards hazard = hazardService.getHazardByID(req.getParameter("key"));
		//TODO SOME VALIDATION THE USER IS ALLOWED TO DELETE 
		//TODO FIX THE REPLY STRING
		String respStr = respStr = "{ \"success\" : \"false\", error: \"Couldn't find hazard report\"}";;
		
		if(hazard != null){
			missionPayloadService.deleteMissionPayload(hazard.getMissionPayload().getID());
			hazardService.deleteHazard(hazard.getID());
			respStr = "{ \"success\" : \"true\" }";
		}
		res.setContentType("application/json;charset=utf-8");
	    // Send the raw output string we put together
	    res.getWriter().write(respStr);
	}

	private String removeTimeFromDate(Date date) {
		if (date != null) {
			return date.toString().substring(0, 10);
		}

		return null;
	}
}