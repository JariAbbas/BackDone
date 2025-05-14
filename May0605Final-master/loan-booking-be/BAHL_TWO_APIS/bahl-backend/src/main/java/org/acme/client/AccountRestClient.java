package org.acme.client;

import org.acme.modelDAL.Account;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.sql.SQLException;

@RegisterRestClient(configKey = "account-client")
@Path("/internal/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface AccountRestClient {

    @GET
    @Path("/{customerNumber}")
    Account getAccount(@PathParam("customerNumber") String customerNumber);

    @PUT
    @Path("/{customerNumber}/updateBalance")
    void updateBalance(@PathParam("customerNumber") String customerNumber,
                       @QueryParam("amount") double newBalance) throws SQLException;

    @GET
    @Path("/{customerNumber}/title")
    String getAccountTitle(@PathParam("customerNumber") String customerNumber);
}
