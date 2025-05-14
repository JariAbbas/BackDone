package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.BookingTransaction;
import org.acme.service.BookingTransactionService;

import java.sql.SQLException;
import java.util.List;

@Path("/booking-transactions")
@Produces(MediaType.APPLICATION_JSON)
public class BookingTransactionResource {

    @Inject
    BookingTransactionService bookingTransactionService;

    // Remove getTransactionsByLoanRange and getTransactionsByLoanNumber methods

    @GET
    @Path("/by-voucher-number")
    public Response getTransactionsByVoucherNumber(@QueryParam("voucherNumber") String voucherNumber) {
        if (voucherNumber == null || voucherNumber.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Please provide the 'voucherNumber' as a query parameter.")
                    .build();
        }

        try {
            List<BookingTransaction> transactions = bookingTransactionService.getTransactionsByVoucherNumber(voucherNumber);
            if (transactions.isEmpty()) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            return Response.ok(transactions).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to fetch transactions: " + e.getMessage())
                    .build();
        }
    }
}