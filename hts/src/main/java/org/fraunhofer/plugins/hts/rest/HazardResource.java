package org.fraunhofer.plugins.hts.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fraunhofer.plugins.hts.db.service.HazardService;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A resource of message.
 */
@Path("/report")
public class HazardResource {
	private final HazardService hazardService;
	
	public HazardResource(HazardService hazardService) {
		this.hazardService = checkNotNull(hazardService);
	}
	
    @GET
    @Path("hazardnumber/{hazardNumber}")
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response checkHazardNum(@PathParam("hazardNumber") String hazardNumber) {
    	if(!hazardService.hazardNumberExists(hazardNumber)) {
    		return Response.ok(new HazardResourceModel("Hazard # is available")).build();
    	}
    	else {
    		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new HazardResourceModel("Hazard # exists")).build();
    	}
    	
    }
    
}