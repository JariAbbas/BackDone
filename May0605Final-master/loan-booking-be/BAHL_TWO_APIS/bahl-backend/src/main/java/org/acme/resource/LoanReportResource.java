package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.service.LoanReportService;
import org.eclipse.birt.core.exception.BirtException;

import java.util.HashMap;
import java.util.Map;

@Path("/loanReport")
public class LoanReportResource {

    @Inject
    LoanReportService birtService;

    @GET
    @Path("/generate")
    @Produces(MediaType.TEXT_HTML)
    public Response generateReport(
            @QueryParam("startrange") Integer startrange,
            @QueryParam("endrange") Integer endrange,
            @QueryParam("startrange2") Integer startrange2,
            @QueryParam("endrange2") Integer endrange2) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("startrange", startrange);
            params.put("endrange", endrange);
            params.put("startrange2", startrange2);
            params.put("endrange2", endrange2);

            byte[] bytes = birtService.generateReport(params);
            return Response.ok(bytes)
                    .header("Content-Disposition", "inline; filename=loan_report_" + startrange + "_" + endrange + "_" + startrange2 + "_" + endrange2 + ".pdf")
                    .header("Content-Type", "application/pdf")
                    .build();
        } catch (BirtException e) {
            System.out.printf("error in report: %s%n", e.toString());
            return Response.status(Response.Status.BAD_REQUEST).entity("Error generating report: " + e.getMessage()).build();
        }
    }
}