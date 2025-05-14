package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.service.ActivityService;
import java.util.List;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;

@Path("/activities")
@Produces(MediaType.APPLICATION_JSON)
public class ActivityResource {

    @Inject
    ActivityService activityService;

    @GET
    @Path("/{userId}")
    public Response getActivities(@PathParam("userId") String userId) {
        List<String> activities = activityService.getActivitiesForUser(userId);

        if (activities != null) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (String activity : activities) {
                arrayBuilder.add(activity);
            }
            return Response.ok(arrayBuilder.build().toString()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Json.createObjectBuilder().add("error", "User not found or has no activities.").build().toString())
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }
}