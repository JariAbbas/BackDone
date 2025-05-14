package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.BaseRate;
import org.acme.service.BaseRateService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Path("/base-rates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BaseRateResource {

    @Inject
    BaseRateService baseRateService;

    @GET
    public Response getAllBaseRates() {
        try {
            List<BaseRate> baseRates = baseRateService.getAllBaseRates();
            return Response.ok(baseRates).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/{baseName}")
    public Response getBaseRateByName(@PathParam("baseName") String baseName) {
        try {
            Optional<BaseRate> baseRate = baseRateService.getBaseRateByName(baseName);
            return baseRate.map(Response::ok)
                    .orElseGet(() -> Response.status(Response.Status.NOT_FOUND)).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error: " + e.getMessage()).build();
        }
    }

    @POST
    public Response createBaseRate(BaseRateRequest request) {
        try {
            baseRateService.createBaseRate(request.getBaseName(), request.getBaseRate());
            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error: " + e.getMessage()).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{baseName}")
    public Response updateBaseRate(@PathParam("baseName") String baseName, BaseRateUpdateRequest request) {
        try {
            baseRateService.updateBaseRate(baseName, request.getBaseRate());
            return Response.ok().build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error: " + e.getMessage()).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteBaseRate(@PathParam("id") Long id) {
        try {
            baseRateService.deleteBaseRate(id);
            return Response.noContent().build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error: " + e.getMessage()).build();
        }
    }

    public static class BaseRateRequest {
        private String baseName;
        private BigDecimal baseRate;

        public String getBaseName() {
            return baseName;
        }

        public void setBaseName(String baseName) {
            this.baseName = baseName;
        }

        public BigDecimal getBaseRate() {
            return baseRate;
        }

        public void setBaseRate(BigDecimal baseRate) {
            this.baseRate = baseRate;
        }
    }

    public static class BaseRateUpdateRequest {
        private BigDecimal baseRate;

        public BigDecimal getBaseRate() {
            return baseRate;
        }

        public void setBaseRate(BigDecimal baseRate) {
            this.baseRate = baseRate;
        }
    }
}