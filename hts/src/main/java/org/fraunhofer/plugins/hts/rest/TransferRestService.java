package org.fraunhofer.plugins.hts.rest;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//import org.fraunhofer.plugins.hts.rest.datatype.transferRiskValueJSON;

import org.fraunhofer.plugins.hts.model.Hazard_Causes;
import org.fraunhofer.plugins.hts.model.Hazard_Controls;
import org.fraunhofer.plugins.hts.model.Transfers;
import org.fraunhofer.plugins.hts.model.Transfers.TransferType;
import org.fraunhofer.plugins.hts.response.ResponseHelper;
import org.fraunhofer.plugins.hts.rest.model.CauseJSON;
import org.fraunhofer.plugins.hts.rest.model.ControlJSON;
import org.fraunhofer.plugins.hts.rest.model.TransferJSON;
import org.fraunhofer.plugins.hts.service.CauseService;
import org.fraunhofer.plugins.hts.service.ControlService;
import org.fraunhofer.plugins.hts.service.TransferService;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.extras.common.log.Logger.Log;
import com.atlassian.jira.component.ComponentAccessor;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

@Path("/transfer")
public class TransferRestService {
	private static Log logger = Logger.getInstance(TransferRestService.class);

	private TransferService transferService;
	private ControlService controlService;
	private CauseService causeService;
	CauseService hazardCauseService;

	public TransferRestService(TransferService transferService, CauseService causeService,
			ControlService controlService) {
		this.transferService = transferService;
		this.causeService = causeService;
		this.controlService = controlService;
	}

	@GET
	@Path("findOrigins")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getIncomingTransfersForElement(@QueryParam("type") String type,
			@QueryParam("elementId") int elementId) {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			checkArgument(type != null && TransferType.valueOf(type.toUpperCase()) != null && elementId > 0);

			List<Hazard_Causes> causes = new ArrayList<Hazard_Causes>();
			List<Hazard_Controls> controls = new ArrayList<Hazard_Controls>();

			List<Transfers> originsForId = transferService.getOriginsForId(type.toUpperCase(), elementId);

			for (Transfers transfer : originsForId) {
				switch (TransferType.valueOf(transfer.getOriginType())) {
				case CAUSE:
					Hazard_Causes cause = causeService.getHazardCauseByID(transfer.getOriginID());
					if (cause == null)
						logger.warn("Transfer origin cause could not be found by id.");
					else
						causes.add(cause);
					break;
				case CONTROL:
					Hazard_Controls control = controlService.getHazardControlByID(transfer.getOriginID());
					if (control == null)
						logger.warn("Transfer origin control could not be found by id.");
					else
						controls.add(control);
					break;
				default:
					logger.warn("Request for incoming transfer origins: unknown origin type");
				}
			}

			List<CauseJSON> causesJSON = Lists.transform(causes, new Function<Hazard_Causes, CauseJSON>() {
				public CauseJSON apply(Hazard_Causes c) {
					return new CauseJSON(c, c.getHazards()[0]);
				}
			});

			List<ControlJSON> controlsJSON = Lists.transform(controls, new Function<Hazard_Controls, ControlJSON>() {
				public ControlJSON apply(Hazard_Controls c) {
					return new ControlJSON(c, c.getHazard()[0]);
				}
			});

			return Response.ok(new TransferJSON(causesJSON, controlsJSON)).build();
		} else {
			return ResponseHelper.notLoggedIn();
		}
	}
}