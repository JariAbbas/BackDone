package org.acme.service;

import org.acme.client.AccountRestClient;
import org.acme.modelDAL.Account;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.sql.SQLException;

@ApplicationScoped
public class AccountService {

    @Inject
    @RestClient
    AccountRestClient accountClient;

    @Inject
    LoanLimitService loanLimitService;

    public Account validateAccount(String customerNumber) {
        return accountClient.getAccount(customerNumber);
    }

    public boolean updateAccountBalance(String customerNumber, double amount) throws SQLException {
        Account account = accountClient.getAccount(customerNumber);
        if (account != null) {
            double newBalance = account.getBalance() + amount;
            accountClient.updateBalance(customerNumber, newBalance);
            loanLimitService.updateLimitBalanceByCustomerNumber(customerNumber);
            return true;
        }
        return false;
    }

    public boolean reverseAccountBalance(String customerNumber, double amount) throws SQLException {
        Account account = accountClient.getAccount(customerNumber);
        if (account != null) {
            double newBalance = account.getBalance() - amount;
            accountClient.updateBalance(customerNumber, newBalance);
            loanLimitService.updateLimitBalanceByCustomerNumber(customerNumber);
            return true;
        }
        return false;
    }

    public String getAccountTitle(String customerNumber) {
        return accountClient.getAccountTitle(customerNumber);
    }

}