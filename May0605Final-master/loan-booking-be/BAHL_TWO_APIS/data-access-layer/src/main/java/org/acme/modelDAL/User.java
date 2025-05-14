package org.acme.modelDAL;


import java.time.LocalDateTime;

public class User {
    private String userId;
    private int branchCode;
    private String userIPAddress;
    private String userRole;
    private String module;
    private LocalDateTime lastSignOnDate;
    private String branchName;


    // Constructors
    public User(String userId, int branchCode, String userIPAddress, String userRole, String module , String branchName) {
        this.userId = userId;
        this.branchCode = branchCode;
        this.userIPAddress = userIPAddress;
        this.userRole = userRole;
        this.module = module;
        this.branchName = branchName;
    }

    // Getters & Setters


    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDateTime getLastSignOnDate() { return lastSignOnDate; }
    public void setLastSignOnDate(LocalDateTime lastSignOnDate) { this.lastSignOnDate = lastSignOnDate; }

    public int getBranchCode() { return branchCode; }
    public void setBranchCode(int branchCode) { this.branchCode = branchCode; }

    public String getUserIPAddress() { return userIPAddress; }
    public void setUserIPAddress(String userIPAddress) { this.userIPAddress = userIPAddress; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
}