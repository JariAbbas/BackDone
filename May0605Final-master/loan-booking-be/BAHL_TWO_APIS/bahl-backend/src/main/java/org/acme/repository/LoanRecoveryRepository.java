package org.acme.repository;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.client.UserDatesClient; // Import UserDatesClient
import org.acme.exception.UnauthorizedLoanException;
import org.acme.model.LoanRecoveryDetails; // Use the details model for fetching
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit; // For date difference calculation
import java.time.LocalDateTime;

@ApplicationScoped
public class LoanRecoveryRepository {

    @Inject
    AgroalDataSource dataSource;

    @Inject
    @RestClient
    UserDatesClient userDatesClient; // Inject UserDatesClient

    // Helper method to get the loan application status
    private Integer getLoanApplicationStatus(String loanNumber) throws SQLException, UnauthorizedLoanException {
        String sql = "SELECT Status FROM LoanApplication WHERE LoanNumber = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, loanNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int status = rs.getInt("Status");
                    if (status == 0) {
                        throw new UnauthorizedLoanException("You cannot recover this loan because the loan has been canceled and is present in the authorization queue.");
                    } else if (status == 1) {
                        throw new UnauthorizedLoanException("Loan Number: " + loanNumber + " is unauthorized for recovery.");
                    }
                    return status;
                }
            }
        }
        return null; // Loan not found
    }

    // Method to fetch the loan details, including ODRate if available
    public LoanRecoveryDetails findRecoveryDetailsByLoanNumber(String loanNumber) throws SQLException, UnauthorizedLoanException {
        Integer loanStatus = getLoanApplicationStatus(loanNumber);
        if (loanStatus != null && loanStatus == 1) {
            throw new UnauthorizedLoanException("Loan Number: " + loanNumber + " is unauthorized for recovery.");
        }
        if (loanStatus == null) {
            return null; // Loan not found
        }

        // Use BigDecimal for precision
        final String query = """
        SELECT
            la.CustomerNumber,
            la.GrantDate,
            la.DealAmount,
            la.ApplicableRate,
            la.ODRate,  -- Assuming ODRate is stored in LoanApplication table
            acc.AccountTitle,
            curr.CurrencyCode,
            curr.CurrencyName,
            lfd.ProfitAmount,
            rd.MaturityDate  -- Fetch MaturityDate from REPAYMENTDETAILS
        FROM
            LoanApplication la
        JOIN
            AccountTable acc ON la.CustomerNumber = acc.CustomerNumber
        JOIN
            CurrencyTable curr ON acc.CurrencyCode = curr.CurrencyCode
        LEFT JOIN
            LoanFinancialDetails lfd ON la.LoanNumber = lfd.LoanNumber
        LEFT JOIN
            REPAYMENTDETAILS rd ON la.LoanNumber = rd.LoanNumber  -- Join REPAYMENTDETAILS to get MaturityDate
        WHERE
            la.LoanNumber = ? AND la.Status IN (9, 10, 7 /* Add 7 for Pending Authorization */)
        """; // Include status 7

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, loanNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    LoanRecoveryDetails details = new LoanRecoveryDetails();
                    details.setCustomerNumber(rs.getString("CustomerNumber"));
                    details.setAccountTitle(rs.getString("AccountTitle"));
                    details.setCurrencyCode(rs.getInt("CurrencyCode"));
                    details.setCurrencyName(rs.getString("CurrencyName"));
                    details.setDealAmount(rs.getBigDecimal("DealAmount"));

                    // Profit Amount from LoanApplication (your 'Accrual' in LoanApplication)
                    BigDecimal profitAmountFromLoanApp = rs.getBigDecimal("ProfitAmount");
                    details.setProfitAmount(profitAmountFromLoanApp != null ? profitAmountFromLoanApp : BigDecimal.ZERO);

                    double applicableRate = rs.getDouble("ApplicableRate");
                    double odRate = rs.getDouble("ODRate");

                    Date grantDateSql = rs.getDate("GrantDate");
                    BigDecimal dealAmount = details.getDealAmount();
                    Date maturityDateSql = rs.getDate("MaturityDate");

                    // Fetch the latest currentDate from UserDates using the client
                    LocalDateTime currentDateTime = userDatesClient.getLatestCurrentDate();
                    LocalDate currentDate;
                    if (currentDateTime != null) {
                        currentDate = currentDateTime.toLocalDate();
                    } else {
                        currentDate = LocalDate.now(); // Fallback to system date if UserDates is empty/error
                    }

                    if (grantDateSql != null && dealAmount != null && maturityDateSql != null) {
                        LocalDate grantDate = grantDateSql.toLocalDate();
                        LocalDate maturityDate = maturityDateSql.toLocalDate();

                        // Check if GrantDate and RecoveryDate are the same
                        if (grantDate.isEqual(currentDate)) {
                            throw new UnauthorizedLoanException("It's not possible to recover a loan on the same day it was granted.");
                        }

                        long daysDiffFromGrantToCurrent = ChronoUnit.DAYS.between(grantDate, currentDate);
                        int currentDateNoOfDays = (int) Math.max(0, daysDiffFromGrantToCurrent);
                        details.setCurrentDateNoOfDays(currentDateNoOfDays);

                        BigDecimal calculatedAccrual = BigDecimal.ZERO;
                        BigDecimal dailyApplicableRate = BigDecimal.valueOf(applicableRate).divide(BigDecimal.valueOf(36500), 10, RoundingMode.HALF_UP);
                        BigDecimal dailyODRate = BigDecimal.valueOf(odRate).divide(BigDecimal.valueOf(36500), 10, RoundingMode.HALF_UP);

                        if (!currentDate.isAfter(maturityDate)) {
                            // Recovery on or before Maturity Date
                            calculatedAccrual = dealAmount.multiply(dailyApplicableRate).multiply(BigDecimal.valueOf(currentDateNoOfDays));
                        } else {
                            // Recovery after Maturity Date
                            long daysAfterMaturity = ChronoUnit.DAYS.between(maturityDate, currentDate);
                            BigDecimal accrualBeforeMaturity = dealAmount.multiply(dailyApplicableRate).multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(grantDate, maturityDate)));
                            BigDecimal accrualAfterMaturity = dealAmount.multiply(dailyODRate).multiply(BigDecimal.valueOf(daysAfterMaturity));
                            calculatedAccrual = accrualBeforeMaturity.add(accrualAfterMaturity);
                        }

                        details.setCalculatedAccrual(calculatedAccrual.setScale(2, RoundingMode.HALF_UP));

                        // Calculate Recovery Amount
                        BigDecimal recoveryAmount = details.getDealAmount().add(details.getCalculatedAccrual());
                        details.setRecoveryAmount(recoveryAmount.setScale(2, RoundingMode.HALF_UP));

                    } else {
                        // Handle cases where calculation isn't possible
                        details.setCurrentDateNoOfDays(0);
                        details.setCalculatedAccrual(BigDecimal.ZERO);
                        details.setRecoveryAmount(details.getDealAmount().setScale(2, RoundingMode.HALF_UP)); // Only principal if dates missing
                    }

                    details.setRecoveryDate(Date.valueOf(currentDate)); // Set recovery date from UserDates

                    return details;
                }
            }
        }
        return null; // Loan not found or missing related data
    }



    public void saveRecoveryRecord(String loanNumber, LoanRecoveryDetails details, Date originalGrantDate, String recoveredByUserId) throws SQLException, UnauthorizedLoanException {
        Integer loanStatus = getLoanApplicationStatus(loanNumber);
        if (loanStatus != null && loanStatus == 1) {
            throw new UnauthorizedLoanException("Loan Number: " + loanNumber + " is unauthorized for recovery.");
        }
        if (loanStatus == null || loanStatus != 9) {
            throw new UnauthorizedLoanException("Loan Number: " + loanNumber + " is not eligible for recovery.");
        }

        // Fetch the latest currentDate from UserDates for RecoveryDate using the client
        LocalDateTime currentDateTime = userDatesClient.getLatestCurrentDate();
        Date recoveryDate;
        if (currentDateTime != null) {
            recoveryDate = Date.valueOf(currentDateTime.toLocalDate());
        } else {
            recoveryDate = Date.valueOf(LocalDate.now()); // Fallback to server date if not found
        }

        final String insertQuery = """
            INSERT INTO LoanRecovery
            (LoanNumber, CustomerNumber, AccountTitle, CurrencyCode, CurrencyName,
             DealAmount, ProfitAmount, CalculatedAccrual, RecoveryAmount, RecoveryDate,
             OriginalGrantDate, DaysSinceGrant, RecoveredBy, Status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

            stmt.setString(1, loanNumber);
            stmt.setString(2, details.getCustomerNumber());
            stmt.setString(3, details.getAccountTitle());
            stmt.setInt(4, details.getCurrencyCode());
            stmt.setString(5, details.getCurrencyName());
            stmt.setBigDecimal(6, details.getDealAmount());
            stmt.setBigDecimal(7, details.getProfitAmount());
            stmt.setBigDecimal(8, details.getCalculatedAccrual());
            stmt.setBigDecimal(9, details.getRecoveryAmount());
            stmt.setDate(10, recoveryDate); // Use fetched currentDate for RecoveryDate
            stmt.setDate(11, originalGrantDate);
            stmt.setInt(12, details.getCurrentDateNoOfDays());
            stmt.setString(13, recoveredByUserId);
            stmt.setInt(14, 1);

            stmt.executeUpdate();
        }
    }

    // Helper method to get the original grant date (needed separately for insertion)
    public Date getLoanGrantDate(String loanNumber) throws SQLException, UnauthorizedLoanException {
        Integer loanStatus = getLoanApplicationStatus(loanNumber);
        if (loanStatus != null && loanStatus == 1) {
            throw new UnauthorizedLoanException("Loan Number: " + loanNumber + " is unauthorized for recovery.");
        }
        if (loanStatus == null || loanStatus != 9) {
            return null; // Or throw exception if loan must exist and be eligible
        }
        String sql = "SELECT GrantDate FROM LoanApplication WHERE LoanNumber = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, loanNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDate("GrantDate");
                }
            }
        }
        return null; // Or throw exception if loan must exist
    }

    // Optional: Check if recovery already exists for a loan
//    public boolean doesRecoveryExist(String loanNumber) throws SQLException, UnauthorizedLoanException {
//        Integer loanStatus = getLoanApplicationStatus(loanNumber);
//        if (loanStatus != null && loanStatus == 1) {
//            throw new UnauthorizedLoanException("Loan Number: " + loanNumber + " is unauthorized for recovery.");
//        }
//        if (loanStatus == null || loanStatus != 9) {
//            return false; // Loan not found or not eligible
//        }
//        String sql = "SELECT 1 FROM LoanRecovery WHERE LoanNumber = ?";
//        try (Connection conn = dataSource.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setString(1, loanNumber);
//            try (ResultSet rs = stmt.executeQuery()) {
//                return rs.next(); // Returns true if a record exists
//            }
//        }
//    }

    public boolean doesRecoveryExist(String loanNumber) throws SQLException {
        String sql = "SELECT 1 FROM LoanRecovery WHERE LoanNumber = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, loanNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Returns true if a record exists
            }
        }
    }

    public void updateRecoveryStatus(String loanNumber, int newStatus) throws SQLException {
        String sql = "UPDATE LoanRecovery SET Status = ? WHERE LoanNumber = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newStatus);
            stmt.setString(2, loanNumber);
            stmt.executeUpdate(); // Execute the update statement
        }
    }


    public void updateAuthorizedBy(String loanNumber, String authorizedByUserId) throws SQLException, UnauthorizedLoanException {
        // No need to check LoanApplication status here, as this action should only be callable
        // when the recovery is in the pending authorization state.

        String sql = "UPDATE LoanRecovery SET AuthorizedBy = ? WHERE LoanNumber = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authorizedByUserId);
            stmt.setString(2, loanNumber);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update authorized by for Loan Number: " + loanNumber + ". Recovery record not found.");
            }
        }
    }
}