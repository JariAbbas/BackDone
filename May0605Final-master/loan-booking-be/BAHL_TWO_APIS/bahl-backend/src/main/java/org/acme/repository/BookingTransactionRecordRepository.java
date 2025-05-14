package org.acme.repository;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.model.BookingTransaction;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class BookingTransactionRecordRepository {

    @Inject
    AgroalDataSource dataSource;

    @Transactional(rollbackOn = Exception.class)
    public void create(String transactionCode, String voucherNumber, String accountNumber, String accountTitle, String currency, BigDecimal debitAmount, BigDecimal creditAmount, Date transactionDate, Integer loanNumber) throws SQLException {
        String sql = "INSERT INTO BookingTransactionTable (TransactionCode, VoucherNumber, AccountNumber, AccountTitle, Currency, DebitAmount, CreditAmount, TransactionDate, LoanNumber) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, transactionCode);
            stmt.setString(2, voucherNumber);
            stmt.setString(3, accountNumber);
            stmt.setString(4, accountTitle);
            stmt.setString(5, currency);
            stmt.setBigDecimal(6, debitAmount);
            stmt.setBigDecimal(7, creditAmount);
            stmt.setDate(8, transactionDate);
            stmt.setInt(9, loanNumber); // Set the LoanNumber
            stmt.executeUpdate();
        }
    }

    public List<BookingTransaction> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<BookingTransaction> transactions = new ArrayList<>();
        String sql = "SELECT TransactionCode, VoucherNumber, TransactionNumber, AccountNumber, AccountTitle, Currency, DebitAmount, CreditAmount, TransactionDate, LoanNumber " +
                "FROM BookingTransactionTable " +
                "WHERE DATE(TransactionDate) >= ? AND DATE(TransactionDate) <= ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BookingTransaction transaction = new BookingTransaction();
                    transaction.setTransactionCode(rs.getString("TransactionCode"));
                    transaction.setVoucherNumber(rs.getString("VoucherNumber"));
                    transaction.setTransactionNumber(rs.getInt("TransactionNumber"));
                    transaction.setAccountNumber(rs.getString("AccountNumber"));
                    transaction.setAccountTitle(rs.getString("AccountTitle"));
                    transaction.setCurrency(rs.getString("Currency"));
                    transaction.setDebitAmount(rs.getBigDecimal("DebitAmount"));
                    transaction.setCreditAmount(rs.getBigDecimal("CreditAmount"));
                    transaction.setTransactionDate(rs.getTimestamp("TransactionDate").toLocalDateTime());
                    transaction.setLoanNumber(rs.getInt("LoanNumber")); // Retrieve LoanNumber
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }

    public List<BookingTransaction> findByVoucherNumber(String voucherNumber) throws SQLException {
        String sql = "SELECT * FROM BookingTransactionTable WHERE voucherNumber = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, voucherNumber);
            ResultSet rs = stmt.executeQuery();
            List<BookingTransaction> transactions = new ArrayList<>();
            while (rs.next()) {
                BookingTransaction transaction = new BookingTransaction();
                transaction.setTransactionCode(rs.getString("TransactionCode"));
                transaction.setVoucherNumber(rs.getString("VoucherNumber"));
                transaction.setTransactionNumber(rs.getInt("TransactionNumber"));
                transaction.setAccountNumber(rs.getString("AccountNumber"));
                transaction.setAccountTitle(rs.getString("AccountTitle"));
                transaction.setCurrency(rs.getString("Currency"));
                transaction.setDebitAmount(rs.getBigDecimal("DebitAmount"));
                transaction.setCreditAmount(rs.getBigDecimal("CreditAmount"));
                transaction.setTransactionDate(rs.getTimestamp("TransactionDate").toLocalDateTime());
                transaction.setLoanNumber(rs.getInt("LoanNumber")); // Retrieve LoanNumber
                transactions.add(transaction);
            }
            return transactions;
        }
    }

    // Remove findByLoanNumberRange and findByLoanNumber methods
}