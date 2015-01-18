package org.fraunhofer.plugins.hts.rest;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fraunhofer.plugins.hts.db.Transfers;
import org.fraunhofer.plugins.hts.db.Transfers.TransferType;
import org.fraunhofer.plugins.hts.db.service.TransferService;
import org.fraunhofer.plugins.hts.rest.datatype.TransferJSON;

import com.atlassian.jira.component.ComponentAccessor;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

//String respStr = "{ \"success\" : \"true\" }";
@Path("/transfer")
public class TransferRestService {
	private TransferService transferService;

	public TransferRestService(TransferService transferService) {
		this.transferService = transferService;
	}

	@GET
	@Path("findOrigins")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getIncomingTransfersForElement(@QueryParam("type") String type,
			@QueryParam("elementId") int elementId) {
		if (ComponentAccessor.getJiraAuthenticationContext().isLoggedInUser()) {
			checkArgument(type != null && TransferType.valueOf(type.toUpperCase()) != null && elementId > 0);

			List<TransferJSON> values = Lists.transform(transferService.getOriginsForId(type.toUpperCase(), elementId),
					new Function<Transfers, TransferJSON>() {
						public TransferJSON apply(Transfers t) {
							return new TransferJSON(t);
						}
					});

			return Response.ok(values).build();
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity(new HazardResourceModel("User is not logged in"))
					.build();
		}
	}
}
