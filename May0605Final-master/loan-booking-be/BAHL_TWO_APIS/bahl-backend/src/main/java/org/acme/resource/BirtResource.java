package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.service.BirtService;
import org.eclipse.birt.core.exception.BirtException;

import java.util.HashMap;
import java.util.Map;

@Path("/report")
public class BirtResource {

    @Inject
    BirtService birtService;

    @GET
    @Path("/generate")
    @Produces(MediaType.TEXT_HTML) // Changed to APPLICATION_PDF
    public Response generateReport(@QueryParam("voucherNumber") String voucherNumber) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("voucherNumberParam", voucherNumber); // Parameter name should match BIRT design

            byte[] bytes = birtService.generateReport(params);
            return Response.ok(bytes)
                    .header("Content-Disposition", "inline; filename=loan_report_" + voucherNumber + ".pdf") // Updated filename
                    .header("Content-Type", "application/pdf")
                    .build();
        } catch (BirtException e) {
            System.out.printf("error in report: %s%n", e.toString()); // Improved error logging
            return Response.status(Response.Status.BAD_REQUEST).entity("Error generating report: " + e.getMessage()).build(); // Added error entity
        }
    }
}