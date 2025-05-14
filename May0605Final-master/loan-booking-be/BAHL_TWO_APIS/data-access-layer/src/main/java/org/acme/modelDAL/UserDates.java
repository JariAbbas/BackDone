package org.acme.modelDAL;

import java.time.LocalDateTime;

public class UserDates {
    private String userId;
    private int branchCode;
    private String branchName;
    private LocalDateTime currentDate;
    private LocalDateTime previousDate; // Added previousDate

    // Constructor
    public UserDates(String userId, int branchCode, String branchName, LocalDateTime currentDate, LocalDateTime previousDate) {
        this.userId = userId;
        this.branchCode = branchCode;
        this.branchName = branchName != null ? branchName : "Unknown";
        this.currentDate = currentDate;
        this.previousDate = previousDate;
    }

    public UserDates() {

    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getBranchCode() { return branchCode; }
    public void setBranchCode(int branchCode) { this.branchCode = branchCode; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public LocalDateTime getCurrentDate() { return currentDate; }
    public void setCurrentDate(LocalDateTime currentDate) { this.currentDate = currentDate; }

    public LocalDateTime getPreviousDate() { return previousDate; }
    public void setPreviousDate(LocalDateTime previousDate) { this.previousDate = previousDate; }

    public void setId(int id) {
    }
}