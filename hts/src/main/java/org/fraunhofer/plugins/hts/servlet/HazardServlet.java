package org.fraunhofer.plugins.hts.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;

import org.apache.commons.lang.time.DateUtils;
import org.apache.velocity.exception.ParseErrorException;
import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.Risk_Categories;
import org.fraunhofer.plugins.hts.db.Risk_Likelihoods;
import org.fraunhofer.plugins.hts.db.Subsystems;
import org.fraunhofer.plugins.hts.db.service.HazardGroupService;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.ReviewPhaseService;
import org.fraunhofer.plugins.hts.db.service.RiskCategoryService;
import org.fraunhofer.plugins.hts.db.service.RiskLikelihoodsService;
import org.fraunhofer.plugins.hts.db.service.SubsystemService;
import org.ofbiz.core.entity.jdbc.dbtype.SimpleDatabaseType;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
	private final TemplateRenderer templateRenderer;
	
	public HazardServlet(HazardService hazardService, HazardGroupService hazardGroupService, TemplateRenderer templateRenderer, RiskCategoryService riskCategoryService, 
			RiskLikelihoodsService riskLikelihoodService, SubsystemService subsystemService, ReviewPhaseService reviewPhaseService) {
		this.hazardService = checkNotNull(hazardService);
		this.hazardGroupService = checkNotNull(hazardGroupService);
		this.riskCategoryService = checkNotNull(riskCategoryService);
		this.riskLikelihoodService = checkNotNull(riskLikelihoodService);
		this.subsystemService = checkNotNull(subsystemService);
		this.reviewPhaseService = checkNotNull(reviewPhaseService);
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
		final String preparer = ComponentAccessor.getJiraAuthenticationContext().getUser().getName();
		final String created = changeDateFormat(req.getParameter("hazard-initation"));
		final String completed = changeDateFormat(req.getParameter("hazard-initation"));
		final Review_Phases reviewPhase = reviewPhaseService.getReviewPhaseByID(req.getParameter("hazard-reviewPhase"));
		final Risk_Categories risk = riskCategoryService.getRiskByID(req.getParameter("hazard-risk"));
		final Risk_Likelihoods likelihood = riskLikelihoodService.getLikelihoodByID(req.getParameter("hazard-likelihood"));
		final Hazard_Group group = hazardGroupService.getHazardGroupByID(req.getParameter("hazard-group"));
		final Date lastEdit = DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH);
		hazardService.add(title, description, preparer, hazardNum, created, completed, lastEdit, risk, likelihood, group, reviewPhase);
				res.sendRedirect(req.getContextPath() + "/plugins/servlet/hazardservlet");
	}
	
	private String changeDateFormat(String date) {
		SimpleDateFormat fromForm = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat wantedForm = new SimpleDateFormat("MM/dd/yyyy");
		try {
			String reformatted = wantedForm.format(fromForm.parse(date));
			return reformatted;
		} catch (ParseException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return date;
	}
}