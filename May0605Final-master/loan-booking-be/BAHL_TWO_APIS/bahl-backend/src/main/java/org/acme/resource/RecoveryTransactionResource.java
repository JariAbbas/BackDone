package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.RecoveryTransaction;
import org.acme.repository.RecoveryTransactionRecordRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Path("/recovery-transactions")
@Produces(MediaType.APPLICATION_JSON)
public class RecoveryTransactionResource {

    @Inject
    RecoveryTransactionRecordRepository recoveryTransactionRepository;

    @GET
    public Response getTransactionsByDateRange(
            @QueryParam("startingDate") String startingDate,
            @QueryParam("endingDate") String endingDate) {
        if (startingDate == null || startingDate.isEmpty() || endingDate == null || endingDate.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Both startingDate and endingDate are required in yyyy-MM-dd format.\"}").build();
        }

        try {
            LocalDate startDate = LocalDate.parse(startingDate);
            LocalDate endDate = LocalDate.parse(endingDate);

            if (startDate.isAfter(endDate)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"startingDate cannot be after endingDate.\"}").build();
            }

            List<RecoveryTransaction> transactions = recoveryTransactionRepository.findByDateRange(startDate, endDate);
            return Response.ok(transactions).build();

        } catch (DateTimeParseException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Invalid date format. Please use yyyy-MM-dd.\"}").build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Failed to fetch transactions: " + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/by-voucher-id-range") // New endpoint
    public Response getTransactionsByVoucherIdRange(
            @QueryParam("fromVoucherId") String fromVoucherId,
            @QueryParam("toVoucherId") String toVoucherId) {
        if (fromVoucherId == null || fromVoucherId.isEmpty() || toVoucherId == null || toVoucherId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Both fromVoucherId and toVoucherId are required.\"}").build();
        }

        try {
            List<RecoveryTransaction> transactions = recoveryTransactionRepository.findByVoucherIdRange(fromVoucherId, toVoucherId);
            return Response.ok(transactions).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Failed to fetch transactions: " + e.getMessage() + "\"}").build();
        }
    }
}