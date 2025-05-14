// org.acme.model.GLAccountCustomerNumbers.java
package org.acme.model;

public class GLAccountCustomerNumbers {
    private String customerNumber1;
    private String customerNumber2;

    // Constructors
    public GLAccountCustomerNumbers() {
    }

    public GLAccountCustomerNumbers(String customerNumber1, String customerNumber2) {
        this.customerNumber1 = customerNumber1;
        this.customerNumber2 = customerNumber2;
    }

    // Getters and Setters
    public String getCustomerNumber1() {
        return customerNumber1;
    }

    public void setCustomerNumber1(String customerNumber1) {
        this.customerNumber1 = customerNumber1;
    }

    public String getCustomerNumber2() {
        return customerNumber2;
    }

    public void setCustomerNumber2(String customerNumber2) {
        this.customerNumber2 = customerNumber2;
    }
}