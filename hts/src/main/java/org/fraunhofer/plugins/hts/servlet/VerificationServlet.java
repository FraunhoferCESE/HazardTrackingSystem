package org.fraunhofer.plugins.hts.servlet;

import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class VerificationServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(VerificationServlet.class);
	private final TemplateRenderer templateRenderer;
	private final HazardService hazardService;
	
	public VerificationServlet(TemplateRenderer templateRenderer, HazardService hazardService) {
		this.templateRenderer = templateRenderer;
		this.hazardService = checkNotNull(hazardService);
	}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
    		Map<String, Object> context = Maps.newHashMap();
    		resp.setContentType("text/html;charset=utf-8");
    		context.put("baseUrl", ComponentAccessor.getApplicationProperties().getString("jira.baseurl"));
    		if ("y".equals(req.getParameter("edit"))) {
    			Hazards currentHazard = hazardService.getHazardByID(req.getParameter("key"));
				context.put("hazardNumber", currentHazard.getHazardNum());
				context.put("hazardTitle", currentHazard.getTitle());
				context.put("hazardID", currentHazard.getID());
				context.put("hazard", currentHazard);
				templateRenderer.render("templates/EditHazard.vm", context, resp.getWriter());
    		}
    		else {
    			templateRenderer.render("templates/HazardPage.vm", context, resp.getWriter());
    		}
    	}
    	else {
    		resp.sendRedirect(req.getContextPath() + "/login.jsp");
    	}
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	if ("y".equals(req.getParameter("edit"))) {
    		res.sendRedirect(req.getContextPath() + "/plugins/servlet/verificationform");
    	}
    	else {
    		final Hazards currentHazard = hazardService.getHazardByID(req.getParameter("hazardID"));
    		res.sendRedirect(req.getContextPath() + "/plugins/servlet/causeform?edit=y&key=" + currentHazard.getID());
    	}
    }

}