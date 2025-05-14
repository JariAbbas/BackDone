package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.LoanLimit;
import org.acme.service.LoanLimitService;
import java.util.List;

@Path("/loanlimits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoanLimitResource {

    @Inject
    LoanLimitService loanLimitService;

    @POST
    @Path("/create/{customerNumber}")
    public Response createLoanLimit(@PathParam("customerNumber") String customerNumber,
                                    LoanLimit loanLimitRequest,
                                    @HeaderParam("userId") String userId) {

        if (loanLimitRequest.getLoanLimit() <= 0 ) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"Invalid LoanLimit .\"}").build();
        }

        LoanLimit unAuthorizedLoanLimit = loanLimitService.createLoanLimit(customerNumber, loanLimitRequest.getLoanLimit(),0, userId); //yahan balance 0 pass krna hai

        if (unAuthorizedLoanLimit != null) {
            return Response.status(Response.Status.CREATED).entity(unAuthorizedLoanLimit).build();
        } else {
            // A loan limit already exists
            return Response.status(Response.Status.CONFLICT).entity("{\"message\": \"Loan limit already exists for this customer. Use the update endpoint.\"}").build();
        }
    }

    @PUT
    @Path("/update/{customerNumber}")  // Changed to update existing authorized limit
    public Response updateLoanLimit(@PathParam("customerNumber") String customerNumber,
                                    LoanLimit loanLimitRequest,
                                    @HeaderParam("userId") String userId) {


        if (loanLimitRequest.getLoanLimit() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"Invalid LoanLimit value.\"}").build();
        }
        LoanLimit updatedLoanLimit = loanLimitService.updateLoanLimit(customerNumber, loanLimitRequest.getLoanLimit(), userId);

        if (updatedLoanLimit != null) {
            return Response.ok(updatedLoanLimit).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"Failed to update loan limit.\"}").build();
    }

    @GET
    @Path("/{customerNumber}")
    public Response getLoanLimit(@PathParam("customerNumber") String customerNumber) {
        LoanLimit loanLimit = loanLimitService.getLoanLimitByCustomerNumber(customerNumber);
        if (loanLimit == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Loan limit not found for customer.\"}").build();
        }
        return Response.ok(loanLimit).build();
    }

    // New endpoint to authorize a loan limit
    @POST
    @Path("/authorize/{customerNumber}")
    public Response authorizeLoanLimit(@PathParam("customerNumber") String customerNumber,  @HeaderParam("userId") String authorizedBy) {
        LoanLimit authorizedLoanLimit = loanLimitService.authorizeLoanLimit(customerNumber, authorizedBy);
        if (authorizedLoanLimit == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"Failed to authorize loan limit.\"}").build(); //Or 404
        }
        return Response.ok(authorizedLoanLimit).build();
    }

    @GET
    @Path("/unauthorized/{customerNumber}")
    public Response getUnauthorizedLoanLimit(@PathParam("customerNumber") String customerNumber) {
        LoanLimit unauthorizedLoanLimit = loanLimitService.getUnauthorizedLoanLimitByCustomerNumber(customerNumber);
        if (unauthorizedLoanLimit == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Unauthorized Loan limit not found for customer.\"}").build();
        }
        return Response.ok(unauthorizedLoanLimit).build();
    }

    @POST
    @Path("/reject/{customerNumber}")
    public Response rejectLoanLimit(@PathParam("customerNumber") String customerNumber) {
        boolean rejected = loanLimitService.rejectLoanLimit(customerNumber);
        if (!rejected) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"Failed to reject loan limit or unauthorized limit not found.\"}").build();
        }
        return Response.ok("{\"message\": \"Loan limit rejected successfully.\"}").build();
    }
    @GET
    @Path("/unauthorized")
    public Response getUnauthorizedLoanLimits() {
        List<LoanLimit> unauthorizedLoanLimits = loanLimitService.getUnauthorizedLoanLimits();
        if (unauthorizedLoanLimits == null || unauthorizedLoanLimits.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(unauthorizedLoanLimits).build();
    }

}