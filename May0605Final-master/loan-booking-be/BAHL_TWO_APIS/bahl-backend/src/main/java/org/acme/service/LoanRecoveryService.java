package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.exception.LoanNotFoundException;
import org.acme.exception.RecoveryAlreadyExistsException;
import org.acme.exception.UnauthorizedLoanException;
import org.acme.model.LoanRecoveryDetails;
import org.acme.repository.LoanApplicationRepository;
import org.acme.repository.LoanRecoveryRepository;
import org.acme.repository.RecoveryTransactionRecordRepository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDateTime;

@ApplicationScoped
public class LoanRecoveryService {

    @Inject
    LoanRecoveryRepository loanRecoveryRepository;

    @Inject
    LoanApplicationRepository loanApplicationRepository;

    @Inject
    AccountService accountService;

    @Inject
    RecoveryTransactionRecordRepository recoveryTransactionRecordRepository;

    @Inject
    UserDatesService userDatesService;

    private static final int LOAN_STATUS_READY_FOR_RECOVERY = 9;
    private static final int LOAN_STATUS_RECOVERY_PENDING_AUTHORIZATION = 7;
    private static final int LOAN_STATUS_RECOVERY_COMPLETED = 8;
    private static final int LOAN_STATUS_CANCELLED = 0;
    private static final int LOAN_STATUS_UNAUTHORIZED = 1;

    // Transaction Codes for Recovery
    private static final String CUSTOMER_DEBIT_DEAL_CODE = "36091";
    private static final String ASSET_GL_CREDIT_DEAL_CODE = "86091";
    private static final String CUSTOMER_DEBIT_ACCRUAL_CODE = "30889";
    private static final String INCOME_GL_CREDIT_ACCRUAL_CODE = "80889";

    // Define GL Account Numbers and Titles (Replace with your actual values)
    private static final String ASSET_GL_ACCOUNT = "A01001011000";
    private static final String ASSET_ACCOUNT_TITLE = "Financing";
    private static final String INCOME_GL_ACCOUNT = "I23001010011";
    private static final String INCOME_ACCOUNT_TITLE = "Income";
    private static final String DEFAULT_CURRENCY = "PKR"; // Set your default currency


    private void updateLoanApplicationStatus(String loanNumber, int newStatus) throws SQLException {
        loanApplicationRepository.updateStatusByLoanNumber(loanNumber, newStatus);
    }


    public LoanRecoveryDetails getLoanRecoveryDetails(String loanNumber) throws SQLException, LoanNotFoundException, UnauthorizedLoanException {
        try {
            Integer currentLoanStatus = loanApplicationRepository.getStatusByLoanNumber(loanNumber);
            if (currentLoanStatus == null) {
                throw new LoanNotFoundException("Loan not found with Loan Number: " + loanNumber);
            }
            if (currentLoanStatus == LOAN_STATUS_CANCELLED) {
                throw new UnauthorizedLoanException("Loan Number: " + loanNumber + " is cancelled and cannot be recovered.");
            }
            if (currentLoanStatus == LOAN_STATUS_UNAUTHORIZED) {
                throw new UnauthorizedLoanException("Loan Number: " + loanNumber + " is unauthorized and cannot be recovered.");
            }
            if (currentLoanStatus != LOAN_STATUS_READY_FOR_RECOVERY
                    && currentLoanStatus != LOAN_STATUS_RECOVERY_PENDING_AUTHORIZATION) {
                throw new UnauthorizedLoanException("Loan Number: " + loanNumber
                        + " is not in a valid status for viewing recovery details (current status: " + currentLoanStatus
                        + ").");
            }

            LoanRecoveryDetails details = loanRecoveryRepository.findRecoveryDetailsByLoanNumber(loanNumber);
            if (details == null) {
                throw new LoanNotFoundException("Loan recovery details not found for Loan Number: " + loanNumber + ". Check if loan exists and has associated account/currency/financial data.");
            }
            return details;
        } catch (UnauthorizedLoanException e) {
            throw e; // Re-throw the UnauthorizedLoanException
        }
    }


