package org.acme.model;

import java.math.BigDecimal; // Use BigDecimal for monetary values
import java.sql.Date;
import java.time.LocalDate;

public class LoanRecoveryDetails {

    private String customerNumber;
    private String accountTitle;
    private Integer currencyCode;
    private String currencyName;
    private BigDecimal dealAmount;
    private BigDecimal profitAmount;          // From LoanFinancialDetails
    private BigDecimal calculatedAccrual;     // Calculated at recovery time
    private Integer currentDateNoOfDays;    // Days since grant date
    private Date recoveryDate;            // Current date during recovery view/action
    private BigDecimal recoveryAmount;        // Higher of profitAmount or calculatedAccrual

    // Default constructor
    public LoanRecoveryDetails() {
    }

    // Constructor might be useful
    public LoanRecoveryDetails(String customerNumber, String accountTitle, Integer currencyCode, String currencyName, BigDecimal dealAmount, BigDecimal profitAmount, BigDecimal calculatedAccrual, Integer currentDateNoOfDays, Date recoveryDate, BigDecimal recoveryAmount) {
        this.customerNumber = customerNumber;
        this.accountTitle = accountTitle;
        this.currencyCode = currencyCode;
        this.currencyName = currencyName;
        this.dealAmount = dealAmount;
        this.profitAmount = profitAmount;
        this.calculatedAccrual = calculatedAccrual;
        this.currentDateNoOfDays = currentDateNoOfDays;
        this.recoveryDate = recoveryDate;
        this.recoveryAmount = recoveryAmount;
    }


    // --- Getters and Setters ---

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getAccountTitle() {
        return accountTitle;
    }

    public void setAccountTitle(String accountTitle) {
        this.accountTitle = accountTitle;
    }

    public Integer getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(Integer currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public BigDecimal getDealAmount() {
        return dealAmount;
    }

    public void setDealAmount(BigDecimal dealAmount) {
        this.dealAmount = dealAmount;
    }

    public BigDecimal getProfitAmount() {
        return profitAmount;
    }

    public void setProfitAmount(BigDecimal profitAmount) {
        this.profitAmount = profitAmount;
    }

    public BigDecimal getCalculatedAccrual() {
        return calculatedAccrual;
    }

    public void setCalculatedAccrual(BigDecimal calculatedAccrual) {
        this.calculatedAccrual = calculatedAccrual;
    }

    public Integer getCurrentDateNoOfDays() {
        return currentDateNoOfDays;
    }

    public void setCurrentDateNoOfDays(Integer currentDateNoOfDays) {
        this.currentDateNoOfDays = currentDateNoOfDays;
    }

    public Date getRecoveryDate() {
        return recoveryDate;
    }

    public void setRecoveryDate(Date recoveryDate) {
        this.recoveryDate = recoveryDate;
    }

    public BigDecimal getRecoveryAmount() {
        return recoveryAmount;
    }

    public void setRecoveryAmount(BigDecimal recoveryAmount) {
        this.recoveryAmount = recoveryAmount;
    }
}