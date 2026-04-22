package com.studentmanagement.model;

/**
 * Backlog Entity Class
 * Member 5: M.NARAYANA SWAMY - Backlog Management
 * Represents backlog records for students
 */
public class Backlog {
    
    // Fields
    private int backlogId;          // Unique identifier
    private String rollNumber;      // Student identifier
    private String subjectCode;     // Subject identifier
    private String subjectName;     // Subject name
    private int semester;           // Semester when backlog occurred
    private String reason;          // Marks / Attendance
    private String clearanceStatus; // Pending / Cleared
    private String clearanceDate;   // Date when cleared (if applicable)
    
    // Constructors
    public Backlog() {
    }
    
    public Backlog(int backlogId, String rollNumber, String subjectCode, String subjectName, 
                   int semester, String reason) {
        this.backlogId = backlogId;
        this.rollNumber = rollNumber;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.semester = semester;
        this.reason = reason;
        this.clearanceStatus = "Pending";
        this.clearanceDate = null;
    }
    
    // Business Logic Methods
    public void markAsCleared(String clearanceDate) {
        this.clearanceStatus = "Cleared";
        this.clearanceDate = clearanceDate;
    }
    
    public boolean isPending() {
        return "Pending".equals(clearanceStatus);
    }
    
    public boolean isCleared() {
        return "Cleared".equals(clearanceStatus);
    }
    
    // Getters and Setters
    public int getBacklogId() {
        return backlogId;
    }
    
    public void setBacklogId(int backlogId) {
        this.backlogId = backlogId;
    }
    
    public String getRollNumber() {
        return rollNumber;
    }
    
    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }
    
    public String getSubjectCode() {
        return subjectCode;
    }
    
    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }
    
    public String getSubjectName() {
        return subjectName;
    }
    
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
    
    public int getSemester() {
        return semester;
    }
    
    public void setSemester(int semester) {
        this.semester = semester;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getClearanceStatus() {
        return clearanceStatus;
    }
    
    public void setClearanceStatus(String clearanceStatus) {
        this.clearanceStatus = clearanceStatus;
    }
    
    public String getClearanceDate() {
        return clearanceDate;
    }
    
    public void setClearanceDate(String clearanceDate) {
        this.clearanceDate = clearanceDate;
    }
    
    @Override
    public String toString() {
        return "Backlog{" +
                "backlogId=" + backlogId +
                ", rollNumber='" + rollNumber + '\'' +
                ", subjectCode='" + subjectCode + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ", semester=" + semester +
                ", reason='" + reason + '\'' +
                ", clearanceStatus='" + clearanceStatus + '\'' +
                ", clearanceDate='" + clearanceDate + '\'' +
                '}';
    }
}
