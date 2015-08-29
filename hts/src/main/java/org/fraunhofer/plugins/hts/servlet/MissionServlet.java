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
import org.fraunhofer.plugins.hts.view.model.HazardMinimal;
import org.fraunhofer.plugins.hts.view.model.JIRAProject;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MissionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final HazardService hazardService;
	private final TemplateRenderer templateRenderer;
	private final DateTimeFormatter dateTimeFormatter;

	public MissionServlet(HazardService hazardService, TemplateRenderer templateRenderer,
			DateTimeFormatter dateTimeFormatter) {
		this.hazardService = hazardService;
		this.templateRenderer = checkNotNull(templateRenderer);
		this.dateTimeFormatter = dateTimeFormatter.forLoggedInUser();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
		if (jiraAuthenticationContext.isLoggedInUser()) {
			Map<String, Object> context = Maps.newHashMap();
			context.put("dateFormatter", dateTimeFormatter);

			List<HazardMinimal> hazards = Lists.newArrayList();
			List<JIRAProject> userProjects = Lists.newArrayList();

			String missionId = req.getParameter("missionId");
			if (missionId == null) {
				userProjects = hazardService.getUserProjectsWithHazards(jiraAuthenticationContext.getUser());
				hazards = hazardService.getUserHazardsMinimal(userProjects);

			} else {
				try {
					long projectId = Long.parseLong(missionId);
					if (hazardService.hasHazardPermission(projectId, jiraAuthenticationContext.getUser())) {
						userProjects.add(new JIRAProject(projectId,
								ComponentAccessor.getProjectManager().getProjectObj(projectId).getName()));
						hazards = hazardService.getUserHazardsMinimal(userProjects);
					}
				} catch (NumberFormatException e) {
				}
			}
			context.put("missions", userProjects);
			context.put("hazards", hazards);
			res.setContentType("text/html");
			templateRenderer.render("templates/mission-page.vm", context, res.getWriter());
		}
	}
}