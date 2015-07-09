package org.fraunhofer.plugins.hts.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fraunhofer.plugins.hts.model.Hazard_Causes;
import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.model.Transfers;
import org.fraunhofer.plugins.hts.rest.model.CauseJSON;
import org.fraunhofer.plugins.hts.service.HazardCauseService;
import org.fraunhofer.plugins.hts.service.HazardService;
import org.fraunhofer.plugins.hts.service.TransferService;
import org.fraunhofer.plugins.hts.view.model.HazardMinimalJSON;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;

//String respStr = "{ \"success\" : \"true\" }";
@Path("/hazard")
public class HazardRestService {
	private HazardService hazardService;
	private HazardCauseService hazardCauseService;
	private TransferService transferService;

	public HazardRestService(HazardService hazardService, HazardCauseService hazardCauseService) {
		this.hazardService = hazardService;
		this.hazardCauseService = hazardCauseService;
	}

	@GET
	@Path("all")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllHazards() {
		JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
		if (jiraAuthenticationContext.isLoggedInUser()) {
			List<HazardMinimalJSON> hazards = getUserHazardsMinimalJson(jiraAuthenticationContext.getUser());
			return Response.ok(hazards).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
					.build();
		}
	}

	public List<HazardMinimalJSON> getUserHazardsMinimalJson(ApplicationUser user) {
		List<Hazards> allHazards = hazardService.getUserHazards(user);
		List<HazardMinimalJSON> allHazardsMinimal = new ArrayList<HazardMinimalJSON>();
		for (Hazards hazard : allHazards) {
			Project jiraProject = hazardService.getHazardProject(hazard);
			Issue jiraSubtask = hazardService.getHazardSubTask(hazard);
			String baseURL = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");

			allHazardsMinimal.add(new HazardMinimalJSON(hazard.getID(), hazard.getHazardTitle(),
					hazard.getHazardNumber(), jiraSubtask.getSummary(),
					baseURL + "/browse/" + jiraProject.getKey() + "-" + jiraSubtask.getNumber(), jiraProject.getName(),
					baseURL + "/browse/" + jiraProject.getKey(), hazard.getRevisionDate().toString()));
		}
		return allHazardsMinimal;
	}

	@GET
	@Path("cause/{hazardID}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllCausesBelongingToHazard(@PathParam("hazardID") int hazardID) {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			List<CauseJSON> causes = new ArrayList<CauseJSON>();
			for (Hazard_Causes cause : hazardCauseService
					.getAllNonDeletedCausesWithinHazard(hazardService.getHazardByID(hazardID))) {
				if (cause.getTransfer() == 0) {
					causes.add(new CauseJSON(cause.getID(), cause.getCauseNumber(), cause.getTitle(), false, true,
							"CAUSE"));
				} else {
					// Transferred Cause
					Transfers transfer = transferService.getTransferByID(cause.getTransfer());
					if (transfer.getTargetType().equals("CAUSE")) {
						// TODO This needs to check if the cause has been
						// moved too.
						Hazard_Causes targetCause = hazardCauseService.getHazardCauseByID(transfer.getTargetID());
						causes.add(new CauseJSON(cause.getID(), cause.getCauseNumber(), targetCause.getTitle(), true,
								true, "CAUSE"));
					} else if (transfer.getTargetType().equals("HAZARD")) {
						Hazards targetHazard = hazardService.getHazardByID(transfer.getTargetID());
						causes.add(new CauseJSON(cause.getID(), cause.getCauseNumber(), targetHazard.getHazardTitle(),
								true, true, "CAUSE"));
					}

				}
			}
			return Response.ok(causes).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
					.build();
		}
	}
}
