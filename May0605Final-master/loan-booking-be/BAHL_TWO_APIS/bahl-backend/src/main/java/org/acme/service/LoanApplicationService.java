package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.client.UserDatesClient; // Import UserDatesClient
import org.acme.model.LoanApplication;
import org.acme.modelDAL.UserDates;
import org.acme.repository.LoanApplicationRepository;
import org.acme.repository.BookingTransactionRecordRepository;
import org.eclipse.microprofile.rest.client.inject.RestClient;
// import org.acme.repository.UserDatesRepository; // Remove UserDatesRepository import

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class LoanApplicationService {

    @Inject
    LoanApplicationRepository loanApplicationRepository;

    @Inject
    BookingTransactionRecordRepository bookingTransactionRecordRepository; // Inject BookingTransactionRecordRepository

    @Inject
    AccountService accountService; // Inject AccountService

    @Inject
    @RestClient
    UserDatesClient userDatesClient; // Inject UserDatesClient

    // Define the Asset GL Account Number and Title (replace with your actual values)
    private static final String ASSET_GL_ACCOUNT = "A01001011000";
    private static final String ASSET_ACCOUNT_TITLE = "Financing";
    private static final String DEFAULT_CURRENCY = "PKR"; // Set your default currency
    private static final String LOAN_CREATION_DEBIT_CODE = "36036";
    private static final String LOAN_CREATION_CREDIT_CODE = "86036";
    private static final String LOAN_REJECTION_DEBIT_CODE = "86036"; // Reverse of credit
    private static final String LOAN_REJECTION_CREDIT_CODE = "36036"; // Reverse of debit

    public List<LoanApplication> listAll() throws SQLException {
        return loanApplicationRepository.listAll();
    }

    public LoanApplication findById(int id) throws SQLException {
        return loanApplicationRepository.findById(id);
    }

    public LoanApplication findByLoanNumber(String loanNumber) throws SQLException {
        return loanApplicationRepository.findByLoanNumber(loanNumber);
    }

    public void updateStatusByLoanNumber(String loanNumber, int status) throws SQLException {
        loanApplicationRepository.updateStatusByLoanNumber(loanNumber, status);
    }

    public LocalDateTime getCurrentUserDateForLoanBooking() {
        // Use the client to fetch the user date
        return userDatesClient.getPreviousDateForUser("Creditusr"); // Assuming this client method is appropriate
    }

    @Transactional(rollbackOn = Exception.class)
    public void create(LoanApplication loan) throws SQLException {
        // Calculate initial accrual on the backend
        // Fetch UserDates specifically for Creditusr using the client
        LocalDateTime userDateTime = userDatesClient.getPreviousDateForUser("Creditusr");
        Date transactionDate = null;
        if (userDateTime != null) {
            loan.setGrantDate(Date.valueOf(userDateTime.toLocalDate()));
            transactionDate = Date.valueOf(userDateTime.toLocalDate());
        } else {
            loan.setGrantDate(Date.valueOf(LocalDate.now()));
            transactionDate = Date.valueOf(LocalDate.now());
        }

        if (loan.getDealAmount() != null && loan.getApplicableRate() != null && loan.getNoOfDays() != null) {
            double accrual = (loan.getDealAmount() * loan.getApplicableRate() / 36500) * loan.getNoOfDays(); // Total accrual for the period
            loan.setAccrual(accrual);
        }
        loanApplicationRepository.create(loan);

        // Fetch the LoanID of the newly created loan
        LoanApplication createdLoan = loanApplicationRepository.findByLoanNumber(loan.getLoanNumber());
        Integer loanId = null;
        if (createdLoan != null) {
            loanId = createdLoan.getLoanId();
        } else {
            // Handle the case where the loan wasn't created successfully (shouldn't happen if create doesn't throw exception)
            throw new SQLException("Failed to retrieve LoanID after creating loan number: " + loan.getLoanNumber());
        }

        BigDecimal dealAmount = BigDecimal.valueOf(loan.getDealAmount());
        String loanNumber = loan.getLoanNumber();
        String customerNumber = loan.getCustomerNumber();

        // Record transaction for Debit (Asset GL)
        bookingTransactionRecordRepository.create(
                LOAN_CREATION_DEBIT_CODE,
                loanNumber,
                ASSET_GL_ACCOUNT,
                ASSET_ACCOUNT_TITLE,
                DEFAULT_CURRENCY,
                dealAmount,
                BigDecimal.ZERO,
                transactionDate,
                loanId // Pass the LoanID
        );

        // Record transaction for Credit (Customer Number)
        String customerAccountTitle = accountService.getAccountTitle(customerNumber);
        bookingTransactionRecordRepository.create(
                LOAN_CREATION_CREDIT_CODE,
                loanNumber,
                customerNumber,
                customerAccountTitle,
                DEFAULT_CURRENCY,
                BigDecimal.ZERO,
                dealAmount,
                transactionDate,
                loanId // Pass the LoanID
        );

        // --- MODIFICATION START: Remove account balance update from loan creation ---
        // accountService.updateAccountBalance(customerNumber, loan.getDealAmount()); // OLD LINE - REMOVED
        // --- MODIFICATION END ---
    }

    // --- MODIFICATION START: New method to approve a loan application ---
    @Transactional(rollbackOn = Exception.class)
    public LoanApplication approveLoanApplication(String loanNumber, String authorizedBy) throws SQLException, IllegalStateException {
        LoanApplication loan = loanApplicationRepository.findByLoanNumber(loanNumber);
        if (loan == null) {
            throw new SQLException("Loan not found with Loan Number: " + loanNumber + " for approval.");
        }

        if (loan.getStatus() == 1) { // Case 1: Initial approval of a newly created loan
            System.out.println("Performing initial approval for loan: " + loanNumber + ". Status: 1 -> 9");
            // This is the first time this loan is being made financially active.
            // Update account balance which will trigger limit balance update.
            accountService.updateAccountBalance(loan.getCustomerNumber(), loan.getDealAmount());

            loan.setStatus(9); // Set status to 9 (Active / Authorized)
            loan.setAuthorizedBy(authorizedBy);
            // Potentially log that initial financial impact has occurred.

        } else if (loan.getStatus() == 2) { // Case 2: Approving a modification to an already active loan
            System.out.println("Approving modification for already active loan: " + loanNumber + ". Status: 2 -> 9");
            // The loan was previously active (Status 9), then modified (became Status 2).
            // We are now authorizing these modifications (e.g., remarks, noOfDays).
            // DO NOT call accountService.updateAccountBalance() again for the original dealAmount,
            // as the financial impact for that amount already happened when it first became active.
            // If the modification itself involved a change in dealAmount, that would require a
            // different, more complex process (e.g., reverse old, book new amount, re-check limits),
            // which is not implied by the user story for simple modifications.

            loan.setStatus(9); // Set status back to 9 (Active, with modifications approved)
            loan.setAuthorizedBy(authorizedBy); // Record who authorized the modification
            // Potentially log that a modification was approved without new financial impact on original deal amount.

        } else {
            // Loan is in a state that cannot be approved by this method.
            throw new IllegalStateException("Loan with Loan Number: " + loanNumber +
                    " is not in a state for this approval flow (must be Status 1 or 2). Current status: " + loan.getStatus());
        }

        // Persist the changes to the loan (status, authorizedBy)
        boolean updated = loanApplicationRepository.update(loan); // Assumes your repo's update method handles these fields
        if (!updated) {
            // This might occur if the update method returns false on failure (e.g., loanId not found by that specific update method)
            throw new SQLException("Failed to update loan status/authorizedBy for Loan Number: " + loanNumber);
        }

        return loan; // Return the updated loan object
    }    // --- MODIFICATION END ---

    public LoanApplication updateByLoanNumber(String loanNumber, LoanApplication loan) throws SQLException {
        // We no longer try to update the GrantDate here.
        // The GrantDate will remain as it was originally set.

        boolean updated = loanApplicationRepository.updateByLoanNumber(loanNumber, loan);
        if (updated) {
            return loanApplicationRepository.findByLoanNumber(loanNumber); // Fetch and return the updated entity
        }
        return null; // Or throw an exception if update failed
    }

    @Transactional(rollbackOn = Exception.class)
    public boolean rejectLoan(String loanNumber) throws SQLException {
        LoanApplication loan = loanApplicationRepository.findByLoanNumber(loanNumber);
        if (loan == null) {
            // If loan not found, you might want to throw an exception or return a more specific error.
            // For now, returning false as per original structure, but indicating failure.
            throw new SQLException("Loan not found with Loan Number: " + loanNumber + " for rejection.");
        }

        // --- MODIFICATION START: Remove account balance reversal if loan was never financially active ---
        // If the loan was never approved (e.g., status 1 or 2), its balance was never updated.
        // So, no need to reverse it. The GL entries for parked transactions are reversed below.
        // accountService.reverseAccountBalance(loan.getCustomerNumber(), loan.getDealAmount()); // OLD LINE - REMOVED
        // --- MODIFICATION END ---

        // Fetch UserDates for transaction date using the client
        LocalDateTime userDateTime = userDatesClient.getPreviousDateForUser("Creditusr");
        Date transactionDate = null;
        if (userDateTime != null) {
            transactionDate = Date.valueOf(userDateTime.toLocalDate());
        } else {
            transactionDate = Date.valueOf(LocalDate.now());
        }

        BigDecimal dealAmount = BigDecimal.valueOf(loan.getDealAmount());
        Integer loanId = loan.getLoanId();
        String customerNumber = loan.getCustomerNumber();
        String customerAccountTitle = accountService.getAccountTitle(customerNumber);

        // Record reversing transaction for Debit (Customer Number) - This is reversing the parked GL
        bookingTransactionRecordRepository.create(
                LOAN_REJECTION_DEBIT_CODE,
                loanNumber,
                customerNumber,
                customerAccountTitle,
                DEFAULT_CURRENCY,
                dealAmount,
                BigDecimal.ZERO,
                transactionDate,
                loanId
        );

        // Record reversing transaction for Credit (Asset GL) - This is reversing the parked GL
        bookingTransactionRecordRepository.create(
                LOAN_REJECTION_CREDIT_CODE,
                loanNumber,
                ASSET_GL_ACCOUNT,
                ASSET_ACCOUNT_TITLE,
                DEFAULT_CURRENCY,
                BigDecimal.ZERO,
                dealAmount,
                transactionDate,
                loanId
        );

        // --- MODIFICATION START: Update status to 987 (Rejected) for consistency with Resource layer ---
        // loanApplicationRepository.updateLoanApplicationStatus(loanNumber, 4); // OLD LINE - Status 4
        loanApplicationRepository.updateLoanApplicationStatus(loanNumber, 987); // NEW LINE - Status 987 (Rejected)
        // --- MODIFICATION END ---
        return true;
    }

    public boolean cancelLoan(String loanNumber) throws SQLException {
        return loanApplicationRepository.cancelLoan(loanNumber);
    }

    public int getCancelledLoanStatus(String loanNumber) throws SQLException {
        return loanApplicationRepository.getCancelledLoanStatus(loanNumber);
    }

    public void updateCancelledLoanStatus(String loanNumber, int status) throws SQLException {loanApplicationRepository.updateCancelledLoanStatus(loanNumber, status);
    }

    public void updateLoanApplicationStatus(String loanNumber, int status) throws SQLException {
        loanApplicationRepository.updateLoanApplicationStatus(loanNumber, status);
    }

    public List<LoanApplication> findLoansAwaitingCancellationAuthorization() throws SQLException {
        return loanApplicationRepository.findLoansAwaitingCancellationAuthorization();
    }

    @Transactional(rollbackOn = Exception.class)
    public boolean authorizeLoanCancellation(String loanNumber) throws SQLException {
        LoanApplication loan = loanApplicationRepository.findByLoanNumber(loanNumber);
        // This loan object, when fetched here, would typically have Status = 0,
        // because the LoanApplicationResource.cancelLoan endpoint calls
        // loanApplicationService.updateLoanApplicationStatus(loanNumber, 0)
        // before the /authorize-cancellation endpoint (which calls this service method) is hit.

        if (loan != null) {
            // --- MODIFICATION START: Remove account balance reversal for never-approved loans ---
            // The LoanApplicationResource.cancelLoan endpoint, which leads to this flow,
            // has checks ensuring that only loans not yet Active (e.g., status 1)
            // can be put into this cancellation process (it errors for status 2 or 9).
            // Therefore, a loan reaching this point for cancellation authorization was likely never financially active.
            // accountService.reverseAccountBalance(loan.getCustomerNumber(), loan.getDealAmount()); // OLD LINE - REMOVED
            // --- MODIFICATION END ---


            // Fetch UserDates for transaction date using the client
            LocalDateTime userDateTime = userDatesClient.getPreviousDateForUser("Creditusr");
            Date transactionDate = null;
            if (userDateTime != null) {
                transactionDate = Date.valueOf(userDateTime.toLocalDate());
            } else {
                transactionDate = Date.valueOf(LocalDate.now());
            }

            BigDecimal dealAmount = BigDecimal.valueOf(loan.getDealAmount());
            Integer loanId = loan.getLoanId(); // LoanID is available on the loan object
            String customerNumber = loan.getCustomerNumber();
            String customerAccountTitle = accountService.getAccountTitle(customerNumber);

            // Record reversing transaction for Debit (Customer Number) - Reversing parked GLs
            bookingTransactionRecordRepository.create(
                    LOAN_REJECTION_DEBIT_CODE, // Using rejection codes for cancellation reversal of parked GLs
                    loanNumber,
                    customerNumber,
                    customerAccountTitle,
                    DEFAULT_CURRENCY,
                    dealAmount,
                    BigDecimal.ZERO,
                    transactionDate,
                    loanId
            );

            // Record reversing transaction for Credit (Asset GL) - Reversing parked GLs
            bookingTransactionRecordRepository.create(
                    LOAN_REJECTION_CREDIT_CODE, // Using rejection codes for cancellation reversal of parked GLs
                    loanNumber,
                    ASSET_GL_ACCOUNT,
                    ASSET_ACCOUNT_TITLE,
                    DEFAULT_CURRENCY,
                    BigDecimal.ZERO,
                    dealAmount,
                    transactionDate,
                    loanId
            );

            loanApplicationRepository.updateCancelledLoanStatus(loanNumber, 9); // 9 = Cancellation Authorized
            loanApplicationRepository.deleteLoanApplication(loanNumber); // Delete from LoanApplication table
            return true;
        }
        return false;
    }

    public boolean rejectLoanCancellation(String loanNumber) throws SQLException {
        // Update status in CancelledLoan table to 987 (Rejected Cancellation)
        loanApplicationRepository.updateCancelledLoanStatus(loanNumber, 987);
        // Also update status in LoanApplication table back to 1 (Unauthorized) or its original pre-cancellation request status.
        // The current code updates LoanApplication status to 987, which might be confusing if it means "Loan Rejected"
        // vs "Cancellation Rejected".
        // For minimal change, we'll keep the existing logic of setting LoanApplication status to 987.
        // A more robust flow might revert it to status 1.
        loanApplicationRepository.updateLoanApplicationStatus(loanNumber, 1); // Revert to Unauthorized, as cancellation was rejected
        return true;
    }

    public boolean checkLoanExists(String loanNumber) throws SQLException {
        return loanApplicationRepository.findByLoanNumber(loanNumber) != null;
    }

    public boolean checkCancelledLoanExists(String loanNumber) throws SQLException {
        return loanApplicationRepository.checkCancelledLoanExists(loanNumber);
    }

    // Method to fetch the current date from UserDates using the client
    public LocalDate getCurrentDateFromUserDates() {
        LocalDateTime userDateTime = userDatesClient.getLatestCurrentDate();
        if (userDateTime != null) {
            return userDateTime.toLocalDate();
        }
        // Fallback or error handling if needed
        // For example, throw new IllegalStateException("Could not fetch current date from UserDates service.");
        // Or return LocalDate.now() as a last resort, though relying on the service is preferred.
        System.err.println("Warning: Could not fetch current date from UserDates service. Falling back to system date.");
        return LocalDate.now(); // Fallback, consider implications
    }
}