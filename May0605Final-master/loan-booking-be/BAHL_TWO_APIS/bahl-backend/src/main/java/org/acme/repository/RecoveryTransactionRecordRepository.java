package org.acme.repository;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.model.RecoveryTransaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class RecoveryTransactionRecordRepository {

    @Inject
    AgroalDataSource dataSource;

    @Transactional(rollbackOn = Exception.class)
    public void create(String transactionCode, String voucherNumber, String accountNumber, String accountTitle, String currency, BigDecimal debitAmount, BigDecimal creditAmount, Date transactionDate, String voucherId) throws SQLException {
        // VoucherId ko padding karne ke liye helper function
        String paddedVoucherId = String.format("%04d", Integer.parseInt(voucherId));

        String sql = "INSERT INTO RecoveryTransactionTable (TransactionCode, VoucherNumber, AccountNumber, AccountTitle, Currency, DebitAmount, CreditAmount, TransactionDate, VoucherId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            stmt.setString(9, paddedVoucherId); // Padded VoucherId set karein
            stmt.executeUpdate();
        }
    }

    public String getNextVoucherId() throws SQLException {
        String sql = "SELECT LPAD(NEXT VALUE FOR VoucherIdSequence, 4, '0') FROM SYSIBM.SYSDUMMY1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getString(1);
            } else {
                throw new SQLException("Could not retrieve next VoucherId from sequence.");
            }
        }
    }


    public List<RecoveryTransaction> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<RecoveryTransaction> transactions = new ArrayList<>();
        String sql = "SELECT TransactionCode, VoucherNumber, TransactionNumber, AccountNumber, AccountTitle, Currency, DebitAmount, CreditAmount, TransactionDate, VoucherId " +
                "FROM RecoveryTransactionTable " +
                "WHERE DATE(TransactionDate) >= ? AND DATE(TransactionDate) <= ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RecoveryTransaction transaction = new RecoveryTransaction();
                    transaction.setTransactionCode(rs.getString("TransactionCode"));
                    transaction.setVoucherNumber(rs.getString("VoucherNumber"));
                    transaction.setTransactionNumber(rs.getInt("TransactionNumber"));
                    transaction.setAccountNumber(rs.getString("AccountNumber"));
                    transaction.setAccountTitle(rs.getString("AccountTitle"));
                    transaction.setCurrency(rs.getString("Currency"));
                    transaction.setDebitAmount(rs.getBigDecimal("DebitAmount"));
                    transaction.setCreditAmount(rs.getBigDecimal("CreditAmount"));
                    transaction.setTransactionDate(rs.getTimestamp("TransactionDate").toLocalDateTime());
                    transaction.setVoucherId(rs.getString("VoucherId")); // Retrieve VoucherId
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }

    public List<RecoveryTransaction> findByVoucherIdRange(String fromVoucherId, String toVoucherId) throws SQLException {
        List<RecoveryTransaction> transactions = new ArrayList<>();
        String sql = "SELECT TransactionCode, VoucherNumber, TransactionNumber, AccountNumber, AccountTitle, Currency, DebitAmount, CreditAmount, TransactionDate, VoucherId " +
                "FROM RecoveryTransactionTable " +
                "WHERE VoucherId >= ? AND VoucherId <= ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fromVoucherId);
            stmt.setString(2, toVoucherId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RecoveryTransaction transaction = new RecoveryTransaction();
                    transaction.setTransactionCode(rs.getString("TransactionCode"));
                    transaction.setVoucherNumber(rs.getString("VoucherNumber"));
                    transaction.setTransactionNumber(rs.getInt("TransactionNumber"));
                    transaction.setAccountNumber(rs.getString("AccountNumber"));
                    transaction.setAccountTitle(rs.getString("AccountTitle"));
                    transaction.setCurrency(rs.getString("Currency"));
                    transaction.setDebitAmount(rs.getBigDecimal("DebitAmount"));
                    transaction.setCreditAmount(rs.getBigDecimal("CreditAmount"));
                    transaction.setTransactionDate(rs.getTimestamp("TransactionDate").toLocalDateTime());
                    transaction.setVoucherId(rs.getString("VoucherId")); // Retrieve VoucherId
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }
}