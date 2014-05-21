package org.fraunhofer.plugins.hts.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.service.HazardCauseService;
import org.fraunhofer.plugins.hts.db.service.HazardService;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

public class CauseServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private final HazardCauseService hazardCauseService;
	private final HazardService hazardService;
	private final TemplateRenderer templateRenderer; 
	
	public CauseServlet(HazardCauseService hazardCauseService, TemplateRenderer templateRenderer, HazardService hazardService) {
		this.hazardCauseService = checkNotNull(hazardCauseService);
		this.templateRenderer = checkNotNull(templateRenderer);
		this.hazardService = checkNotNull(hazardService);
	}
	
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			res.setContentType("text/html;charset=utf-8");
			Map<String, Object> context = Maps.newHashMap();
			context.put("newestHazard", hazardService.getNewestHazardReport());
			context.put("causes", hazardCauseService.all());
			templateRenderer.render("templates/HazardPage.vm", context, res.getWriter());
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
    	final String causeNumber = "Cause 2";
    	//TODO change once we can navigate from the newest hazard report to older ones
    	final Hazards hazard = hazardService.getNewestHazardReport();
    	
    	if("y".equals(req.getParameter("edit"))) {
    		String id = req.getParameter("key");
    		hazardCauseService.update(id, description, effects, owner, title);
    	}
    	else {
    		hazardCauseService.add(causeNumber, description, effects, owner, title, hazard);
    	}
    	res.sendRedirect(req.getContextPath() + "/plugins/servlet/causeform");
    }

}