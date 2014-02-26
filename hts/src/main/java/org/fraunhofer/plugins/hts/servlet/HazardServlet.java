package org.fraunhofer.plugins.hts.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;

import org.apache.commons.lang.time.DateUtils;
import org.fraunhofer.plugins.hts.db.Risk_Categories;
import org.fraunhofer.plugins.hts.db.service.HazardGroupService;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.RiskCategoryService;
import org.fraunhofer.plugins.hts.db.service.RiskLikelihoodsService;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

public final class HazardServlet extends HttpServlet {
	private final HazardService hazardService;
	private final HazardGroupService hazardGroupService;
	private final RiskCategoryService riskCategoryService;
	private final RiskLikelihoodsService riskLikelihood;
	private final TemplateRenderer templateRenderer;
	
	public HazardServlet(HazardService hazardService, HazardGroupService hazardGroupService, TemplateRenderer templateRenderer, RiskCategoryService riskCategoryService, RiskLikelihoodsService riskLikelihood) {
		this.hazardService = checkNotNull(hazardService);
		this.hazardGroupService = checkNotNull(hazardGroupService);
		this.riskCategoryService = checkNotNull(riskCategoryService);
		this.riskLikelihood = checkNotNull(riskLikelihood);
		this.templateRenderer = templateRenderer;
	}

	// TODO remove the HTML code, fix the form and use the servlet to input to
	// the database
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		Map<String, Object> context = Maps.newHashMap();
		context.put("hazardGroups", hazardGroupService.all());
		context.put("riskCategories", riskCategoryService.all());
		context.put("riskLikelihoods", riskLikelihood.all());
		res.setContentType("text/html;charset=utf-8");
		templateRenderer.render("templates/HazardForm.vm", context, res.getWriter());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		final String hazardNum = req.getParameter("hazard-number");
		final String title = req.getParameter("hazard-title");
		final String description = req.getParameter("hazard-description");
		Risk_Categories risk = null;
		final String preparer = ComponentAccessor.getJiraAuthenticationContext().getUser().getDisplayName();
		hazardService.add(title, description, preparer, hazardNum, new Date(), DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH), new Date(), risk);

		res.sendRedirect(req.getContextPath() + "/plugins/servlet/hazardservlet");
	}
}