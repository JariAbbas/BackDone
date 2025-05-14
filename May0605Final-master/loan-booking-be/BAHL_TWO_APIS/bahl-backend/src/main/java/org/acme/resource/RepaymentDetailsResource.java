package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.RepaymentDetails;
import org.acme.service.RepaymentDetailsService;

@Path("/repayments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RepaymentDetailsResource {

    @Inject
    RepaymentDetailsService repaymentDetailsService;

    @GET
    @Path("/{loanNumber}")
    public Response getRepaymentDetails(@PathParam("loanNumber") String loanNumber) throws Exception {
        RepaymentDetails repayment = repaymentDetailsService.findByLoanNumber(loanNumber);
        if (repayment != null) {
            return Response.ok(repayment).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    public Response createRepaymentDetails(RepaymentDetails repayment) throws Exception {
        repaymentDetailsService.create(repayment);
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{loanNumber}")
    @Consumes(MediaType.APPLICATION_JSON) // Ensure it consumes JSON
    public Response updateNoOfDays(
            @PathParam("loanNumber") String loanNumber,
            RepaymentDetails updateRequest) throws Exception {

        if (updateRequest == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Request body must contain 'noOfDays' to update.\"}")
                    .build();
        }

        RepaymentDetails updatedRepayment = repaymentDetailsService.updateNoOfDays(loanNumber, updateRequest.getNoOfDays());

        if (updatedRepayment != null) {
            return Response.ok(updatedRepayment).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Repayment details not found for Loan Number: " + loanNumber + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{loanNumber}")
    public Response deleteRepaymentDetails(@PathParam("loanNumber") String loanNumber) throws Exception {
        repaymentDetailsService.delete(loanNumber);
        return Response.noContent().build();
    }
}