package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.modelDAL.UserDates;
import org.acme.service.UserService;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

    public static class LoginRequest {
        public String userId;
        public int branchCode;
        public String password;
    }

    @POST
    public Response login(LoginRequest request) {
        UserDates userDates = userService.loginAndFetchDates(request.userId, request.branchCode, request.password);
        System.out.println("API YAHHHHHHH !!!!! --------------------------->");
        if (userDates != null) {
            JsonObjectBuilder successResponseBuilder = Json.createObjectBuilder()
                    .add("userId", userDates.getUserId())
                    .add("branchCode", userDates.getBranchCode())
                    .add("branchName", userDates.getBranchName())
                    .add("currentSignOnDate", userDates.getCurrentDate().toString());

            if (userDates.getPreviousDate() != null) {
                successResponseBuilder.add("previousSignOnDate", userDates.getPreviousDate().toString());
            } else {
                successResponseBuilder.addNull("previousSignOnDate");
            }

            JsonObject successResponse = successResponseBuilder.build();
            return Response.ok(successResponse.toString()).build();

        }

        JsonObject errorResponse = Json.createObjectBuilder()
                .add("error", "Invalid Credentials")
                .build();

        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(errorResponse.toString())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}