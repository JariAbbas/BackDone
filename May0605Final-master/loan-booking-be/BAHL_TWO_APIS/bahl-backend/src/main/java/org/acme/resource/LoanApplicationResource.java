package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.client.UserDatesClient;
import org.acme.model.LoanApplication;
import org.acme.model.LoanLimit;
// import org.acme.modelDAL.UserDates; // Not directly used here if UserDatesClient provides all needed
import org.acme.service.LoanApplicationService;
import org.acme.service.LoanLimitService;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Path("/loans")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoanApplicationResource {

    @Inject
    LoanApplicationService loanApplicationService;

    @Inject
    LoanLimitService loanLimitService;

    @Inject
    @RestClient
    UserDatesClient userDatesClient;

    // Helper classes for structured responses
    // Placed at the top for better readability or in their own files if preferred.
    static class ErrorMessage {
        public String message;
        public ErrorMessage(String message) {
            this.message = message;
        }
    }

    static class SuccessMessage {
        public String message;
        public SuccessMessage(String message) {
            this.message = message;
        }
    }

    @POST
    @Path("/create/{customerNumber}")
    public Response createLoan(
            @PathParam("customerNumber") String customerNumber,
            @QueryParam("noOfDays") Integer noOfDays,
            @QueryParam("applicableRate") Double applicableRate,
            @QueryParam("odRate") Double odRate,
            LoanApplication loanRequest) {

        // Validate required fields from loanRequest if not implicitly handled by Bean Validation
        if (loanRequest.getDocumentRefNo() == null || loanRequest.getDealAmount() == null || loanRequest.getRemarks() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Missing required loan details (documentRefNo, dealAmount, remarks).")).build();
        }
        if (noOfDays == null || applicableRate == null || odRate == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Missing required query parameters (noOfDays, applicableRate, odRate).")).build();
        }


        LoanLimit loanLimitData = loanLimitService.getLoanLimitByCustomerNumber(customerNumber);
        if (loanLimitData == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Loan limit not found for this customer.")).build();
        }

        if (loanRequest.getDealAmount() > loanLimitData.getLoanLimit()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Deal Amount cannot exceed the Loan Limit of " + loanLimitData.getLoanLimit() + ".")).build();
        }

        LocalDate currentDateFromService = null;
        try {
            LocalDateTime userDateTime = userDatesClient.getPreviousDateForUser("Creditusr"); // Assuming "Creditusr" for booking date
            currentDateFromService = (userDateTime != null) ? userDateTime.toLocalDate() : LocalDate.now();
            if (userDateTime == null) System.out.println("No UserDates found for Creditusr, using current system date for grantDate: " + currentDateFromService);

        } catch (Exception e) {
            System.err.println("Error fetching UserDates for grantDate: " + e.getMessage() + ". Falling back to system date.");
            currentDateFromService = LocalDate.now();
        }
        Date grantDate = Date.valueOf(currentDateFromService);

        String documentRefNo = loanRequest.getDocumentRefNo();
        String loanNumber = "";
        if (documentRefNo.length() >= 8) { // Basic check, assuming format like "ABCD1234..."
            loanNumber = documentRefNo.substring(4, 8);
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Invalid DocumentRefNo format. Expected at least 8 characters.")).build();
        }

        LoanApplication loan = new LoanApplication(
                loanNumber, customerNumber, grantDate, documentRefNo,
                loanRequest.getDealAmount(), loanRequest.getRemarks(),
                0.0, // Initial accrual calculated in service
                1,   // Default status: 1 (Unauthorized)
                null, // authorizedBy is NULL at creation
                noOfDays, applicableRate, odRate
        );

        try {
            loanApplicationService.create(loan);
            LoanApplication createdLoan = loanApplicationService.findByLoanNumber(loan.getLoanNumber()); // Fetch to get ID and service-set fields
            return Response.status(Response.Status.CREATED).entity(createdLoan).build();
        } catch (Exception e) {
            // Log the server-side exception for debugging
            System.err.println("Error during loan creation: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Loan creation failed: " + e.getMessage())).build();
        }
    }

    @GET
    public Response getAllLoans() throws SQLException { // Consider adding try-catch for SQLException
        List<LoanApplication> loans = loanApplicationService.listAll();
        return Response.ok(loans).build();
    }

    @GET
    @Path("/{id}")
    public Response getLoanById(@PathParam("id") int id) throws SQLException { // Consider adding try-catch
        LoanApplication loan = loanApplicationService.findById(id);
        if (loan != null) {
            return Response.ok(loan).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorMessage("Loan not found with ID: " + id)).build();
        }
    }

    @PUT
    @Path("/{loanNumber}") // Endpoint to initiate modification for an ACTIVE loan
    public Response updateLoanByLoanNumber(
            @PathParam("loanNumber") String loanNumber,
            LoanApplication loanDetailsToUpdate) { // Expects remarks and noOfDays in the payload

        try {
            LoanApplication existingLoan = loanApplicationService.findByLoanNumber(loanNumber);
            if (existingLoan == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorMessage("Loan with Loan Number " + loanNumber + " not found for update.")).build();
            }

            // User Story: "Only Active loan can be open for Modification"
            if (existingLoan.getStatus() != 9) { // If loan is NOT Active (Status 9)
                String message = "Loan " + loanNumber + " cannot be modified. ";
                if (existingLoan.getStatus() == 0) { message += "It is Cancelled."; }
                else if (existingLoan.getStatus() == 2) { message += "It is already awaiting modification authorization."; }
                else if (existingLoan.getStatus() == 987) { message += "It is Rejected."; }
                // Add other status checks like "Paid" if applicable
                else { message += "Its current status (" + existingLoan.getStatus() + ") does not allow modification."; }
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage(message)).build();
            }

            // If we reach here, loan is Active (Status 9) and can be modified.
            // User story: "Only “No. of days” and “Remarks” field will be editable."
            if (loanDetailsToUpdate.getNoOfDays() == null || loanDetailsToUpdate.getRemarks() == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Both 'noOfDays' and 'remarks' must be provided for modification.")).build();
            }
            existingLoan.setNoOfDays(loanDetailsToUpdate.getNoOfDays());
            existingLoan.setRemarks(loanDetailsToUpdate.getRemarks());

            // "Modification will be park for authorization queue." -> Set status to 'Awaiting Modification Authorization'
            existingLoan.setStatus(2); // Status 2: Awaiting Modification Authorization

            LoanApplication updatedLoan = loanApplicationService.updateByLoanNumber(loanNumber, existingLoan);
            if (updatedLoan != null) {
                return Response.ok(updatedLoan).build();
            } else {
                // This might be redundant if service throws exception on failure to update
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage("Failed to submit loan modification for " + loanNumber + ".")).build();
            }
        } catch (SQLException e) {
            System.err.println("SQL Error during loan modification for " + loanNumber + ": " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage("Database error submitting loan modification: " + e.getMessage())).build();
        } catch (Exception e) {
            System.err.println("Unexpected Error during loan modification for " + loanNumber + ": " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Error submitting loan modification: " + e.getMessage())).build();
        }
    }

    @GET
    @Path("/number/{loanNumber}")
    public Response getLoanByLoanNumber(@PathParam("loanNumber") String loanNumber) {
        try {
            LoanApplication loan = loanApplicationService.findByLoanNumber(loanNumber);
            if (loan != null) {
                return Response.ok(loan).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorMessage("Loan not found with Loan Number: " + loanNumber)).build();
            }
        } catch (SQLException e) {
            System.err.println("SQL Error fetching loan by number " + loanNumber + ": " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage("Database error: " + e.getMessage())).build();
        }
    }

    @PUT
    @Path("/cancel/{loanNumber}")
    public Response cancelLoan(@PathParam("loanNumber") String loanNumber) {
        try {
            LoanApplication loan = loanApplicationService.findByLoanNumber(loanNumber);
            if (loan == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorMessage("Loan not found with Loan Number: " + loanNumber)).build();
            }

            // --- Enhanced Status Checks for Cancellation Initiation ---
            if (loan.getStatus() == 0) { // Already Cancelled in LoanApplication table
                // Check if it's pending authorization in CancelledLoan table
                if (loanApplicationService.checkCancelledLoanExists(loanNumber)) {
                    int clStatus = loanApplicationService.getCancelledLoanStatus(loanNumber);
                    if (clStatus == 1) return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Cancellation is already pending authorization for Loan: " + loanNumber)).build();
                    if (clStatus == 9) return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Loan " + loanNumber + " has already been fully cancelled.")).build();
                }
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Loan " + loanNumber + " is already marked as cancelled.")).build();
            }
            if (loan.getStatus() == 9) { // Active
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Active loan cannot be cancelled through this flow. Loan Number: " + loanNumber)).build();
            }
            if (loan.getStatus() == 2) { // Awaiting Modification Authorization
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Loan is awaiting modification authorization. Cannot cancel. Loan Number: " + loanNumber)).build();
            }
            if (loan.getStatus() == 987) { // Rejected
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Rejected loan cannot be cancelled. Loan Number: " + loanNumber)).build();
            }

            LocalDate currentDate = loanApplicationService.getCurrentDateFromUserDates(); // Service handles fallback
            LocalDate grantDate = loan.getGrantDate().toLocalDate();

            // Only Status 1 (Unauthorized) should proceed for this cancellation initiation.
            if (loan.getStatus() != 1 ) {
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Only unauthorized loans (Status 1) can be initiated for cancellation. Current status: " + loan.getStatus() + " for Loan Number: " + loanNumber)).build();
            }

            // Check if already pending in CancelledLoan (this might be slightly redundant given above checks but good for integrity)
            if (loanApplicationService.checkCancelledLoanExists(loanNumber)) {
                int clStatus = loanApplicationService.getCancelledLoanStatus(loanNumber);
                if (clStatus == 1) return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Cancellation is already pending authorization for Loan: " + loanNumber)).build();
                if (clStatus == 9) return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Loan " + loanNumber + " has already been fully cancelled.")).build();
                if (clStatus == 987 && loan.getStatus() == 1) { /* A previous cancellation was rejected, allow re-initiation if loan is still status 1 */ }
                else if (clStatus == 987) return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("A previous cancellation request for " + loanNumber + " was rejected.")).build();

            }



            if (!grantDate.isEqual(currentDate)) {
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Deal can only be cancelled on Book date (Same day). Loan: " + loanNumber + ", Book date: " + grantDate + ", Current book date: " + currentDate)).build();
            }

            boolean cancellationInitiated = loanApplicationService.cancelLoan(loanNumber); // Service sets LA status to 0, creates CL with status 1

            if (cancellationInitiated) {
                return Response.ok(new SuccessMessage("Deal cancellation initiated and awaiting authorization for Loan Number: " + loanNumber)).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Failed to initiate cancellation for Loan Number: " + loanNumber + ". Loan might not be in a cancellable state (Status 1).")).build();
            }
        } catch (SQLException e) {
            System.err.println("SQL Error during cancelLoan for " + loanNumber + ": " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage("Database error during cancellation: " + e.getMessage())).build();
        } catch (Exception e) {
            System.err.println("Unexpected Error during cancelLoan for " + loanNumber + ": " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage("Unexpected error during cancellation: " + e.getMessage())).build();
        }
    }


    @PUT
    @Path("/authorize/{loanNumber}")
    public Response authorizeLoan(
            @PathParam("loanNumber") String loanNumber,
            @QueryParam("authorizedBy") String authorizedBy) {

        if (authorizedBy == null || authorizedBy.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("AuthorizedBy user must be provided via query parameter.")).build();
        }

        try {
            LoanApplication authorizedLoan = loanApplicationService.approveLoanApplication(loanNumber, authorizedBy);
            // Service method now handles logic for initial approval (status 1) vs modification approval (status 2)
            return Response.ok(new SuccessMessage("Loan Number " + loanNumber + " process authorized successfully by " + authorizedBy + ". Status set to Active (9).")).entity(authorizedLoan).build();
        } catch (SQLException e) {
            System.err.println("SQL Error during authorizeLoan for " + loanNumber + ": " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage("Database error during authorization: " + e.getMessage())).build();
        } catch (IllegalStateException e) { // Custom exception from service for invalid state
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Authorization failed: " + e.getMessage())).build();
        } catch (Exception e) {
            System.err.println("Unexpected Error during authorizeLoan for " + loanNumber + ": " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage("An unexpected error occurred during authorization: " + e.getMessage())).build();
        }
    }

    @PUT
    @Path("/reject/{loanNumber}")
    public Response rejectLoan(
            @PathParam("loanNumber") String loanNumber
            // Consider adding @QueryParam("rejectedBy") String rejectedBy if needed for audit
    ) {
        try {
            LoanApplication loan = loanApplicationService.findByLoanNumber(loanNumber); // Fetch first to check current status
            if (loan == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorMessage("Loan not found with Loan Number: " + loanNumber)).build();
            }

            // Allow rejection if status is 1 (Unauthorized) or 2 (Awaiting Modification Authorization)
            if (loan.getStatus() != 1 && loan.getStatus() != 2) {
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Only loans with status Unauthorized (1) or Awaiting Modification Authorization (2) can be rejected. Current status: " + loan.getStatus())).build();
            }

            boolean rejected = loanApplicationService.rejectLoan(loanNumber); // Service handles GL reversal (of parked) and status update to 987
            if (rejected) {
                return Response.ok(new SuccessMessage("Loan Number " + loanNumber + " rejected successfully. Status set to Rejected (987).")).build();
            } else {
                // This path might not be hit if service's rejectLoan throws an exception on specific failures
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage("Failed to reject loan " + loanNumber + ". The loan might not have been found by the service method or another issue occurred.")).build();
            }
        } catch (SQLException e) {
            System.err.println("SQL Error during rejectLoan for " + loanNumber + ": " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage("Database error during rejection: " + e.getMessage())).build();
        } catch (Exception e) { // Catch other exceptions from service layer (e.g., if service throws for loan not found)
            System.err.println("Unexpected Error during rejectLoan for " + loanNumber + ": " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Rejection failed: " + e.getMessage())).build();
        }
    }

    @GET
    @Path("/check-loan-exists/{loanNumber}")
    public Response checkLoanExists(@PathParam("loanNumber") String loanNumber) {
        try {
            boolean exists = loanApplicationService.checkLoanExists(loanNumber);
            return Response.ok(exists).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage(e.getMessage())).build();
        }
    }

    @GET
    @Path("/check-cancelled-loan-exists/{loanNumber}")
    public Response checkCancelledLoanExists(@PathParam("loanNumber") String loanNumber) {
        try {
            boolean exists = loanApplicationService.checkCancelledLoanExists(loanNumber);
            return Response.ok(exists).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage(e.getMessage())).build();
        }
    }

    @GET
    @Path("/cancellation-authorization") // Gets loans awaiting cancellation authorization
    public Response getLoansAwaitingCancellationAuthorization() {
        try {
            List<LoanApplication> loans = loanApplicationService.findLoansAwaitingCancellationAuthorization();
            // These loans in LoanApplication table have Status = 0,
            // and corresponding entries in CancelledLoan table have Status = 1.
            return Response.ok(loans).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage(e.getMessage())).build();
        }
    }

    @PUT
    @Path("/authorize-cancellation/{loanNumber}")
    public Response authorizeCancellation(
            @PathParam("loanNumber") String loanNumber
            // Consider @QueryParam("authorizedBy") String authorizedBy for audit
    ) {
        try {
            boolean authorized = loanApplicationService.authorizeLoanCancellation(loanNumber);
            if (authorized) {
                return Response.ok(new SuccessMessage("Loan cancellation authorized and processed for Loan Number: " + loanNumber)).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Failed to authorize loan cancellation for " + loanNumber + ". Loan may not exist in CancelledLoan table or not be in correct state (Status 1).")).build();
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage("Database error during cancellation authorization: " + e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage("Unexpected error during cancellation authorization: " + e.getMessage())).build();
        }
    }

    @PUT
    @Path("/reject-cancellation/{loanNumber}")
    public Response rejectCancellation(
            @PathParam("loanNumber") String loanNumber
            // Consider @QueryParam("rejectedBy") String rejectedBy for audit
    ) {
        try {
            boolean rejected = loanApplicationService.rejectLoanCancellation(loanNumber); // Service sets CL status to 987, LA status to 1
            if (rejected) {
                return Response.ok(new SuccessMessage("Loan cancellation rejected for Loan Number: " + loanNumber + ". Loan reverted to Unauthorized (Status 1).")).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Failed to reject loan cancellation for " + loanNumber + ". Loan may not exist in CancelledLoan table or not be in correct state.")).build();
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage("Database error during cancellation rejection: " + e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage("Unexpected error during cancellation rejection: " + e.getMessage())).build();
        }
    }

    @PUT
    @Path("/authorize-recovery-modification/{loanNumber}")
    public Response authorizeRecoveryModification(
            @PathParam("loanNumber") String loanNumber
            // Consider @QueryParam("authorizedBy") String authorizedBy
    ) {
        try {
            LoanApplication loan = loanApplicationService.findByLoanNumber(loanNumber);
            if (loan == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorMessage("Loan not found with Loan Number: " + loanNumber)).build();
            }
            if (loan.getStatus() != 7) { // Assuming 7 is "Awaiting Recovery Authorization"
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Only loans awaiting recovery authorization (Status 7) can be processed. Current status: " + loan.getStatus())).build();
            }
            loanApplicationService.updateStatusByLoanNumber(loanNumber, 10); // Status 10: Modification Authorized After Recovery
            return Response.ok(new SuccessMessage("Loan Number " + loanNumber + " authorized for modification after recovery. Status set to 10.")).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage("Database error: " + e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage("Unexpected error: " + e.getMessage())).build();
        }
    }
}
