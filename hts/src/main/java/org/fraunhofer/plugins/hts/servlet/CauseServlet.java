package org.fraunhofer.plugins.hts.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fraunhofer.plugins.hts.db.Hazard_Causes;
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
    		Map<String, Object> context = Maps.newHashMap();
    		res.setContentType("text/html;charset=utf-8");
    		if("y".equals(req.getParameter("edit"))) {
    			Hazards currentHazard = hazardService.getHazardByID(req.getParameter("key"));
    			context.put("hazardNumber", currentHazard.getHazardNum());
    			context.put("hazardTitle", currentHazard.getTitle());
    			context.put("hazardID", currentHazard.getID());
      			context.put("hazard", currentHazard);
    			context.put("causes", hazardCauseService.getAllNonDeletedCausesWithinAHazard(currentHazard));
    			templateRenderer.render("templates/EditHazard.vm", context, res.getWriter());
    		}
    		else {
        		Hazards newestHazardReport = hazardService.getNewestHazardReport();
    			context.put("hazardNumber", newestHazardReport.getHazardNum());
    			context.put("hazardTitle", newestHazardReport.getTitle());
    			context.put("hazardID", newestHazardReport.getID());
    			context.put("causes", hazardCauseService.getAllNonDeletedCausesWithinAHazard(newestHazardReport));
    			templateRenderer.render("templates/HazardPage.vm", context, res.getWriter());
    		}
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
    	//TODO change once we can navigate from the newest hazard report to older ones
    	final Hazards currentHazard = hazardService.getHazardByID(req.getParameter("hazardID"));
    	
    	if("y".equals(req.getParameter("edit"))) {
    		String id = req.getParameter("key");
    		hazardCauseService.update(id, description, effects, owner, title);
    	}
    	else {
    		hazardCauseService.add(description, effects, owner, title, currentHazard);
    		res.sendRedirect(req.getContextPath() + "/plugins/servlet/causeform?edit=y&key=" + currentHazard.getID());
    		return;
    	}
    	res.sendRedirect(req.getContextPath() + "/plugins/servlet/causeform");
    }
    
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			Hazard_Causes causeToBeDeleted  = hazardCauseService.getHazardCauseByID(req.getParameter("key"));
			String reason = req.getParameter("reason");
			String respStr = "{ \"success\" : \"false\", error: \"Couldn't find hazard report\"}";
			
			if(causeToBeDeleted != null) {
				hazardCauseService.deleteCause(causeToBeDeleted, reason);
				respStr =  "{ \"success\" : \"true\" }";
			}
			
			res.setContentType("application/json;charset=utf-8");
			// Send the raw output string 
			res.getWriter().write(respStr);
		}
		else {
			res.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}

}