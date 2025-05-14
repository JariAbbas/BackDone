package org.acme.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecoveryTransaction {
    private String transactionCode;
    private String voucherNumber;
    private Integer transactionNumber;
    private String accountNumber;
    private String accountTitle;
    private String currency;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private LocalDateTime transactionDate;
    private String voucherId; // Add this field

    // Default constructor
    public RecoveryTransaction() {
    }

    // Constructor with all fields (including voucherId)
    public RecoveryTransaction(String transactionCode, String voucherNumber, Integer transactionNumber,
                               String accountNumber, String accountTitle, String currency, BigDecimal debitAmount,
                               BigDecimal creditAmount, LocalDateTime transactionDate, String voucherId) {
        this.transactionCode = transactionCode;
        this.voucherNumber = voucherNumber;
        this.transactionNumber = transactionNumber;
        this.accountNumber = accountNumber;
        this.accountTitle = accountTitle;
        this.currency = currency;
        this.debitAmount = debitAmount;
        this.creditAmount = creditAmount;
        this.transactionDate = transactionDate;
        this.voucherId = voucherId; // Initialize the new field
    }

    // Getters and setters for all fields

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }

    public Integer getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(Integer transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountTitle() {
        return accountTitle;
    }

    public void setAccountTitle(String accountTitle) {
        this.accountTitle = accountTitle;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(BigDecimal debitAmount) {
        this.debitAmount = debitAmount;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    // Getter and Setter for voucherId
    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }
}