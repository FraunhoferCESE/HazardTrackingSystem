package org.fraunhofer.plugins.hts.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.common.base.Preconditions.checkNotNull;

import org.fraunhofer.plugins.hts.db.ControlGroups;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.service.ControlGroupsService;
import org.fraunhofer.plugins.hts.db.service.HazardControlService;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.Map;

public class ControlsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final TemplateRenderer templateRenderer;
	private final HazardControlService hazardControlService;
	private final ControlGroupsService controlGroupsService;
	
	public ControlsServlet(TemplateRenderer templateRenderer, 
			HazardControlService hazardControlService, 
			ControlGroupsService controlGroupsService) {
		this.templateRenderer = checkNotNull(templateRenderer);
		this.hazardControlService = checkNotNull(hazardControlService);
		this.controlGroupsService = checkNotNull(controlGroupsService);
	}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
    		Map<String, Object> context = Maps.newHashMap();
    		context.put("controlGroups", controlGroupsService.all());
    		
        	resp.setContentType("text/html;charset=utf-8");
        	templateRenderer.render("templates/HazardPage.vm", context, resp.getWriter());
    	}
    	else {
    		resp.sendRedirect(req.getContextPath() + "/login.jsp");
    	}
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	final String description = req.getParameter("controlDescription");
    	final ControlGroups controlGroup = controlGroupsService.getControlGroupServicebyID(req.getParameter("controlGroup"));
    	
    	hazardControlService.add(description, controlGroup);
    	res.sendRedirect(req.getContextPath() + "/plugins/servlet/controlform");
    }

}