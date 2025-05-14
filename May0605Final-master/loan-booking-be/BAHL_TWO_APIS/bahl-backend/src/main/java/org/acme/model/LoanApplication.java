package org.acme.model;

import java.sql.Date;

public class LoanApplication {

    private Integer loanId;
    private String loanNumber;
    private String customerNumber;
    private Date grantDate;
    private String documentRefNo;
    private Double dealAmount;
    private String remarks;
    private Double accrual;
    private Integer status;
    private String authorizedBy;
    private Integer noOfDays; // Add this
    private Double applicableRate; // Add this
    private Double odRate; // Add this

    // Default constructor
    public LoanApplication() {
    }

    // Constructor with all fields (adjust as needed)
    public LoanApplication(String loanNumber, String customerNumber, Date grantDate, String documentRefNo, Double dealAmount, String remarks, Double accrual, Integer status, String authorizedBy, Integer noOfDays, Double applicableRate, Double odRate) {
        this.loanNumber = loanNumber;
        this.customerNumber = customerNumber;
        this.grantDate = grantDate;
        this.documentRefNo = documentRefNo;
        this.dealAmount = dealAmount;
        this.remarks = remarks;
        this.accrual = accrual;
        this.status = status;
        this.authorizedBy = authorizedBy;
        this.noOfDays = noOfDays;
        this.applicableRate = applicableRate;
        this.odRate = odRate;
    }

    // Getters and setters for all fields...

    public Integer getLoanId() {
        return loanId;
    }

    public void setLoanId(Integer loanId) {
        this.loanId = loanId;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public void setLoanNumber(String loanNumber) {
        this.loanNumber = loanNumber;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public Date getGrantDate() {
        return grantDate;
    }

    public void setGrantDate(Date grantDate) {
        this.grantDate = grantDate;
    }

    public String getDocumentRefNo() {
        return documentRefNo;
    }

    public void setDocumentRefNo(String documentRefNo) {
        this.documentRefNo = documentRefNo;
    }

    public Double getDealAmount() {
        return dealAmount;
    }

    public void setDealAmount(Double dealAmount) {
        this.dealAmount = dealAmount;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Double getAccrual() {
        return accrual;
    }

    public void setAccrual(Double accrual) {
        this.accrual = accrual;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAuthorizedBy() {
        return authorizedBy;
    }

    public void setAuthorizedBy(String authorizedBy) {
        this.authorizedBy = authorizedBy;
    }

    public Integer getNoOfDays() {
        return noOfDays;
    }

    public void setNoOfDays(Integer noOfDays) {
        this.noOfDays = noOfDays;
    }

    public Double getApplicableRate() {
        return applicableRate;
    }

    public void setApplicableRate(Double applicableRate) {
        this.applicableRate = applicableRate;
    }

    public Double getOdRate() {
        return odRate;
    }

    public void setOdRate(Double odRate) {
        this.odRate = odRate;
    }
}