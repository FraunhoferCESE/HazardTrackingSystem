package org.fraunhofer.plugins.hts.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fraunhofer.plugins.hts.db.service.HazardCauseService;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

public class CauseServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private final HazardCauseService hazardCauseService;
	private final TemplateRenderer templateRenderer; 
	
	public CauseServlet(HazardCauseService hazardCauseService, TemplateRenderer templateRenderer) {
		this.hazardCauseService = checkNotNull(hazardCauseService);
		this.templateRenderer = checkNotNull(templateRenderer);
	}
	
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {

			res.setContentType("text/html;charset=utf-8");
			templateRenderer.render("templates/HazardPage.vm", res.getWriter());

		} 
		else {
			res.sendRedirect(req.getContextPath() + "/login.jsp");
		}
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	final String title = req.getParameter("causeTitle");
    	final String owner = req.getParameter("causeOwner");
    	final String effects = req.getParameter("causeEffects");
    	final String description = req.getParameter("causeDescription");
    	final String causeID = "1";
    	
    	hazardCauseService.add(causeID, description, effects, owner, title);
    	
    }

}