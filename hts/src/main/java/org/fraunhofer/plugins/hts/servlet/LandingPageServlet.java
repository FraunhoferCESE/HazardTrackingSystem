package org.fraunhofer.plugins.hts.servlet;

import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

public class LandingPageServlet extends HttpServlet{
    private final HazardService hazardService;
    private final TemplateRenderer templateRenderer;
    
    public LandingPageServlet(HazardService hazardService, TemplateRenderer templateRenderer) {
    	this.hazardService = checkNotNull(hazardService);
    	this.templateRenderer = templateRenderer;
    	
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
    	Map<String, Object> context = Maps.newHashMap();
    	int numberOfReports = hazardService.all().size();
    	context.put("lastCreated", hazardService.all().get(numberOfReports - 1));
    	context.put("hazardReports", hazardService.all());
        resp.setContentType("text/html");
        templateRenderer.render("templates/LandingPage.vm", context, resp.getWriter());
    }

}