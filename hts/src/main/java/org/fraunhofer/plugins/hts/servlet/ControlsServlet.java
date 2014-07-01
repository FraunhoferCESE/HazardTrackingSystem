package org.fraunhofer.plugins.hts.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.common.base.Preconditions.checkNotNull;

import org.fraunhofer.plugins.hts.db.ControlGroups;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.service.ControlGroupsService;
import org.fraunhofer.plugins.hts.db.service.HazardCauseService;
import org.fraunhofer.plugins.hts.db.service.HazardControlService;
import org.fraunhofer.plugins.hts.db.service.HazardService;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.Map;

public class ControlsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final TemplateRenderer templateRenderer;
	private final HazardService hazardService;
	private final HazardControlService hazardControlService;
	private final ControlGroupsService controlGroupsService;
	private final HazardCauseService hazardCauseService;
	
	public ControlsServlet(TemplateRenderer templateRenderer, 
			HazardService hazardService, 
			HazardControlService hazardControlService, 
			ControlGroupsService controlGroupsService,
			HazardCauseService hazardCauseService) {
		this.templateRenderer = checkNotNull(templateRenderer);
		this.hazardService = checkNotNull(hazardService);
		this.hazardControlService = checkNotNull(hazardControlService);
		this.controlGroupsService = checkNotNull(controlGroupsService);
		this.hazardCauseService = checkNotNull(hazardCauseService);
	}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
    		Map<String, Object> context = Maps.newHashMap();
			Hazards newestHazardReport = hazardService.getNewestHazardReport();
			// Content for upper part of page; hazard info and list of previously defined controls
			context.put("hazardNumber", newestHazardReport.getHazardNum());
			context.put("hazardTitle", newestHazardReport.getTitle());
			context.put("hazardID", newestHazardReport.getID());
			context.put("hazardControls", hazardControlService.getAllControlsWithinAHazard(newestHazardReport));
			// Content for lower part of page; creating a new control
			context.put("hazardCauses", hazardCauseService.getAllNonDeletedCausesWithinAHazard(newestHazardReport));
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
    	final Hazards currentHazard = hazardService.getHazardByID(req.getParameter("hazardID"));
    	final String description = req.getParameter("controlDescription");
    	final ControlGroups controlGroup = controlGroupsService.getControlGroupServicebyID(req.getParameter("controlGroup"));
    	final Hazard_Causes[] causes = hazardCauseService.getHazardCausesByID(changeStringArray(req.getParameterValues("controlCauses")));
    	hazardControlService.add(currentHazard, description, controlGroup, causes);
    	res.sendRedirect(req.getContextPath() + "/plugins/servlet/controlform");
    }
    
	private Integer[] changeStringArray(String[] array) {
		if (array == null) {
			return null;
		} else {
			Integer[] intArray = new Integer[array.length];
			for (int i = 0; i < array.length; i++) {
				intArray[i] = Integer.parseInt(array[i]);
			}
			return intArray;
		}
	}

}