package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.client.UserDatesClient;
import org.acme.modelDAL.SaveUserDatesRequest;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDateTime;

@Path("/set_dates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserDatesResource {

    @Inject
    @RestClient
    UserDatesClient userDatesClient;

    public static class SetDatesRequest {
        public String userId;
        public int branchCode;
        public String branchName;
        public String currentDate;
    }

    @GET
    @Path("/{userId}")
    public Response getPreviousDate(@PathParam("userId") String userId) {
        LocalDateTime previousDate = userDatesClient.getPreviousDateForUser(userId);
        if (previousDate != null) {
            JsonObject response = Json.createObjectBuilder()
                    .add("previousDate", previousDate.toString().split("T")[0])
                    .build();
            return Response.ok(response.toString()).build();
        } else {
            JsonObject emptyResponse = Json.createObjectBuilder()
                    .add("previousDate", "") // Return an empty string instead of null
                    .build();
            return Response.ok(emptyResponse.toString()).build();
        }
    }

    @GET
    @Path("/latest")
    public Response getLatestCurrentDate() {
        LocalDateTime latestDate = userDatesClient.getLatestCurrentDate();
        if (latestDate != null) {
            JsonObject response = Json.createObjectBuilder()
                    .add("currentDate", latestDate.toString().split("T")[0])
                    .build();
            return Response.ok(response.toString()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }


    @POST
    public Response setUserDates(SetDatesRequest request) {
        try {
            if (request.userId == null || request.currentDate == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Invalid request data\"}")
                        .build();
            }

            SaveUserDatesRequest clientRequest = new SaveUserDatesRequest();
            clientRequest.userId = request.userId;
            clientRequest.branchCode = request.branchCode;
            clientRequest.branchName = request.branchName;
            clientRequest.currentDate = request.currentDate;

            LocalDateTime previousDate = userDatesClient.saveUserDates(clientRequest);

            JsonObjectBuilder successResponse = Json.createObjectBuilder()
                    .add("message", "Dates saved successfully");

            if (previousDate != null) {
                successResponse.add("previousDate", previousDate.toString().split("T")[0]);
            } else {
                successResponse.add("previousDate", "");  // Return empty string instead of null
            }

            return Response.ok(successResponse.build().toString()).build();
        } catch (Exception e) {
            JsonObject errorResponse = Json.createObjectBuilder()
                    .add("error", "Failed to save dates: " + e.getMessage()) // Debugging info added
                    .build();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse.toString())
                    .build();
        }
    }
}