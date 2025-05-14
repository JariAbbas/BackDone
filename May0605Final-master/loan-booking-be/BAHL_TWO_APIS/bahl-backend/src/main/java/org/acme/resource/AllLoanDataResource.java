package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.service.AllLoanDataService; // Create this service

import org.eclipse.birt.core.exception.BirtException;

import java.util.HashMap;
import java.util.Map;

@Path("/allLoanData") // Meaningful path
public class AllLoanDataResource {

    @Inject
    AllLoanDataService birtService; // Inject the service

    @GET
    @Path("/generate")
    @Produces(MediaType.TEXT_HTML) // Produce PDF
    public Response generateReport() {
        try {
            Map<String, Object> params = new HashMap<>(); // No parameters needed, but keep the structure

            byte[] bytes = birtService.generateReport(params);
            return Response.ok(bytes)
                    .header("Content-Disposition", "inline; filename=all_loan_data_report.pdf") // Meaningful filename
                    .header("Content-Type", "application/pdf")
                    .build();
        } catch (BirtException e) {
            System.out.printf("error in report: %s%n", e.toString());
            return Response.status(Response.Status.BAD_REQUEST).entity("Error generating report: " + e.getMessage()).build();
        }
    }
}