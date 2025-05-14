package org.acme.model;

import java.math.BigDecimal;

public class Markup {
    private Long id;
    private String loanNumber;
    private Long baseRateId;
    private BigDecimal spreadRate;
    private BigDecimal applicableRate;
    private BigDecimal odRate;

    public Markup() {
    }

    public Markup(Long id, String loanNumber, Long baseRateId, BigDecimal spreadRate, BigDecimal applicableRate, BigDecimal odRate) {
        this.id = id;
        this.loanNumber = loanNumber;
        this.baseRateId = baseRateId;
        this.spreadRate = spreadRate;
        this.applicableRate = applicableRate;
        this.odRate = odRate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public void setLoanNumber(String loanNumber) {
        this.loanNumber = loanNumber;
    }

    public Long getBaseRateId() {
        return baseRateId;
    }

    public void setBaseRateId(Long baseRateId) {
        this.baseRateId = baseRateId;
    }

    public BigDecimal getSpreadRate() {
        return spreadRate;
    }

    public void setSpreadRate(BigDecimal spreadRate) {
        this.spreadRate = spreadRate;
    }

    public BigDecimal getApplicableRate() {
        return applicableRate;
    }

    public void setApplicableRate(BigDecimal applicableRate) {
        this.applicableRate = applicableRate;
    }

    public BigDecimal getOdRate() {
        return odRate;
    }

    public void setOdRate(BigDecimal odRate) {
        this.odRate = odRate;
    }

    // Added constructor without applicableRate for creation
    public Markup(String loanNumber, Long baseRateId, BigDecimal spreadRate, BigDecimal odRate) {
        this.loanNumber = loanNumber;
        this.baseRateId = baseRateId;
        this.spreadRate = spreadRate;
        this.odRate = odRate;
    }
}