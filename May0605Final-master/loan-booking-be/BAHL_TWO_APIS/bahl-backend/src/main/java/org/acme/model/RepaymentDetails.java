package org.acme.model;

import java.sql.Date;

public class RepaymentDetails {
    private String loanNumber;
    private int noOfDays;
    private Date maturityDate;

    // Constructors, getters, and setters

    public RepaymentDetails() {
    }

    public RepaymentDetails(String loanNumber, int noOfDays, Date maturityDate) {
        this.loanNumber = loanNumber;
        this.noOfDays = noOfDays;
        this.maturityDate = maturityDate;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public void setLoanNumber(String loanNumber) {
        this.loanNumber = loanNumber;
    }

    public int getNoOfDays() {
        return noOfDays;
    }

    public void setNoOfDays(int noOfDays) {
        this.noOfDays = noOfDays;
    }

    public Date getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(Date maturityDate) {
        this.maturityDate = maturityDate;
    }
}