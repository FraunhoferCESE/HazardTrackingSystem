package org.fraunhofer.plugins.hts.servlet;

import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.VerificationStatus;
import org.fraunhofer.plugins.hts.db.VerificationType;
import org.fraunhofer.plugins.hts.db.Verifications;
import org.fraunhofer.plugins.hts.db.service.HazardControlService;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.VerificationService;
import org.fraunhofer.plugins.hts.db.service.VerificationStatusService;
import org.fraunhofer.plugins.hts.db.service.VerificationTypeService;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class VerificationServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(VerificationServlet.class);
	private final TemplateRenderer templateRenderer;
	private final HazardService hazardService;
	private final VerificationTypeService verificationTypeService;
	private final VerificationStatusService verificationStatusService;
	private final VerificationService verificationService;
	private final HazardControlService hazardControlService;
	
	public VerificationServlet(TemplateRenderer templateRenderer, HazardService hazardService,
			VerificationTypeService verificationTypeService, VerificationStatusService verificationStatusService,
			VerificationService verificationService, HazardControlService hazardControlService) {
		this.templateRenderer = templateRenderer;
		this.hazardService = hazardService;
		this.verificationTypeService = verificationTypeService;
		this.verificationStatusService = verificationStatusService;
		this.verificationService = verificationService;
		this.hazardControlService = hazardControlService;
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
				context.put("verificationTypes", verificationTypeService.all());
				context.put("allVerifications", verificationService.getAllVerificationsWithinAHazard(currentHazard));
				context.put("verificationStatuses", verificationStatusService.all());
				context.put("allControls", hazardControlService.getAllNonDeletedControlsWithinAHazard(currentHazard));
				templateRenderer.render("templates/EditHazard.vm", context, resp.getWriter());
			} else {
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
    		final Verifications verificationToEdit = verificationService.getVerificationByID(req.getParameter("verificationID"));
    		final String description = req.getParameter("verificationDescriptionEdit");
    		final String responsibleParty = req.getParameter("verificationRespPartyEdit");
    		final Date estCompletionDate = changeToDate(req.getParameter("verificationComplDateEdit"));
        	final VerificationStatus verificationStatus = verificationStatusService.getVerificationStatusByID(req.getParameter("verificationStatusEdit"));
        	final Hazard_Controls[] controls = hazardControlService.getHazardControlsByID(changeStringArray(req.getParameterValues("verificationControlsEdit")));
        	
        	final VerificationType verificationType;
    		if (req.getParameter("verificationTypeEdit") != "") {
    			verificationType = verificationTypeService.getVerificationTypeByID(req.getParameter("verificationTypeEdit"));
    		}
    		else {
    			verificationType = null;
    		}

    		verificationService.update(verificationToEdit, description, verificationType, responsibleParty, estCompletionDate, verificationStatus, controls);
    		res.sendRedirect(req.getContextPath() + "/plugins/servlet/verificationform");
    	}
    	else {
    		final Hazards currentHazard = hazardService.getHazardByID(req.getParameter("hazardID"));
    		final String description = req.getParameter("verificationDescriptionNew");
        	final String responsibleParty = req.getParameter("verificationRespPartyNew");
    		final Date estCompletionDate = changeToDate(req.getParameter("verificationComplDateNew"));
        	final VerificationStatus verificationStatus = verificationStatusService.getVerificationStatusByID(req.getParameter("verificationStatusNew"));
        	final Hazard_Controls[] controls = hazardControlService.getHazardControlsByID(changeStringArray(req.getParameterValues("verificationControlsNew")));
        	
        	final VerificationType verificationType;
    		if (req.getParameter("verificationTypeNew") != "") {
    			verificationType = verificationTypeService.getVerificationTypeByID(req.getParameter("verificationTypeNew"));
    		}
    		else {
    			verificationType = null;
    		}

    		verificationService.add(currentHazard, description, verificationType, responsibleParty, estCompletionDate, verificationStatus, controls);
    		res.sendRedirect(req.getContextPath() + "/plugins/servlet/verificationform?edit=y&key=" + currentHazard.getID());
    	}
    }
    
	private Date changeToDate(String date) {
		if (date != null && !date.isEmpty()) {
			SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yyyy");
			try {
				String reformatted = newFormat.format(oldFormat.parse(date));
				Date converted = newFormat.parse(reformatted);
				return converted;
			} catch (ParseException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return null;
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