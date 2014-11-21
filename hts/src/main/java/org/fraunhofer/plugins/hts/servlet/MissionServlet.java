package org.fraunhofer.plugins.hts.servlet;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.MissionPayloadService;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;

public class MissionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final MissionPayloadService missionService;
	private final HazardService hazardService;
	private final TemplateRenderer templateRenderer;

	public MissionServlet(MissionPayloadService missionService, HazardService hazardService, 
			TemplateRenderer templateRenderer) {
		this.missionService = missionService;
		this.hazardService = hazardService;
		this.templateRenderer = checkNotNull(templateRenderer);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			Map<String, Object> context = Maps.newHashMap();
			// Add all Missions to the context for mission navigation
			context.put("missions", missionService.all());
			// Add all Hazards to the context for hazard navigation
			context.put("hazards", hazardService.getAllHazardsMinimal());
			res.setContentType("text/html");
			templateRenderer.render("templates/mission-page.vm", context, res.getWriter());
		} 
		else {
			res.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// Intentionally empty
	}
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// Intentionally empty
	}
}