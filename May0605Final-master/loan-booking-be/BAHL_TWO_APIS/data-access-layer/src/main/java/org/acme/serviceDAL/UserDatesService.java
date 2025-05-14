package org.acme.serviceDAL;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.modelDAL.UserDates;
import org.acme.repository.UserDatesRepository;
import org.acme.modelDAL.SaveUserDatesRequest;
import java.time.LocalDateTime;

@Path("/user_dates")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserDatesService {

    @Inject
    UserDatesRepository userDatesRepository;

    @POST
    @Path("/save")
    public LocalDateTime saveUserDates(SaveUserDatesRequest request) {
        LocalDateTime parsedDate = LocalDateTime.parse(request.currentDate + "T00:00:00");
        LocalDateTime previousDate = userDatesRepository.getPreviousDateForUser(request.userId);
        UserDates userDates = new UserDates(request.userId, request.branchCode, request.branchName, parsedDate, previousDate);
        userDatesRepository.saveUserDates(userDates);
        return previousDate;
    }

    @GET
    @Path("/{userId}/previous")
    public LocalDateTime getPreviousDateForUser(@PathParam("userId") String userId) {
        return userDatesRepository.getPreviousDateForUser(userId);
    }

    @GET
    @Path("/latest")
    public LocalDateTime getLatestCurrentDate() {
        UserDates latestUserDate = userDatesRepository.findLatest();
        if (latestUserDate != null) {
            return latestUserDate.getCurrentDate();
        }
        return null;
    }


}