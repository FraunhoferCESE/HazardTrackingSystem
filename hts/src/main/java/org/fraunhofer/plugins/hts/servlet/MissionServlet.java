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
import com.atlassian.jira.user.ApplicationUser;
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
		ApplicationUser user = jiraAuthenticationContext.getUser();
		if (user == null) {
			res.sendRedirect(req.getContextPath() + "/login.jsp");
		}
		else {
			Map<String, Object> context = Maps.newHashMap();
			List<HazardMinimal> hazards = new ArrayList<HazardMinimal>();
			Map<Long, Project> userProjects = Maps.newHashMap();
			
			// Get all hazards accessible by the user
			List<Hazards> userHazards = hazardService.getUserHazards(user);
			
			// Extract the unique projects from the available hazards
			ProjectManager projectManager = ComponentAccessor.getProjectManager();			
			for (Hazards hazard : userHazards) {
				if (userProjects.get(hazard.getProjectID()) == null) {
					userProjects.put(hazard.getProjectID(), projectManager.getProjectObj(hazard.getProjectID()));
				}
			}	
				
			// If the user specified a valid project, remove all others proejcts' hazards from the hazard list.
			Project project = null;
			String missionId = req.getParameter("missionId");
			if (missionId != null) {
				try {
					project = projectManager.getProjectObj(Long.parseLong(missionId));
					if (project != null) {
						// Not ideal, but best approach given the current design
						final long projectID = project.getId();
						userHazards.removeIf(new Predicate<Hazards>() {
							public boolean test(Hazards t) {
								return t.getProjectID() != projectID;
							}
						});
					}
				} catch (NumberFormatException e) {
				}
			}
			
			
			IssueManager issueManager = ComponentAccessor.getIssueManager();
			for (Hazards hazard : userHazards) {
				hazards.add(HazardMinimal.create(hazard, userProjects.get(hazard.getProjectID()),
						issueManager.getIssueObject(hazard.getIssueID())));
			}
			
			context.put("hazards", hazards);
			context.put("selectedMission", project == null ? null : project.getId());
			context.put("missions", Lists.newArrayList(userProjects.values()));
			context.put("dateFormatter", dateTimeFormatter);
			res.setContentType("text/html");
			templateRenderer.render("templates/mission-page.vm", context, res.getWriter());
		}
	}
}