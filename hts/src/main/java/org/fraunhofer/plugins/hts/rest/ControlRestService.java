package org.fraunhofer.plugins.hts.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fraunhofer.plugins.hts.model.Hazard_Controls;
import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.model.Verifications;
import org.fraunhofer.plugins.hts.response.ResponseHelper;
import org.fraunhofer.plugins.hts.rest.model.VerificationJSON;
import org.fraunhofer.plugins.hts.service.ControlService;

import com.atlassian.jira.component.ComponentAccessor;
import com.google.common.collect.Lists;

//String respStr = "{ \"success\" : \"true\" }";
@Path("/control")
public class ControlRestService {
	private ControlService controlService;

	public ControlRestService(ControlService controlService) {
		this.controlService = controlService;
	}

	@GET
	@Path("/{controlID}/verifications")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllVerificationsBelongingToControl(@PathParam("controlID") int controlID) {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			Hazard_Controls control = controlService.getHazardControlByID(controlID);
			if (control == null) {
				return ResponseHelper.badRequest("No control by that ID found.");
			} else {
				List<VerificationJSON> associatedVerifications = Lists.newArrayList(); 
				Verifications[] verifications = control.getVerifications();
				Hazards hazard = control.getHazard()[0];
				if(verifications != null) {
					for (Verifications verification : verifications) {
						associatedVerifications.add(new VerificationJSON(verification, hazard));
					}
				}
				return Response.ok(associatedVerifications).build();
			}
				
		} else {
			return ResponseHelper.notLoggedIn();
		}
	}
}
