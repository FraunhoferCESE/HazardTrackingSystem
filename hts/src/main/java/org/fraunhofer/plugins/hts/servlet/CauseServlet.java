package org.fraunhofer.plugins.hts.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Payload;
import org.fraunhofer.plugins.hts.db.service.HazardCauseService;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.TransferService;
import org.fraunhofer.plugins.hts.db.service.MissionPayloadService;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

public class CauseServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final HazardCauseService hazardCauseService;
	private final HazardService hazardService;
	private final MissionPayloadService missionPayloadService;
	private final TemplateRenderer templateRenderer;

	public CauseServlet(HazardCauseService hazardCauseService, TemplateRenderer templateRenderer,
			HazardService hazardService, MissionPayloadService missionPayloadService) {
		this.hazardCauseService = checkNotNull(hazardCauseService);
		this.templateRenderer = checkNotNull(templateRenderer);
		this.hazardService = checkNotNull(hazardService);
		this.missionPayloadService = checkNotNull(missionPayloadService);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			Map<String, Object> context = Maps.newHashMap();
			res.setContentType("text/html;charset=utf-8");
			context.put("baseUrl", ComponentAccessor.getApplicationProperties().getString("jira.baseurl"));
			
			Hazards currentHazard = hazardService.getHazardByID(req.getParameter("key"));
			Mission_Payload currentPayload = currentHazard.getMissionPayload();
			
			List<Hazards> allHazardsBelongingToPayload = missionPayloadService.getAllHazardsWithinMission(String.valueOf(currentPayload.getID()));
			context.put("allHazardsBelongingToPayload", allHazardsBelongingToPayload);
			
			//Hazards[] allHazardsBelongingToPayload = currentPayload.getHazards();
			//context.put("allHazardsBelongingToPayload", allHazardsBelongingToPayload);
			
			if ("y".equals(req.getParameter("edit"))) {
				context.put("hazardNumber", currentHazard.getHazardNum());
				context.put("hazardTitle", currentHazard.getTitle());
				context.put("hazardID", currentHazard.getID());
				context.put("hazard", currentHazard);
				context.put("causes", hazardCauseService.getAllNonDeletedCausesWithinAHazard(currentHazard));
				context.put("transfers", hazardCauseService.getAllTransferredCauses(currentHazard));
				templateRenderer.render("templates/EditHazard.vm", context, res.getWriter());
			}
			// This else is outdated, will no longer be used after issue #169 has been completed
//			else {
//				Hazards newestHazardReport = hazardService.getNewestHazardReport();
//				context.put("hazardNumber", newestHazardReport.getHazardNum());
//				context.put("hazardTitle", newestHazardReport.getTitle());
//				context.put("hazardID", newestHazardReport.getID());
//				context.put("causes", hazardCauseService.getAllNonDeletedCausesWithinAHazard(newestHazardReport));
//				context.put("transfers", hazardCauseService.getAllTransferredCauses(newestHazardReport));
//				templateRenderer.render("templates/HazardPage.vm", context, res.getWriter());
//			}
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
		final Hazards currentHazard = hazardService.getHazardByID(req.getParameter("hazardID"));

		if ("y".equals(req.getParameter("edit"))) {
			String id = req.getParameter("key");
			hazardCauseService.update(id, description, effects, owner, title);
		} else if ("y".equals(req.getParameter("transfer"))) {
			final String transferComment = req.getParameter("transferReason");
			final String hazardID = req.getParameter("hazardList");
			final String causeID = req.getParameter("causeList");
			if (causeID.isEmpty() || causeID == null) {
				Hazards targetHazard = hazardService.getHazardByID(hazardID);
				if (!checkIfInternalHazardTransfer(currentHazard, targetHazard)) {
					hazardCauseService.addHazardTransfer(transferComment, targetHazard.getID(), targetHazard.getTitle(), currentHazard);
				}
			} else {
				Hazard_Causes targetCause = hazardCauseService.getHazardCauseByID(causeID);
				if (!checkIfInternalCauseTransfer(currentHazard, targetCause)) {
					hazardCauseService.addCauseTransfer(transferComment, targetCause.getID(), targetCause.getTitle(), currentHazard);
				}
			}
		} else {
			hazardCauseService.add(description, effects, owner, title, currentHazard);
			res.sendRedirect(req.getContextPath() + "/plugins/servlet/causeform?edit=y&key=" + currentHazard.getID());
			return;
		}
		res.sendRedirect(req.getContextPath() + "/plugins/servlet/causeform");
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			Hazard_Causes causeToBeDeleted = hazardCauseService.getHazardCauseByID(req.getParameter("key"));
			String reason = req.getParameter("reason");
			String respStr = "{ \"success\" : \"false\", error: \"Couldn't find hazard report\"}";

			if (causeToBeDeleted != null) {
				hazardCauseService.deleteCause(causeToBeDeleted, reason);
				respStr = "{ \"success\" : \"true\" }";
			}

			res.setContentType("application/json;charset=utf-8");
			// Send the raw output string
			res.getWriter().write(respStr);
		} else {
			res.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}
	
	private Boolean checkIfInternalHazardTransfer(Hazards originHazard, Hazards targetHazard) {
		if (originHazard.getID() == targetHazard.getID()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private Boolean checkIfInternalCauseTransfer(Hazards targetHazard, Hazard_Causes targetCause) {
		List<Hazard_Causes> allCausesBelongingToHazard = hazardCauseService.getAllCausesWithinAHazard(targetHazard);
		for (Hazard_Causes cause : allCausesBelongingToHazard) {
			if (cause.getID() == targetCause.getID()) {
				return true;
			}
		}
		return false;
	}

}