    public LoanRecoveryDetails performLoanRecovery(String loanNumber, String recoveredByUserId) throws SQLException, LoanNotFoundException, RecoveryAlreadyExistsException, UnauthorizedLoanException {
        // 1. Check current loan status
        Integer currentLoanStatus = loanApplicationRepository.getStatusByLoanNumber(loanNumber);
        if (currentLoanStatus == null) {
            throw new LoanNotFoundException("Loan not found with Loan Number: " + loanNumber);
        }
        if (currentLoanStatus == LOAN_STATUS_CANCELLED) {
            throw new UnauthorizedLoanException("Loan Number: " + loanNumber + " is cancelled and cannot be recovered.");
        }
        if (currentLoanStatus == LOAN_STATUS_UNAUTHORIZED) {
            throw new UnauthorizedLoanException("Loan Number: " + loanNumber + " is unauthorized for recovery.");
        }
        if (currentLoanStatus != LOAN_STATUS_READY_FOR_RECOVERY) {
            throw new UnauthorizedLoanException("Loan Number: " + loanNumber + " is not ready for recovery (current status: " + currentLoanStatus + ").");
        }

        // 2. Check if recovery already exists
        if (loanRecoveryRepository.doesRecoveryExist(loanNumber)) {
            throw new RecoveryAlreadyExistsException("Loan recovery record already exists for Loan Number: " + loanNumber);
        }

        // 3. Fetch recovery details
        LoanRecoveryDetails details = getLoanRecoveryDetails(loanNumber); // Reuses the GET logic, ensures eligibility

        // 4. Get original grant date
        Date originalGrantDate = loanRecoveryRepository.getLoanGrantDate(loanNumber);
        if (originalGrantDate == null) {
            throw new LoanNotFoundException("Original grant date could not be retrieved for Loan Number: " + loanNumber);
        }

        // 5. Save the recovery record
        loanRecoveryRepository.saveRecoveryRecord(loanNumber, details, originalGrantDate, recoveredByUserId);

        // 6. Update LoanApplication status to 'Recovery Pending Authorization'
        updateLoanApplicationStatus(loanNumber, LOAN_STATUS_RECOVERY_PENDING_AUTHORIZATION);

        return details;
    }


    public void sendToAuthorizationQueue(String loanNumber) throws SQLException, LoanNotFoundException, UnauthorizedLoanException {
        Integer currentLoanStatus = loanApplicationRepository.getStatusByLoanNumber(loanNumber);
        if (currentLoanStatus == null) {
            throw new LoanNotFoundException("Loan not found with Loan Number: " + loanNumber);
        }
        if (currentLoanStatus == LOAN_STATUS_CANCELLED) {
            throw new UnauthorizedLoanException("Loan Number: " + loanNumber + " is cancelled.");
        }
        if (currentLoanStatus != LOAN_STATUS_RECOVERY_PENDING_AUTHORIZATION) {
            throw new UnauthorizedLoanException("Loan Number: " + loanNumber + " is not in a state where it can be sent for recovery authorization (current status: " + currentLoanStatus + ").");
        }

        if (!loanRecoveryRepository.doesRecoveryExist(loanNumber)) {
            throw new LoanNotFoundException("No recovery record found for Loan Number: " + loanNumber + ". Please perform recovery first.");
        }

        loanRecoveryRepository.updateRecoveryStatus(loanNumber, 6); // Set LoanRecovery.Status to 6 (Authorization Pending)
        // LoanApplication status is already 7 (Recovery Pending Authorization)
    }


