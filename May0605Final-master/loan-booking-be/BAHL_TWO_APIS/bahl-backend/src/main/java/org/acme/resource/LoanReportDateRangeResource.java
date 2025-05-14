package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.service.LoanReportDateRangeService;
import org.eclipse.birt.core.exception.BirtException;

import java.util.HashMap;
import java.util.Map;

@Path("/loanReportDateRange")
public class LoanReportDateRangeResource {

    @Inject
    LoanReportDateRangeService birtService;


    @GET
    @Path("/generate")
    @Produces(MediaType.TEXT_HTML)
    public Response generateReport(
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate,
            @QueryParam("startDate2") String startDate2,
            @QueryParam("endDate2") String endDate2) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("startDate2", startDate2);
            params.put("endDate2", endDate2);

            byte[] bytes = birtService.generateReport(params);
            return Response.ok(bytes)
                    .header("Content-Disposition", "inline; filename=loan_report_date_range_" + startDate + "_" + endDate + "_" + startDate2 + "_" + endDate2 + ".pdf")
                    .header("Content-Type", "application/pdf")
                    .build();
        } catch (BirtException e) {
            System.out.printf("error in report: %s%n", e.toString());
            return Response.status(Response.Status.BAD_REQUEST).entity("Error generating report: " + e.getMessage()).build();
        }
    }
}