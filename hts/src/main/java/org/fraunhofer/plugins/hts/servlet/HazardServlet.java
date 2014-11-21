package org.fraunhofer.plugins.hts.servlet;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Phase;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.Subsystems;
import org.fraunhofer.plugins.hts.db.service.HazardGroupService;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.MissionPhaseService;
import org.fraunhofer.plugins.hts.db.service.ReviewPhaseService;
import org.fraunhofer.plugins.hts.db.service.SubsystemService;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;

public final class HazardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final HazardService hazardService;
	private final HazardGroupService hazardGroupService;
	private final SubsystemService subsystemService;
	private final ReviewPhaseService reviewPhaseService;
	private final MissionPhaseService missionPhaseService;
	private final TemplateRenderer templateRenderer;

	public HazardServlet(HazardService hazardService, HazardGroupService hazardGroupService,
			TemplateRenderer templateRenderer, SubsystemService subsystemService,
			ReviewPhaseService reviewPhaseService, MissionPhaseService missionPhaseService) {
		this.hazardService = checkNotNull(hazardService);
		this.hazardGroupService = checkNotNull(hazardGroupService);
		this.subsystemService = checkNotNull(subsystemService);
		this.reviewPhaseService = checkNotNull(reviewPhaseService);
		this.missionPhaseService = checkNotNull(missionPhaseService);
		this.templateRenderer = checkNotNull(templateRenderer);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {			
			Map<String, Object> context = Maps.newHashMap();
			boolean contains = req.getParameterMap().containsKey("id");
			if (contains == true) {
				String hazardIDStr = req.getParameter("id");
				// TODO: Parsing could fail, will throw NumberFormatException.
				int hazardID =  Integer.parseInt(hazardIDStr);
				// TODO: CurrentHazard could be returned as null.
				Hazards currentHazard = hazardService.getHazardByID(hazardID);
				context.put("hazard", currentHazard);
				context.put("nonAssociatedSubsystems", subsystemService.getRemaining(currentHazard.getSubsystems()));
				context.put("hazardPreparer", hazardService.getHazardPreparerInformation(currentHazard));
				context.put("reviewPhases", reviewPhaseService.all());
				context.put("nonAssociatedMissionPhases", missionPhaseService.getRemaining(currentHazard.getMissionPhases()));
				context.put("nonAssociatedHazardGroups", hazardGroupService.getRemaining(currentHazard.getHazardGroups()));
				context.put("initiationDate", removeTimeFromDate(currentHazard.getInitiationDate()));
				context.put("completionDate", removeTimeFromDate(currentHazard.getCompletionDate()));
				resp.setContentType("text/html;charset=utf-8");
				templateRenderer.render("templates/hazard-page.vm", context, resp.getWriter());
			} else {
				// TODO: Missing ID parameter.
			}
		} else {
			resp.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			String hazardIDStr = req.getParameter("hazardID");
			int hazardID = Integer.parseInt(hazardIDStr);
			String hazardNumber = req.getParameter("hazardNumber");
			String version = req.getParameter("hazardVersionNumber");
			String hazardTitle = req.getParameter("hazardTitle");
			Subsystems[] subsystems = subsystemService.getSubsystemsByID(changeStringArray(req.getParameterValues("hazardSubsystem")));
			Review_Phases reviewPhase = reviewPhaseService.getReviewPhaseByID(req.getParameter("hazardReviewPhase"));
			Mission_Phase[] missionPhases = missionPhaseService.getMissionPhasesByID(changeStringArray(req.getParameterValues("hazardPhase")));
			Hazard_Group[] hazardGroups = hazardGroupService.getHazardGroupsByID(changeStringArray(req.getParameterValues("hazardGroup")));
			String safetyRequirements = req.getParameter("hazardSafetyRequirements");
			String description = req.getParameter("hazardDescription");
			String justification = req.getParameter("hazardJustification");
			String openWork = req.getParameter("hazardOpenWork");
			Date initiation = changeToDate(req.getParameter("hazardInitation"));
			Date completion = changeToDate(req.getParameter("hazardCompletion"));
			
			hazardService.update(hazardID, hazardNumber, version, hazardTitle, subsystems, reviewPhase, missionPhases, hazardGroups,
					safetyRequirements, description, justification, openWork, initiation, completion);
			
			JSONObject json = new JSONObject();
			createJson(json, "updateSuccess", true);
			createJson(json, "errorMessage", "none");
			resp.setContentType("application/json");
			resp.getWriter().println(json);
		} else {
			resp.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}
	
	private String removeTimeFromDate(Date date) {
		if (date != null) {
			return date.toString().substring(0, 10);
		} else {
			return null;
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

	private JSONObject createJson(JSONObject json, String key, Object value) {
		try {
			json.put(key, value);
		} catch (JSONException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return json;
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