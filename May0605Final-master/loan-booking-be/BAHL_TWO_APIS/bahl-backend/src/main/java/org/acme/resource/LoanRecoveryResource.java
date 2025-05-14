package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.exception.LoanNotFoundException;
import org.acme.exception.RecoveryAlreadyExistsException;
import org.acme.exception.UnauthorizedLoanException;
import org.acme.model.LoanRecoveryDetails;
import org.acme.service.LoanRecoveryService;

import java.sql.SQLException;

@Path("/loanRecovery")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoanRecoveryResource {

    @Inject
    LoanRecoveryService loanRecoveryService;


    @GET
    @Path("/{loanNumber}")
    public Response getRecoveryDetails(@PathParam("loanNumber") String loanNumber) {
        try {
            LoanRecoveryDetails details = loanRecoveryService.getLoanRecoveryDetails(loanNumber);
            return Response.ok(details).build();
        } catch (LoanNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (UnauthorizedLoanException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (SQLException e) {
            // Log the exception e.printStackTrace(); // Or use a proper logger
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Database error fetching recovery details: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) { // Catch generic exception from service
            // Log the exception e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error fetching recovery details: " + e.getMessage() + "\"}")
                    .build();
        }
    }


    @POST
    @Path("/{loanNumber}")
    public Response performRecovery(@PathParam("loanNumber") String loanNumber,
                                    @QueryParam("recoveredBy") String recoveredByUserId) { // Optional: Get user ID from query param or security context
        try {
            // Optional: Validate recoveredByUserId if needed

            LoanRecoveryDetails savedDetails = loanRecoveryService.performLoanRecovery(loanNumber, recoveredByUserId);
            // Recovery successful, return 201 Created (or 200 OK) with the details
            return Response.status(Response.Status.CREATED) // Or Response.ok()
                    .entity(savedDetails)
                    .build();

        } catch (LoanNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (RecoveryAlreadyExistsException e) {
            return Response.status(Response.Status.CONFLICT) // 409 Conflict is appropriate here
                    .entity("{\"message\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (UnauthorizedLoanException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (SQLException e) {
            // Log the exception e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Database error during loan recovery: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) { // Catch generic or custom exceptions from service
            // Log the exception e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error performing loan recovery: " + e.getMessage() + "\"}")
                    .build();
        }
    }


    @POST
    @Path("/{loanNumber}/authorize")
    public Response sendToAuthorization(@PathParam("loanNumber") String loanNumber) {
        try {
            loanRecoveryService.sendToAuthorizationQueue(loanNumber);
            return Response.ok().entity("{\"message\": \"Loan recovery sent to authorization queue successfully.\"}")
                    .build();
        } catch (LoanNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (UnauthorizedLoanException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (SQLException e) {
            // Log the exception e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Database error updating recovery status: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            // Log the exception e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error sending loan recovery to authorization queue: " + e.getMessage() + "\"}")
                    .build();
        }
    }


    @POST
    @Path("/{loanNumber}/authorize/confirm")
    public Response confirmAuthorization(@PathParam("loanNumber") String loanNumber,
                                         @QueryParam("authorizedBy") String authorizedByUserId) {
        try {
            loanRecoveryService.authorizeRecovery(loanNumber, authorizedByUserId);
            return Response.ok().entity("{\"message\": \"Loan recovery authorized successfully.\"}")
                    .build();
        } catch (LoanNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (UnauthorizedLoanException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (SQLException e) {
            // Log the exception e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Database error authorizing loan recovery: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            // Log the exception e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error authorizing loan recovery: " + e.getMessage() + "\"}")
                    .build();
        }
    }


    @PUT // Changed to PUT as it's an update action
    @Path("/{loanNumber}/authorize/reject")
    public Response rejectAuthorization(@PathParam("loanNumber") String loanNumber) {
        try {
            loanRecoveryService.rejectRecovery(loanNumber);
            return Response.ok().entity("{\"message\": \"Loan recovery authorization rejected.\"}")
                    .build();
        } catch (LoanNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (UnauthorizedLoanException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (SQLException e) {
            // Log the exception e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Database error rejecting loan recovery: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            // Log the exception e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error rejecting loan recovery authorization: " + e.getMessage() + "\"}")
                    .build();
        }
    }




}