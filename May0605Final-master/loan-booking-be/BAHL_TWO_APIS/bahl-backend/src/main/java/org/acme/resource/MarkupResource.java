package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.Markup;
import org.acme.service.MarkupService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Path("/markups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MarkupResource {

    @Inject
    MarkupService markupService;

    @GET
    public Response getAllMarkups() {
        try {
            List<Markup> markups = markupService.getAllMarkups();
            return Response.ok(markups).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getMarkupById(@PathParam("id") Long id) {
        try {
            Optional<Markup> markup = markupService.getMarkupById(id);
            return markup.map(Response::ok)
                    .orElseGet(() -> Response.status(Response.Status.NOT_FOUND)).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error: " + e.getMessage()).build();
        }
    }

    @POST
    public Response createMarkup(@Valid CreateMarkupRequest request) {
        try {
            markupService.createMarkup(request.getLoanNumber(), request.getBaseRateId(), request.getSpreadRate(), request.getOdRate(), request.getFixedRate());
            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error: " + e.getMessage()).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateMarkup(@PathParam("id") Long id, @Valid UpdateMarkupRequest request) {
        try {
            markupService.updateMarkup(id, request.getLoanNumber(), request.getSpreadRate(), request.getOdRate());
            return Response.ok().build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error: " + e.getMessage()).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteMarkup(@PathParam("id") Long id) {
        try {
            markupService.deleteMarkup(id);
            return Response.noContent().build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/loan/{loanNumber}")
    public Response getMarkupByLoanNumber(@PathParam("loanNumber") String loanNumber) {
        try {
            Optional<Markup> markup = markupService.getMarkupByLoanNumber(loanNumber);
            return markup.map(Response::ok)
                    .orElseGet(() -> Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"message\": \"Markup not found for Loan Number: " + loanNumber + "\"}")).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error: " + e.getMessage()).build();
        }
    }

    public static class CreateMarkupRequest {
        @NotNull
        @Size(min = 9, max = 9)
        private String loanNumber;

        @NotNull
        private Long baseRateId;

        @NotNull
        @Digits(integer = 2, fraction = 2)
        @DecimalMin("0.00")
        private BigDecimal spreadRate;

        @NotNull
        @Digits(integer = 2, fraction = 2)
        @DecimalMin("0.00")
        private BigDecimal odRate;

        // New field for Fixed Rate input
        @Digits(integer = 10, fraction = 4)
        @DecimalMin("0.0000")
        private BigDecimal fixedRate;

        public String getLoanNumber() {
            return loanNumber;
        }

        public void setLoanNumber(String loanNumber) {
            this.loanNumber = loanNumber;
        }

        public Long getBaseRateId() {
            return baseRateId;
        }

        public void setBaseRateId(Long baseRateId) {
            this.baseRateId = baseRateId;
        }

        public BigDecimal getSpreadRate() {
            return spreadRate;
        }

        public void setSpreadRate(BigDecimal spreadRate) {
            this.spreadRate = spreadRate;
        }

        public BigDecimal getOdRate() {
            return odRate;
        }

        public void setOdRate(BigDecimal odRate) {
            this.odRate = odRate;
        }

        public BigDecimal getFixedRate() {
            return fixedRate;
        }

        public void setFixedRate(BigDecimal fixedRate) {
            this.fixedRate = fixedRate;
        }
    }

    public static class UpdateMarkupRequest {
        @NotNull
        @Size(min = 9, max = 9)
        private String loanNumber;

        @NotNull
        @Digits(integer = 2, fraction = 2)
        @DecimalMin("0.00")
        private BigDecimal spreadRate;

        @NotNull
        @Digits(integer = 2, fraction = 2)
        @DecimalMin("0.00")
        private BigDecimal odRate;

        public String getLoanNumber() {
            return loanNumber;
        }

        public void setLoanNumber(String loanNumber) {
            this.loanNumber = loanNumber;
        }

        public BigDecimal getSpreadRate() {
            return spreadRate;
        }

        public void setSpreadRate(BigDecimal spreadRate) {
            this.spreadRate = spreadRate;
        }

        public BigDecimal getOdRate() {
            return odRate;
        }

        public void setOdRate(BigDecimal odRate) {
            this.odRate = odRate;
        }
    }
}