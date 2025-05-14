package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.client.UserDatesClient;
import org.acme.modelDAL.SaveUserDatesRequest; // Import from DAL
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDateTime;

@ApplicationScoped
public class UserDatesService {

    @Inject
    @RestClient
    UserDatesClient userDatesClient;

    public LocalDateTime saveUserDates(String userId, int branchCode, String branchName, String currentDate) {
        SaveUserDatesRequest request = new SaveUserDatesRequest(); // Use imported DTO
        request.userId = userId;
        request.branchCode = branchCode;
        request.branchName = branchName;
        request.currentDate = currentDate;
        return userDatesClient.saveUserDates(request);
    }

    public LocalDateTime getPreviousDateForUser(String userId) {
        return userDatesClient.getPreviousDateForUser(userId);
    }

    public LocalDateTime getLatestCurrentDate() {
        return userDatesClient.getLatestCurrentDate();
    }
}