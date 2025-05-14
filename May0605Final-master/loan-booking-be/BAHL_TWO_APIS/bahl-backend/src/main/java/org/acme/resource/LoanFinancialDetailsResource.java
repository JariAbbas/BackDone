package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.service.LoanFinancialDetailsService;

import java.util.Map;

@Path("/loan-financial-details")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoanFinancialDetailsResource {

    @Inject
    LoanFinancialDetailsService loanFinancialDetailsService;

    @POST
    @Path("/create")
    public Response createLoanFinancialDetails(Map<String, Object> requestBody) {
        String loanNumber = (String) requestBody.get("loanNumber");
        Integer noOfDays = (Integer) requestBody.get("noOfDays");

        if (loanNumber == null || loanNumber.trim().isEmpty() || noOfDays == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"LoanNumber and noOfDays are required.\"}").build();
        }

        try {
            Map<String, Object> financialDetails = loanFinancialDetailsService.create(loanNumber, noOfDays);
            return Response.status(Response.Status.CREATED).entity(financialDetails).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{loanNumber}")
    public Response updateLoanFinancialDetails(
            @PathParam("loanNumber") String loanNumber,
            Map<String, Object> requestBody) {
        Integer noOfDays = (Integer) requestBody.get("noOfDays");

        if (noOfDays == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"noOfDays is required for update.\"}").build();
        }

        try {
            Map<String, Object> updatedFinancialDetails = loanFinancialDetailsService.update(loanNumber, noOfDays);
            if (updatedFinancialDetails != null) {
                return Response.ok(updatedFinancialDetails).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("message", "Financial details not found for LoanNumber: " + loanNumber)).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", e.getMessage())).build();
        }
    }

    @GET
    @Path("/{loanNumber}")
    public Response getLoanFinancialDetails(@PathParam("loanNumber") String loanNumber) {
        try {
            Map<String, Object> financialDetails = loanFinancialDetailsService.findByLoanNumber(loanNumber);
            if (financialDetails != null) {
                return Response.ok(financialDetails).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("message", "Financial details not found for LoanNumber: " + loanNumber)).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("message", e.getMessage())).build();
        }
    }
}