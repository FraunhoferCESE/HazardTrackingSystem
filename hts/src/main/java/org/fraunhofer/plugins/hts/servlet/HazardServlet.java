package org.fraunhofer.plugins.hts.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;

import org.apache.commons.lang.time.DateUtils;
import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Payload;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.Risk_Categories;
import org.fraunhofer.plugins.hts.db.Risk_Likelihoods;
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
 * Takes care of handling the requests. Loading the Hazard form and allowing user to save to the database.
 * @author ASkulason
 *
 */
public final class HazardServlet extends HttpServlet {
	private final HazardService hazardService;
	private final HazardGroupService hazardGroupService;
	private final RiskCategoryService riskCategoryService;
	private final RiskLikelihoodsService riskLikelihoodService;
	private final SubsystemService subsystemService;
	private final ReviewPhaseService reviewPhaseService;
	private final MissionPayloadService missionPayloadService;
	private final TemplateRenderer templateRenderer;
	
	public HazardServlet(HazardService hazardService, HazardGroupService hazardGroupService, TemplateRenderer templateRenderer, RiskCategoryService riskCategoryService, 
			RiskLikelihoodsService riskLikelihoodService, SubsystemService subsystemService, ReviewPhaseService reviewPhaseService, MissionPayloadService missionPayloadService) {
		this.hazardService = checkNotNull(hazardService);
		this.hazardGroupService = checkNotNull(hazardGroupService);
		this.riskCategoryService = checkNotNull(riskCategoryService);
		this.riskLikelihoodService = checkNotNull(riskLikelihoodService);
		this.subsystemService = checkNotNull(subsystemService);
		this.reviewPhaseService = checkNotNull(reviewPhaseService);
		this.missionPayloadService = checkNotNull(missionPayloadService);
		this.templateRenderer = templateRenderer;
	}

	// TODO 
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		Map<String, Object> context = Maps.newHashMap();
		context.put("hazardGroups", hazardGroupService.all());
		context.put("riskCategories", riskCategoryService.all());
		context.put("riskLikelihoods", riskLikelihoodService.all());
		context.put("reviewPhases", reviewPhaseService.all());
		context.put("PreparerName", ComponentAccessor.getJiraAuthenticationContext().getUser().getDisplayName());
		context.put("PreparerEmail", ComponentAccessor.getJiraAuthenticationContext().getUser().getEmailAddress());
		res.setContentType("text/html;charset=utf-8");
		templateRenderer.render("templates/HazardForm.vm", context, res.getWriter());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		//TODO clean up
		final String hazardNum = req.getParameter("hazard-number");
		final String title = req.getParameter("hazard-title");
		final String description = req.getParameter("hazard-description");
		final String preparer = ComponentAccessor.getJiraAuthenticationContext().getUser().getName() + " (" + ComponentAccessor.getJiraAuthenticationContext().getUser().getEmailAddress() + ")";
		final Review_Phases reviewPhase = reviewPhaseService.getReviewPhaseByID(req.getParameter("hazard-reviewPhase"));
		final Risk_Categories risk = riskCategoryService.getRiskByID(req.getParameter("hazard-risk"));
		final Risk_Likelihoods likelihood = riskLikelihoodService.getLikelihoodByID(req.getParameter("hazard-likelihood"));
		final Hazard_Group group = hazardGroupService.getHazardGroupByID(req.getParameter("hazard-group"));
		final Date lastEdit = new Date();
		final String payload = req.getParameter("hazard-payload");
		
		//TODO see if they want to pull in the jira project name as payload
		if("y".equals(req.getParameter("edit"))) {
			//TODO move to the correct service
			Hazards old = hazardService.getHazardByID(req.getParameter("key"));
			old.setHazardNum(hazardNum);
			old.setTitle(title);
			old.setHazardDesc(description);
			old.setReviewPhase(reviewPhase);
			old.setRiskCategory(risk);
			old.setRiskLikelihood(likelihood);
			old.setHazardGroup(group);
			old.setRevisionDate(lastEdit);
			Mission_Payload current = old.getMissionPayload();
			current.setName(payload);
			current.save();
			old.save();
		}
		else {
			final Date created = changeDateFormat(req.getParameter("hazard-initation"));
			final Date completed = changeDateFormat(req.getParameter("hazard-completion"));

			final String subsystem = req.getParameter("hazard-subsystem");
			Hazards hazard = hazardService.add(title, description, preparer, hazardNum, created, completed, lastEdit, risk, likelihood, group, reviewPhase);
			missionPayloadService.add(hazard, payload);
			subsystemService.add(hazard, subsystem, subsystem);
		}
		res.sendRedirect(req.getContextPath() + "/plugins/servlet/hazardlist");
	}
	
	private Date changeDateFormat(String date) {
		SimpleDateFormat fromForm = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat wantedForm = new SimpleDateFormat("MM/dd/yyyy");
		try {
			String reformatted = wantedForm.format(fromForm.parse(date));
			Date converted = wantedForm.parse(reformatted);
			return converted;
		} catch (ParseException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}
}