package org.fraunhofer.plugins.hts.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fraunhofer.plugins.hts.model.Hazard_Causes;
import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.model.Transfers;
import org.fraunhofer.plugins.hts.response.ResponseHelper;
import org.fraunhofer.plugins.hts.rest.model.CauseJSON;
import org.fraunhofer.plugins.hts.service.CauseService;
import org.fraunhofer.plugins.hts.service.HazardService;
import org.fraunhofer.plugins.hts.service.TransferService;

import com.atlassian.jira.component.ComponentAccessor;

//String respStr = "{ \"success\" : \"true\" }";
@Path("/hazard")
public class HazardRestService {
	private HazardService hazardService;
	private CauseService hazardCauseService;
	private TransferService transferService;

	public HazardRestService(HazardService hazardService, CauseService hazardCauseService, TransferService transferService) {
		this.hazardService = hazardService;
		this.hazardCauseService = hazardCauseService;
		this.transferService = transferService;
	}
	
	@GET
	@Path("cause/{hazardID}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllCausesBelongingToHazard(@PathParam("hazardID") int hazardID, @QueryParam("includeTransfers") boolean includeTransfers) {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			List<CauseJSON> causes = new ArrayList<CauseJSON>();
			for (Hazard_Causes cause : hazardCauseService
					.getAllNonDeletedCausesWithinHazard(hazardService.getHazardById(hazardID))) {
				if (cause.getTransfer() == 0) {
					causes.add(new CauseJSON(cause.getID(), cause.getCauseNumber(), cause.getTitle(), false, true,
							"CAUSE"));
				} else if(includeTransfers) {
					// Transferred Cause
					Transfers transfer = transferService.getTransferByID(cause.getTransfer());
					if (transfer.getTargetType().equals("CAUSE")) {
						// TODO This needs to check if the cause has been
						// moved too.
						Hazard_Causes targetCause = hazardCauseService.getHazardCauseByID(transfer.getTargetID());
						causes.add(new CauseJSON(cause.getID(), cause.getCauseNumber(), targetCause.getTitle(), true,
								true, "CAUSE"));
					} else if (transfer.getTargetType().equals("HAZARD")) {
						Hazards targetHazard = hazardService.getHazardById(transfer.getTargetID());
						causes.add(new CauseJSON(cause.getID(), cause.getCauseNumber(), targetHazard.getHazardTitle(),
								true, true, "CAUSE"));
					}

				}
			}
			return Response.ok(causes).build();
		} else {
			return ResponseHelper.notLoggedIn();
		}
	}
}
