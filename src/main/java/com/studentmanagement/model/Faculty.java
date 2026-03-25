package com.studentmanagement.model;

/**
 * Faculty Model Class
 * Represents a faculty member in the system
 */
public class Faculty {
    private String facultyId;
    private String name;
    private String department;
    private String email;
    private String phone;
    private String password;
    private String status;
    
    // Constructors
    public Faculty() {
    }
    
    public Faculty(String facultyId, String name, String department, String email, 
                   String phone, String password, String status) {
        this.facultyId = facultyId;
        this.name = name;
        this.department = department;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.status = status;
    }
    
    // Getters and Setters
    public String getFacultyId() {
        return facultyId;
    }
    
    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "Faculty{" +
                "facultyId='" + facultyId + '\'' +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}