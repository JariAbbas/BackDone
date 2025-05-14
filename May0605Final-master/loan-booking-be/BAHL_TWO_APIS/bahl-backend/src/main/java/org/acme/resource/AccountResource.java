package org.acme.resource;

import org.acme.modelDAL.Account;
import org.acme.service.AccountService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountResource {

    @Inject
    AccountService accountService;

    @GET
    @Path("/{customerNumber}")
    public Response checkAccount(@PathParam("customerNumber") String customerNumber) {
        Account account = accountService.validateAccount(customerNumber);

        if (account == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Customer not found\"}")
                    .build();
        }

        if (account.getCurrencyCode() != 586) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Only PKR Accounts are allowed for limit.\"}")
                    .build();
        }

        return Response.ok(account).build();
    }
}
