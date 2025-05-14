package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import org.acme.modelDAL.Account;
import org.acme.model.LoanLimit;
import org.acme.repository.LoanLimitRepository;
import org.acme.repository.UnauthorizedLoanLimitRepository;
import java.util.List;

@ApplicationScoped
public class LoanLimitService {

    @Inject
    LoanLimitRepository loanLimitRepository;

    @Inject
    UnauthorizedLoanLimitRepository unauthorizedLoanLimitRepository; // Inject the new repository

    @Inject
    AccountService accountService;

    public LoanLimit createLoanLimit(String customerNumber, double loanLimit, double balance, String userId) {
        // Check if a loan limit already exists for this customer (authorized or unauthorized)
        if (loanLimitRepository.getLoanLimitByCustomerNumber(customerNumber) != null ||
                unauthorizedLoanLimitRepository.getUnauthorizedLoanLimitByCustomerNumber(customerNumber) != null) {
            return null; // Indicate that a limit already exists
        }

        double limitBalance = loanLimit - balance;
        LoanLimit newLoanLimit = new LoanLimit();
        newLoanLimit.setCustomerNumber(customerNumber);
        newLoanLimit.setLoanLimit(loanLimit);
        newLoanLimit.setLimitBalance(limitBalance);
        newLoanLimit.setCreatedBy(userId);
        newLoanLimit.setCreatedOn(LocalDateTime.now());
        newLoanLimit.setUpdatedBy(userId);
        newLoanLimit.setUpdatedOn(LocalDateTime.now());

        // Save to UnauthorizedLoanLimits
        if (unauthorizedLoanLimitRepository.createUnauthorizedLoanLimit(newLoanLimit)) {
            return newLoanLimit; // Return the DTO
        }
        return null;
    }

    public LoanLimit updateLoanLimit(String customerNumber, double newLoanLimit, String userId) {
        LoanLimit existingLoanLimit = loanLimitRepository.getLoanLimitByCustomerNumber(customerNumber); //yeh authorized se la raha hai

        if (existingLoanLimit == null) {
            return null;
        }
        Account account = accountService.validateAccount(customerNumber);
        if (account != null) {
            double currentBalance = account.getBalance();
            double limitBalance = newLoanLimit - currentBalance;
            existingLoanLimit.setLoanLimit(newLoanLimit);
            existingLoanLimit.setLimitBalance(limitBalance);
            existingLoanLimit.setUpdatedBy(userId);
            existingLoanLimit.setUpdatedOn(LocalDateTime.now());
            if (loanLimitRepository.updateLoanLimit(existingLoanLimit)) {
                return existingLoanLimit;
            }
        }
        return null;
    }

    public LoanLimit getLoanLimitByCustomerNumber(String customerNumber) {
        return loanLimitRepository.getLoanLimitByCustomerNumber(customerNumber);
    }

    // New method to authorize a loan limit
    public LoanLimit authorizeLoanLimit(String customerNumber, String authorizedBy) {
        // 1. Get from Unauthorized table
        LoanLimit unauthorizedLoanLimit = unauthorizedLoanLimitRepository.getUnauthorizedLoanLimitByCustomerNumber(customerNumber);
        if (unauthorizedLoanLimit == null) {
            return null;
        }

        // 2. Check if an authorized limit already exists (shouldn't happen if flow is correct)
        if (loanLimitRepository.getLoanLimitByCustomerNumber(customerNumber) != null) {
            // Consider logging this as an inconsistency
            return null;
        }

        // 3.  Copy to LoanLimits table
        LoanLimit authorizedLoanLimit = new LoanLimit();
        authorizedLoanLimit.setCustomerNumber(unauthorizedLoanLimit.getCustomerNumber());
        authorizedLoanLimit.setLoanLimit(unauthorizedLoanLimit.getLoanLimit());
        authorizedLoanLimit.setLimitBalance(unauthorizedLoanLimit.getLimitBalance());
        authorizedLoanLimit.setCreatedBy(unauthorizedLoanLimit.getCreatedBy());
        authorizedLoanLimit.setCreatedOn(unauthorizedLoanLimit.getCreatedOn());
        authorizedLoanLimit.setUpdatedBy(authorizedBy); // Set the authorizer
        authorizedLoanLimit.setUpdatedOn(LocalDateTime.now());
        authorizedLoanLimit.setStatus(9); // Set status to 9 (Authorized)
        if (!loanLimitRepository.createLoanLimit(authorizedLoanLimit)) {
            return null; // Or throw an exception
        }

        // 4. Delete from Unauthorized table
        unauthorizedLoanLimitRepository.deleteUnauthorizedLoanLimit(customerNumber);
        return authorizedLoanLimit;
    }

    public LoanLimit getUnauthorizedLoanLimitByCustomerNumber(String customerNumber) {
        return unauthorizedLoanLimitRepository.getUnauthorizedLoanLimitByCustomerNumber(customerNumber);
    }

    public List<LoanLimit> getUnauthorizedLoanLimits() {
        return unauthorizedLoanLimitRepository.getUnauthorizedLoanLimits();
    }


    public void updateLimitBalanceByCustomerNumber(String customerNumber) {
        LoanLimit loanLimit = loanLimitRepository.getLoanLimitByCustomerNumber(customerNumber); //yeh authorized table se la raha

        if (loanLimit != null) {
            Account account = accountService.validateAccount(customerNumber);
            if (account != null) {
                double currentBalance = account.getBalance();
                double newLimitBalance = loanLimit.getLoanLimit() - currentBalance;
                loanLimit.setLimitBalance(newLimitBalance);
                loanLimit.setUpdatedBy("System");
                loanLimit.setUpdatedOn(LocalDateTime.now());
                loanLimitRepository.updateLoanLimitOnBalanceChange(loanLimit);
            }
        }
    }

    public boolean rejectLoanLimit(String customerNumber) {
        // 1. Check if an unauthorized loan limit exists
        LoanLimit unauthorizedLoanLimit = unauthorizedLoanLimitRepository.getUnauthorizedLoanLimitByCustomerNumber(customerNumber);
        if (unauthorizedLoanLimit == null) {
            return false; // Or handle this case as you see fit (e.g., throw exception)
        }

        // 2. Delete the unauthorized loan limit
        return unauthorizedLoanLimitRepository.deleteUnauthorizedLoanLimit(customerNumber);
    }
}