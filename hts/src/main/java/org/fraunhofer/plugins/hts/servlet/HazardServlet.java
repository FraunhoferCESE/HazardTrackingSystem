package org.fraunhofer.plugins.hts.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.atlassian.templaterenderer.TemplateRenderer;

import org.fraunhofer.plugins.hts.db.service.HazardService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import static com.google.common.base.Preconditions.*;

public final class HazardServlet extends HttpServlet {
	private final HazardService hazardService;
	private final TemplateRenderer templateRenderer;
	
	public HazardServlet(HazardService hazardService, TemplateRenderer templateRenderer) {
		this.hazardService = checkNotNull(hazardService);
		this.templateRenderer = templateRenderer;
	}

	// TODO remove the HTML code, fix the form and use the servlet to input to
	// the database
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		templateRenderer.render("templates/HazardForm.vm", res.getWriter());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		final String title = req.getParameter("task");
		final String description = req.getParameter("task");

		hazardService.add(title, description, "preparer", "hazardNum", new Date(), new Date(), new Date());

		res.sendRedirect(req.getContextPath() + "/plugins/servlet/hazardservlet");
	}
}