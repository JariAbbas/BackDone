package org.acme.model;

import java.math.BigDecimal;

public class BaseRate {
    private Long id;
    private String baseName;
    private BigDecimal baseRate;

    public BaseRate() {
    }

    public BaseRate(Long id, String baseName, BigDecimal baseRate) {
        this.id = id;
        this.baseName = baseName;
        this.baseRate = baseRate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public BigDecimal getBaseRate() {
        return baseRate;
    }

    public void setBaseRate(BigDecimal baseRate) {
        this.baseRate = baseRate;
    }
}