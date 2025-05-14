package org.acme.resource;

import org.acme.modelDAL.Account;
import org.acme.repository.AccountRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;

@Path("/internal/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InternalAccountResource {

    @Inject
    AccountRepository accountRepository;

    @GET
    @Path("/{customerNumber}")
    public Response getAccount(@PathParam("customerNumber") String customerNumber) {
        Account acc = accountRepository.findByCustomerNumber(customerNumber);
        return acc != null ? Response.ok(acc).build() :
                Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("/{customerNumber}/updateBalance")
    public Response updateBalance(@PathParam("customerNumber") String customerNumber,
                                  @QueryParam("amount") double newBalance) throws SQLException {
        boolean updated = accountRepository.updateBalance(customerNumber, newBalance);
        return updated ? Response.ok().build() : Response.status(Response.Status.BAD_REQUEST).build();
    }

    @GET
    @Path("/{customerNumber}/title")
    public Response getAccountTitle(@PathParam("customerNumber") String customerNumber) {
        String title = accountRepository.findAccountTitleByCustomerNumber(customerNumber);
        return title != null ? Response.ok(title).build() : Response.status(Response.Status.NOT_FOUND).build();
    }
}
