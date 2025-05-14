package org.acme.repository;

import org.acme.model.LoanLimit;
import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.*;
import java.time.LocalDateTime;
@ApplicationScoped
public class LoanLimitRepository {

    @Inject
    AgroalDataSource dataSource;

    public boolean createLoanLimit(LoanLimit loanLimit) {
        String sql = """
            INSERT INTO LoanLimits (CustomerNumber, LoanLimit, LimitBalance, CreatedBy, CreatedOn, UpdatedBy, UpdatedOn, Status)
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
            stmt.setInt(8, 9); // Status = 9 for Authorized
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean updateLoanLimit(LoanLimit loanLimit) {
        String sql = """
            UPDATE LoanLimits
            SET LoanLimit = ?, LimitBalance = ?, UpdatedBy = ?, UpdatedOn = ? , Status = ?
            WHERE CustomerNumber = ?
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, loanLimit.getLoanLimit());
            stmt.setDouble(2, loanLimit.getLimitBalance());
            stmt.setString(3, loanLimit.getUpdatedBy());
            stmt.setTimestamp(4, Timestamp.valueOf(loanLimit.getUpdatedOn()));
            stmt.setInt(5, loanLimit.getStatus());
            stmt.setString(6, loanLimit.getCustomerNumber());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public LoanLimit getLoanLimitByCustomerNumber(String customerNumber) {
        String sql = "SELECT * FROM LoanLimits WHERE CustomerNumber = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);) { // Declare ResultSet
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

    public boolean updateLoanLimitOnBalanceChange(LoanLimit loanLimit) {
        String sql = """
        UPDATE LoanLimits
        SET LimitBalance = ?, UpdatedBy = ?, UpdatedOn = ?, Status = ?
        WHERE CustomerNumber = ?
    """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, loanLimit.getLimitBalance());
            stmt.setString(2, loanLimit.getUpdatedBy());
            stmt.setTimestamp(3, Timestamp.valueOf(loanLimit.getUpdatedOn()));
            stmt.setInt(4, loanLimit.getStatus());
            stmt.setString(5, loanLimit.getCustomerNumber());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}