    @Transactional(rollbackOn = Exception.class)
    public void authorizeRecovery(String loanNumber, String authorizedByUserId) throws SQLException, LoanNotFoundException, UnauthorizedLoanException {
        Integer currentLoanStatus = loanApplicationRepository.getStatusByLoanNumber(loanNumber);
        if (currentLoanStatus == null) {
            throw new LoanNotFoundException("Loan not found with Loan Number: " + loanNumber);
        }
        if (currentLoanStatus == LOAN_STATUS_CANCELLED) {
            throw new UnauthorizedLoanException("Loan Number: " + loanNumber + " is cancelled.");
        }
        if (currentLoanStatus != LOAN_STATUS_RECOVERY_PENDING_AUTHORIZATION && currentLoanStatus != 10) {
            throw new UnauthorizedLoanException("Loan Number: " + loanNumber + " is not pending recovery authorization (current status: " + currentLoanStatus + ").");
        }

        if (!loanRecoveryRepository.doesRecoveryExist(loanNumber)) {
            throw new LoanNotFoundException("No recovery record found for Loan Number: " + loanNumber + ".");
        }

        LoanRecoveryDetails recoveryDetails = loanRecoveryRepository.findRecoveryDetailsByLoanNumber(loanNumber);
        if (recoveryDetails == null) {
            throw new LoanNotFoundException("Recovery details not found for Loan Number: " + loanNumber + ".");
        }

        BigDecimal dealAmount = recoveryDetails.getDealAmount();
        BigDecimal accrualAmount = recoveryDetails.getCalculatedAccrual();
        BigDecimal profitAmount = recoveryDetails.getProfitAmount();
        String customerNumber = recoveryDetails.getCustomerNumber();

        LocalDateTime latestCurrentDateTime = userDatesService.getLatestCurrentDate();
        Date transactionDate = null;
        if (latestCurrentDateTime != null) {
            transactionDate = Date.valueOf(latestCurrentDateTime.toLocalDate());
        } else {
            transactionDate = new Date(System.currentTimeMillis());
        }

        // Generate a new VoucherId
        String nextVoucherId = recoveryTransactionRecordRepository.getNextVoucherId(); // Call the new method

        // Record transactions for Deal Amount Recovery
        recoveryTransactionRecordRepository.create(
                CUSTOMER_DEBIT_DEAL_CODE,
                loanNumber,
                customerNumber,
                accountService.getAccountTitle(customerNumber),
                DEFAULT_CURRENCY,
                dealAmount,
                BigDecimal.ZERO,
                transactionDate,
                nextVoucherId // Set the VoucherId
        );
        recoveryTransactionRecordRepository.create(
                ASSET_GL_CREDIT_DEAL_CODE,
                loanNumber,
                ASSET_GL_ACCOUNT,
                ASSET_ACCOUNT_TITLE,
                DEFAULT_CURRENCY,
                BigDecimal.ZERO,
                dealAmount,
                transactionDate,
                nextVoucherId // Set the VoucherId
        );

        // Record transactions for Accrual Recovery
        recoveryTransactionRecordRepository.create(
                CUSTOMER_DEBIT_ACCRUAL_CODE,
                loanNumber,
                customerNumber,
                accountService.getAccountTitle(customerNumber),
                DEFAULT_CURRENCY,
                accrualAmount,
                BigDecimal.ZERO,
                transactionDate,
                nextVoucherId // Set the VoucherId
        );
        recoveryTransactionRecordRepository.create(
                INCOME_GL_CREDIT_ACCRUAL_CODE,
                loanNumber,
                INCOME_GL_ACCOUNT,
                INCOME_ACCOUNT_TITLE,
                DEFAULT_CURRENCY,
                BigDecimal.ZERO,
                accrualAmount,
                transactionDate,
                nextVoucherId // Set the VoucherId
        );

        loanRecoveryRepository.updateRecoveryStatus(loanNumber, 9);
        loanRecoveryRepository.updateAuthorizedBy(loanNumber, authorizedByUserId);

        updateLoanApplicationStatus(loanNumber, LOAN_STATUS_RECOVERY_COMPLETED);
    }


    // You might also need a method to handle rejection of recovery authorization
    public void rejectRecovery(String loanNumber) throws SQLException, LoanNotFoundException, UnauthorizedLoanException {
        Integer currentLoanStatus = loanApplicationRepository.getStatusByLoanNumber(loanNumber);
        if (currentLoanStatus == null) {
            throw new LoanNotFoundException("Loan not found with Loan Number: " + loanNumber);
        }
        if (currentLoanStatus == LOAN_STATUS_CANCELLED) {
            throw new UnauthorizedLoanException("Loan Number: " + loanNumber + " is cancelled.");
        }
        if (currentLoanStatus != LOAN_STATUS_RECOVERY_PENDING_AUTHORIZATION) {
            throw new UnauthorizedLoanException("Loan Number: " + loanNumber + " is not pending recovery authorization (current status: " + currentLoanStatus + ").");
        }

        if (!loanRecoveryRepository.doesRecoveryExist(loanNumber)) {
            throw new LoanNotFoundException("No recovery record found for Loan Number: " + loanNumber + ".");
        }

        loanRecoveryRepository.updateRecoveryStatus(loanNumber, 987); // Or some other rejected status in LoanRecovery
        updateLoanApplicationStatus(loanNumber, 987); // Revert LoanApplication status
    }
}