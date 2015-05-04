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

import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Transfers;
import org.fraunhofer.plugins.hts.db.Transfers.TransferType;
import org.fraunhofer.plugins.hts.db.service.HazardCauseService;
import org.fraunhofer.plugins.hts.db.service.HazardControlService;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.TransferService;
import org.fraunhofer.plugins.hts.rest.datatype.CauseJSON;
import org.fraunhofer.plugins.hts.rest.datatype.ControlJSON;
import org.fraunhofer.plugins.hts.rest.datatype.TransferJSON;
//import org.fraunhofer.plugins.hts.rest.datatype.transferRiskValueJSON;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.extras.common.log.Logger.Log;
import com.atlassian.jira.component.ComponentAccessor;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

//String respStr = "{ \"success\" : \"true\" }";
@Path("/transfer")
public class TransferRestService {
	private static Log logger = Logger.getInstance(TransferRestService.class);

	private TransferService transferService;
	private HazardControlService controlService;
	private HazardCauseService causeService;
	private HazardService hazardService;
	HazardCauseService hazardCauseService;

	public TransferRestService(TransferService transferService,
			HazardCauseService causeService, HazardControlService controlService) {
		this.transferService = transferService;
		this.causeService = causeService;
		this.controlService = controlService;
	}

	@GET
	@Path("findOrigins")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getIncomingTransfersForElement(
			@QueryParam("type") String type,
			@QueryParam("elementId") int elementId) {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			checkArgument(type != null
					&& TransferType.valueOf(type.toUpperCase()) != null
					&& elementId > 0);

			List<Hazard_Causes> causes = new ArrayList<Hazard_Causes>();
			List<Hazard_Controls> controls = new ArrayList<Hazard_Controls>();

			List<Transfers> originsForId = transferService.getOriginsForId(
					type.toUpperCase(), elementId);

			for (Transfers transfer : originsForId) {
				switch (TransferType.valueOf(transfer.getOriginType())) {
				case CAUSE:
					Hazard_Causes cause = causeService
							.getHazardCauseByID(transfer.getOriginID());
					if (cause == null)
						logger.warn("Transfer origin cause could not be found by id.");
					else
						causes.add(cause);
					break;
				case CONTROL:
					Hazard_Controls control = controlService
							.getHazardControlByID(transfer.getOriginID());
					if (control == null)
						logger.warn("Transfer origin control could not be found by id.");
					else
						controls.add(control);
					break;
				default:
					logger.warn("Request for incoming transfer origins: unknown origin type");
				}
			}

			List<CauseJSON> causesJSON = Lists.transform(causes,
					new Function<Hazard_Causes, CauseJSON>() {
						public CauseJSON apply(Hazard_Causes c) {
							return new CauseJSON(c, c.getHazards()[0]);
						}
					});

			List<ControlJSON> controlsJSON = Lists.transform(controls,
					new Function<Hazard_Controls, ControlJSON>() {
						public ControlJSON apply(Hazard_Controls c) {
							return new ControlJSON(c, c.getHazard()[0]);
						}
					});

			return Response.ok(new TransferJSON(causesJSON, controlsJSON))
					.build();
		} else {
			return Response.status(Response.Status.FORBIDDEN)
					.entity(new HazardResourceModel("User is not logged in"))
					.build();
		}
	}

//	@GET
//	@Path("findTransferValues")
//	@Produces({ MediaType.APPLICATION_JSON })
//	public Response getTransferTargetRiskValues(@QueryParam("transferId") String transferId) {
//		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
//			// checkArgument(type != null &&
//
//			// Object getTransferRiskRatings(int transferId) {
//			int transferIdInt = Integer.parseInt(transferId);
//			int transferToLookup = transferIdInt;
//			
//			String targetCauseRiskCategory = null;
//			String targetCauseRiskLikelihood = null;
//			String targetType = null;
//			
//
//			boolean done = false;
//			while (!done) {
//				Set<Integer> previouslyVisitedTransfers = new HashSet<Integer>();
//				previouslyVisitedTransfers.add(transferIdInt);
//				// Lookup the cause transfer
//				Transfers transfer = transferService.getTransferByID(transferToLookup);
//				System.out.println("transfer " + transfer);
//				if (transfer.getTargetType().equals("CAUSE")) {
//					
//					Hazard_Causes targetCause = causeService.getHazardCauseByID(transfer.getTargetID());
//					
//					if (targetCause.getTransfer() != 0) {
//						// The cause transfers to a cause which is ALSO
//						// transferred.
//						
//
//						if (previouslyVisitedTransfers.contains(targetCause
//								.getTransfer())) {
//							// We have already seen this transfer. Thus, thereis a
//							// circular reference to transfers. This is bad. Weneed to return a value that the transfers are
//							// circular.
////							Hazard_Causes cause = hazardCauseService.getHazardCauseByID(String.valueOf(transfer.getTargetID()));
//
//							targetCauseRiskCategory = targetCause.getRiskCategory().getValue();
//							targetCauseRiskLikelihood = targetCause.getRiskLikelihood().getValue();
//							targetType = transfer.getTargetType();
//							
//							return Response.ok(new transferRiskValueJSON(targetCauseRiskCategory, targetCauseRiskLikelihood, targetType, transferId, true, true)).build();
//
//						} else {
//							// We need to loop around again to follow the
//							// transfer chain.
//
//							transferToLookup = targetCause.getTransfer();
//						}
//					} else {
//						Hazard_Causes cause = hazardCauseService.getHazardCauseByID(String.valueOf(transfer.getTargetID()));
//						String causeId = String.valueOf(cause.getID());
//						targetCauseRiskCategory = targetCause.getRiskCategory().getValue();
//						targetCauseRiskLikelihood = targetCause.getRiskLikelihood().getValue();
//						targetType = transfer.getTargetType();
//						
//						
//						return Response.ok(new transferRiskValueJSON(targetCauseRiskCategory, targetCauseRiskLikelihood, causeId, targetType, false, true)).build();
//						// Get this cause's risk values and return them
//					}
//
//				} else if (transfer.getTargetType().equals("HAZARD")) {
//					// The cause transfers to a hazard, so we need to return something
//					// which indicates this. In the template, causes which link to
//					// hazards go into a separate list.
//					Hazards hazard = hazardService.getHazardByID(String.valueOf(transfer.getTargetID()));
//					String hazardId = String.valueOf(hazard.getID());
//					
//					return Response.ok(new transferRiskValueJSON(null, null, hazardId, "HAZARD", false, true)).build();
//				}
//			}
//		}
//		return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in")).build();
//	}
}