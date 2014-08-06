package org.fraunhofer.plugins.hts.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	public VerificationServlet(TemplateRenderer templateRenderer) {
		this.templateRenderer = templateRenderer;
	}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
    		Map<String, Object> context = Maps.newHashMap();
    		resp.setContentType("text/html;charset=utf-8");
    		context.put("baseUrl", ComponentAccessor.getApplicationProperties().getString("jira.baseurl"));
    		templateRenderer.render("templates/EditHazard.vm", context, resp.getWriter());
    	}
    }

}