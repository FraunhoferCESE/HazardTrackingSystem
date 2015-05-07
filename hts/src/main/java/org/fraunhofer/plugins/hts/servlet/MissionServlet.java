package org.fraunhofer.plugins.hts.servlet;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fraunhofer.plugins.hts.service.HazardService;
import org.fraunhofer.plugins.hts.service.JIRAProjectService;
import org.fraunhofer.plugins.hts.view.model.JIRAProject;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;

public class MissionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final JIRAProjectService missionService;
	private final HazardService hazardService;
	private final TemplateRenderer templateRenderer;

	public MissionServlet(JIRAProjectService missionService, HazardService hazardService, 
			TemplateRenderer templateRenderer) {
		this.missionService = missionService;
		this.hazardService = hazardService;
		this.templateRenderer = checkNotNull(templateRenderer);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();		
		if (jiraAuthenticationContext.isLoggedInUser()) {
			Map<String, Object> context = Maps.newHashMap();
			// Add all Missions to the context for mission navigation
			List<JIRAProject> userProjects = missionService.getUserProjects(jiraAuthenticationContext.getUser());
			context.put("missions", userProjects);
			// Add all Hazards to the context for hazard navigation	
			context.put("hazards", hazardService.getUserHazardsMinimal(userProjects));
			res.setContentType("text/html");
			templateRenderer.render("templates/mission-page.vm", context, res.getWriter());
		} else {
			res.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}
}