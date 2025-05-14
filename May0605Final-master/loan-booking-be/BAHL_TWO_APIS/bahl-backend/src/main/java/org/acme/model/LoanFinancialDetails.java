package org.acme.model;

public class LoanFinancialDetails {
    private int financialDetailID;
    private String loanNumber;
    private double profitAmount;
    private double receivableAmountAtMaturity;

    public LoanFinancialDetails() {
    }

    public LoanFinancialDetails(String loanNumber) {
        this.loanNumber = loanNumber;
    }

    public int getFinancialDetailID() {
        return financialDetailID;
    }

    public void setFinancialDetailID(int financialDetailID) {
        this.financialDetailID = financialDetailID;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public void setLoanNumber(String loanNumber) {
        this.loanNumber = loanNumber;
    }

    public double getProfitAmount() {
        return profitAmount;
    }

    public void setProfitAmount(double profitAmount) {
        this.profitAmount = profitAmount;
    }

    public double getReceivableAmountAtMaturity() {
        return receivableAmountAtMaturity;
    }

    public void setReceivableAmountAtMaturity(double receivableAmountAtMaturity) {
        this.receivableAmountAtMaturity = receivableAmountAtMaturity;
    }
}