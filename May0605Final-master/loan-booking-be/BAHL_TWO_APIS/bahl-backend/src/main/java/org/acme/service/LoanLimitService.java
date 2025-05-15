package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal; //NEW
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
        // Check if a loan limit already exists for this customer (authorized or
        // unauthorized)
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
        LoanLimit existingLoanLimit = loanLimitRepository.getLoanLimitByCustomerNumber(customerNumber); // yeh
        // authorized se
        // la raha hai

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
        LoanLimit unauthorizedLoanLimit = unauthorizedLoanLimitRepository
                .getUnauthorizedLoanLimitByCustomerNumber(customerNumber);
        if (unauthorizedLoanLimit == null) {
            return null;
        }

        // 2. Check if an authorized limit already exists (shouldn't happen if flow is
        // correct)
        if (loanLimitRepository.getLoanLimitByCustomerNumber(customerNumber) != null) {
            // Consider logging this as an inconsistency
            return null;
        }

        // 3. Copy to LoanLimits table
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
        LoanLimit loanLimit = loanLimitRepository.getLoanLimitByCustomerNumber(customerNumber); // yeh authorized table
        // se la raha

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
        LoanLimit unauthorizedLoanLimit = unauthorizedLoanLimitRepository
                .getUnauthorizedLoanLimitByCustomerNumber(customerNumber);
        if (unauthorizedLoanLimit == null) {
            return false; // Or handle this case as you see fit (e.g., throw exception)
        }

        // 2. Delete the unauthorized loan limit
        return unauthorizedLoanLimitRepository.deleteUnauthorizedLoanLimit(customerNumber);
    }

    // To credit back deal amount on successful loan recovery >>>
    @Transactional // Ensure this method runs in its own transaction or participates in an existing
    // one
    public void creditBackDealAmount(String customerNumber, BigDecimal recoveredDealAmount, String updatedByUserId) {
        if (customerNumber == null || recoveredDealAmount == null
                || recoveredDealAmount.compareTo(BigDecimal.ZERO) <= 0) {
            String errorMsg = "Invalid parameters for creditBackDealAmount: customerNumber or recoveredDealAmount is invalid.";
            System.err.println(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        LoanLimit existingLoanLimit = loanLimitRepository.getLoanLimitByCustomerNumber(customerNumber);
        if (existingLoanLimit == null) {
            String errorMsg = "Critical Error: No authorized loan limit found for customer: " + customerNumber
                    + " to credit back recovered amount. Recovery process cannot update limit.";
            System.err.println(errorMsg);
            // This is a critical issue. A loan was recovered, so a limit record should
            // exist.
            throw new RuntimeException(errorMsg); // Or a more specific custom exception
        }

        double currentLimitBalance = existingLoanLimit.getLimitBalance();
        double amountToCreditBack = recoveredDealAmount.doubleValue(); // Convert BigDecimal to double

        double newLimitBalance = currentLimitBalance + amountToCreditBack;

        // Cap the new limit balance at the total sanctioned limit
        if (newLimitBalance > existingLoanLimit.getLoanLimit()) {
            newLimitBalance = existingLoanLimit.getLoanLimit();
            System.err.println("Warning: Calculated new limit balance for customer " + customerNumber
                    + " exceeded total limit (" + existingLoanLimit.getLoanLimit()
                    + "). Capping at total limit: " + newLimitBalance);
        }

        existingLoanLimit.setLimitBalance(newLimitBalance);
        existingLoanLimit.setUpdatedBy(updatedByUserId);
        existingLoanLimit.setUpdatedOn(LocalDateTime.now());

        boolean success = loanLimitRepository.updateLoanLimit(existingLoanLimit);
        if (!success) {
            String errorMsg = "Critical Error: Failed to update loan limit balance in repository for customer: "
                    + customerNumber
                    + " after recovery credit back.";
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg); // Or a more specific custom persistence exception
        }
        System.out.println("Successfully updated limit balance for customer " + customerNumber + " to "
                + newLimitBalance + " after recovery.");
    }
}