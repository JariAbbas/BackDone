package org.acme.repository;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.acme.model.LoanLimit;

@ApplicationScoped
public class UnauthorizedLoanLimitRepository {

    @Inject
    AgroalDataSource dataSource;

    public boolean createUnauthorizedLoanLimit(LoanLimit loanLimit) {
        String sql = """
            INSERT INTO UnauthorizedLoanLimits (CustomerNumber, LoanLimit, LimitBalance, CreatedBy, CreatedOn, UpdatedBy, UpdatedOn, Status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, loanLimit.getCustomerNumber());
            stmt.setDouble(2, loanLimit.getLoanLimit());
            stmt.setDouble(3, loanLimit.getLimitBalance());
            stmt.setString(4, loanLimit.getCreatedBy());
            stmt.setTimestamp(5, Timestamp.valueOf(loanLimit.getCreatedOn()));
            stmt.setString(6, loanLimit.getUpdatedBy());
            stmt.setTimestamp(7, Timestamp.valueOf(loanLimit.getUpdatedOn()));
            stmt.setInt(8, 1); // Status = 1 for Unauthorized
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public LoanLimit getUnauthorizedLoanLimitByCustomerNumber(String customerNumber) {
        String sql = "SELECT * FROM UnauthorizedLoanLimits WHERE CustomerNumber = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);) {  // Declare ResultSet here
            stmt.setString(1, customerNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                LoanLimit loanLimit = new LoanLimit();
                loanLimit.setCustomerNumber(rs.getString("CustomerNumber"));
                loanLimit.setLoanLimit(rs.getDouble("LoanLimit"));
                loanLimit.setLimitBalance(rs.getDouble("LimitBalance"));
                loanLimit.setCreatedBy(rs.getString("CreatedBy"));
                loanLimit.setCreatedOn(rs.getTimestamp("CreatedOn").toLocalDateTime());
                loanLimit.setUpdatedBy(rs.getString("UpdatedBy"));
                loanLimit.setUpdatedOn(rs.getTimestamp("UpdatedOn").toLocalDateTime());
                loanLimit.setStatus(rs.getInt("Status"));
                return loanLimit;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public boolean deleteUnauthorizedLoanLimit(String customerNumber) {
        String sql = "DELETE FROM UnauthorizedLoanLimits WHERE CustomerNumber = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerNumber);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUnauthorizedLoanLimitStatus(String customerNumber, int status) {
        String sql = "UPDATE UnauthorizedLoanLimits SET Status = ? WHERE CustomerNumber = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, status);
            stmt.setString(2, customerNumber);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<LoanLimit> getUnauthorizedLoanLimits() {
        String sql = "SELECT * FROM UnauthorizedLoanLimits";
        List<LoanLimit> loanLimits = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                LoanLimit loanLimit = new LoanLimit();
                loanLimit.setCustomerNumber(rs.getString("CustomerNumber"));
                loanLimit.setLoanLimit(rs.getDouble("LoanLimit"));
                loanLimit.setLimitBalance(rs.getDouble("LimitBalance"));
                loanLimit.setCreatedBy(rs.getString("CreatedBy"));
                loanLimit.setCreatedOn(rs.getTimestamp("CreatedOn").toLocalDateTime());
                loanLimit.setUpdatedBy(rs.getString("UpdatedBy"));
                loanLimit.setUpdatedOn(rs.getTimestamp("UpdatedOn").toLocalDateTime());
                loanLimit.setStatus(rs.getInt("Status"));
                loanLimits.add(loanLimit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loanLimits;
    }
}