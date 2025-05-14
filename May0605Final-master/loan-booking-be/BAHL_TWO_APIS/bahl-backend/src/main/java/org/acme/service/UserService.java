package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.client.DataAccessClient;
import org.acme.modelDAL.UserDates;
import org.acme.serviceDAL.DataAccessService;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class UserService {

    @Inject
    @RestClient
    DataAccessClient dataAccessClient;

    public UserDates loginAndFetchDates(String userId, int branchCode, String password) {
        DataAccessService.LoginRequest request = new DataAccessService.LoginRequest();
        request.userId = userId;
        request.branchCode = branchCode;
        request.password = password;
        return dataAccessClient.loginAndFetchDates(request);
    }
}