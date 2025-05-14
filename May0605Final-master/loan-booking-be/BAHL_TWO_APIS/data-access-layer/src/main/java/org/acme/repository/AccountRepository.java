package org.acme.repository;

import org.acme.modelDAL.Account;
import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


@ApplicationScoped
public class AccountRepository {

    @Inject
    AgroalDataSource dataSource; // JDBC DataSource

    public Account findByCustomerNumber(String customerNumber) {
        String sql = """
            SELECT a.CustomerNumber, a.BranchCode, b.BranchName,
                   a.AccountTitle, a.CurrencyCode, a.Balance
            FROM AccountTable a
            JOIN branches b ON a.BranchCode = b.BranchCode
            WHERE a.CustomerNumber = ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Account account = new Account();
                account.setCustomerNumber(rs.getString("CustomerNumber"));
                account.setBranchCode(rs.getInt("BranchCode"));
                account.setBranchName(rs.getString("BranchName"));
                account.setAccountTitle(rs.getString("AccountTitle"));
                account.setCurrencyCode(rs.getInt("CurrencyCode"));
                account.setBalance(rs.getDouble("Balance"));
                return account;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // No account found
    }

    public boolean updateBalance(String customerNumber, double newBalance) throws SQLException {
        String sql = "UPDATE AccountTable SET Balance = ? WHERE CustomerNumber = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newBalance);
            stmt.setString(2, customerNumber);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String findAccountTitleByCustomerNumber(String customerNumber) {
        String sql = "SELECT AccountTitle FROM AccountTable WHERE CustomerNumber = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("AccountTitle");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}