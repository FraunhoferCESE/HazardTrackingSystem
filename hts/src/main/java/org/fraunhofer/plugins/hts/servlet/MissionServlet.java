package org.fraunhofer.plugins.hts.servlet;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.service.HazardService;
import org.fraunhofer.plugins.hts.view.model.HazardMinimal;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.templaterenderer.TemplateRenderer;
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

			List<Hazards> userHazards = hazardService.getUserHazards(jiraAuthenticationContext.getUser());

			ProjectManager projectManager = ComponentAccessor.getProjectManager();
			Map<Long, Project> userProjects = Maps.newHashMap();
			String missionId = req.getParameter("missionId");
			if (missionId != null) {
				try {
					Project project = projectManager.getProjectObj(Long.parseLong(missionId));
					final long projectId = project.getId();
					if (project != null
							&& hazardService.hasHazardPermission(projectId, jiraAuthenticationContext.getUser())) {
						userProjects.put(projectId, project);
						// Not ideal, but best approach given the current design
						userHazards.removeIf(new Predicate<Hazards>() {
							public boolean test(Hazards t) {
								return t.getProjectID() == projectId;
							}
						});
					}
				} catch (NumberFormatException e) {
				}
			} else {
				for (Hazards hazard : userHazards) {
					if (userProjects.get(hazard.getProjectID()) == null) {
						Project project = projectManager.getProjectObj(hazard.getProjectID());
						userProjects.put(hazard.getProjectID(), project);
					}
				}
			}

			List<HazardMinimal> hazards = new ArrayList<HazardMinimal>();
			IssueManager issueManager = ComponentAccessor.getIssueManager();

			for (Hazards hazard : userHazards) {
				hazards.add(HazardMinimal.create(hazard, userProjects.get(hazard.getProjectID()),
						issueManager.getIssueObject(hazard.getIssueID())));
			}
			
			context.put("hazards", hazards);
			context.put("missions", userProjects);
			context.put("dateFormatter", dateTimeFormatter);
			res.setContentType("text/html");
			templateRenderer.render("templates/mission-page.vm", context, res.getWriter());
		}
	}
}