package org.acme.serviceDAL;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.modelDAL.User;
import org.acme.modelDAL.UserDates;
import org.acme.repository.UserRepository;

import java.time.LocalDateTime;

@Path("/data")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DataAccessService {

    @Inject
    UserRepository userRepository;

    public static class LoginRequest {
        public String userId;
        public int branchCode;
        public String password;
    }

    @POST
    @Path("/login")
    public UserDates loginAndFetchDates(LoginRequest request) {
        User user = userRepository.validateUser(request.userId, request.branchCode, request.password);
        if (user != null) {
            LocalDateTime previousLastSignOn = userRepository.getPreviousLastSignOnDate(request.userId, request.branchCode);
            userRepository.updateLastSignOnDate(request.userId, request.branchCode);
            LocalDateTime currentLastSignOn = LocalDateTime.now();

            return new UserDates(user.getUserId(), user.getBranchCode(), user.getBranchName(), currentLastSignOn, previousLastSignOn);
        }
        return null;
    }
}