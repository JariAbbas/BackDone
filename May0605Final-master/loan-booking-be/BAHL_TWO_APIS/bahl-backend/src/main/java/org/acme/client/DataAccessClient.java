package org.acme.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.modelDAL.UserDates;
import org.acme.serviceDAL.DataAccessService;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/data")
@RegisterRestClient
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DataAccessClient {

    @POST
    @Path("/login")
    UserDates loginAndFetchDates(DataAccessService.LoginRequest request);
}