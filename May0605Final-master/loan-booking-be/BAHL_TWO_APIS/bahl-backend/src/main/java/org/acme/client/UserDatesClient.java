package org.acme.client;

import io.quarkus.arc.Unremovable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.acme.modelDAL.SaveUserDatesRequest;
import java.time.LocalDateTime;

@Path("/user_dates")
@RegisterRestClient
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Unremovable // Add Unremovable annotation
public interface UserDatesClient {

    @POST
    @Path("/save")
    LocalDateTime saveUserDates(SaveUserDatesRequest request);

    @GET
    @Path("/{userId}/previous")
    LocalDateTime getPreviousDateForUser(@PathParam("userId") String userId);

    @GET
    @Path("/latest")
    LocalDateTime getLatestCurrentDate();
}