 package com.studentmanagement.dao;

import com.studentmanagement.database.DatabaseConnection;
import com.studentmanagement.model.Faculty;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Faculty Data Access Object
 * Handles all database operations for Faculty
 */
public class FacultyDAO {
    
    /**
     * Authenticate faculty member
     * @param facultyId Faculty ID
     * @param password Password
     * @return Faculty object if valid, null otherwise
     */
    public Faculty authenticateFaculty(String facultyId, String password) {
        String sql = "SELECT * FROM faculty WHERE faculty_id = ? AND password = ? AND status = 'Active'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, facultyId);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractFacultyFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get faculty by ID
     * @param facultyId Faculty ID
     * @return Faculty object
     */
    public Faculty getFacultyById(String facultyId) {
        String sql = "SELECT * FROM faculty WHERE faculty_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, facultyId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractFacultyFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all faculty members
     * @return List of all faculty
     */
    public List<Faculty> getAllFaculty() {
        List<Faculty> facultyList = new ArrayList<>();
        String sql = "SELECT * FROM faculty ORDER BY faculty_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                facultyList.add(extractFacultyFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return facultyList;
    }
    
    /**
     * Get faculty by department
     * @param department Department name
     * @return List of faculty in that department
     */
    public List<Faculty> getFacultyByDepartment(String department) {
        List<Faculty> facultyList = new ArrayList<>();
        String sql = "SELECT * FROM faculty WHERE department = ? ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, department);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                facultyList.add(extractFacultyFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return facultyList;
    }
    
    /**
     * Add new faculty member
     * @param faculty Faculty object
     * @return true if successful, false otherwise
     */
    public boolean addFaculty(Faculty faculty) {
        String sql = "INSERT INTO faculty (faculty_id, name, department, email, phone, password, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, faculty.getFacultyId());
            stmt.setString(2, faculty.getName());
            stmt.setString(3, faculty.getDepartment());
            stmt.setString(4, faculty.getEmail());
            stmt.setString(5, faculty.getPhone());
            stmt.setString(6, faculty.getPassword());
            stmt.setString(7, faculty.getStatus());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update faculty information
     * @param faculty Faculty object with updated information
     * @return true if successful, false otherwise
     */
    public boolean updateFaculty(Faculty faculty) {
        String sql = "UPDATE faculty SET name = ?, department = ?, email = ?, phone = ?, " +
                     "password = ?, status = ? WHERE faculty_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, faculty.getName());
            stmt.setString(2, faculty.getDepartment());
            stmt.setString(3, faculty.getEmail());
            stmt.setString(4, faculty.getPhone());
            stmt.setString(5, faculty.getPassword());
            stmt.setString(6, faculty.getStatus());
            stmt.setString(7, faculty.getFacultyId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete faculty member
     * @param facultyId Faculty ID
     * @return true if successful, false otherwise
     */
    public boolean deleteFaculty(String facultyId) {
        String sql = "DELETE FROM faculty WHERE faculty_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, facultyId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Change faculty password
     * @param facultyId Faculty ID
     * @param newPassword New password
     * @return true if successful, false otherwise
     */
    public boolean changePassword(String facultyId, String newPassword) {
        String sql = "UPDATE faculty SET password = ? WHERE faculty_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newPassword);
            stmt.setString(2, facultyId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Search faculty by name or department
     * @param searchTerm Search term
     * @return List of matching faculty
     */
    public List<Faculty> searchFaculty(String searchTerm) {
        List<Faculty> facultyList = new ArrayList<>();
        String sql = "SELECT * FROM faculty WHERE faculty_id LIKE ? OR name LIKE ? OR department LIKE ? ORDER BY faculty_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                facultyList.add(extractFacultyFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return facultyList;
    }
    
    /**
     * Get faculty count
     * @return Total number of faculty
     */
    public int getFacultyCount() {
        String sql = "SELECT COUNT(*) as count FROM faculty WHERE status = 'Active'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Get subjects taught by a specific faculty member
     * @param facultyId Faculty ID
     * @return List of subject codes
     */
    public List<String> getFacultySubjects(String facultyId) {
        List<String> subjects = new ArrayList<>();
        String sql = "SELECT subject_code FROM faculty_subjects WHERE faculty_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, facultyId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                subjects.add(rs.getString("subject_code"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return subjects;
    }

    /**
     * Get all students who have marks or attendance in a faculty's assigned subjects.
     * Used to populate the "My Students" view in the faculty portal.
     * @param facultyId Faculty ID
     * @return List of students
     */
    public List<com.studentmanagement.model.Student> getStudentsForFaculty(String facultyId) {
        List<com.studentmanagement.model.Student> students = new ArrayList<>();
        // Fixed: proper subquery — gets students who have marks OR attendance
        // in any of this faculty's assigned subjects
        String sql =
            "SELECT DISTINCT s.roll_number, s.name, s.branch, s.year, s.section, " +
            "       s.email, s.status " +
            "FROM students s " +
            "WHERE s.roll_number IN ( " +
            "    SELECT m.roll_number FROM marks m " +
            "    JOIN faculty_subjects fs ON m.subject_code = fs.subject_code " +
            "    WHERE fs.faculty_id = ? " +
            "    UNION " +
            "    SELECT a.roll_number FROM attendance a " +
            "    JOIN faculty_subjects fs ON a.subject_code = fs.subject_code " +
            "    WHERE fs.faculty_id = ? " +
            ") ORDER BY s.roll_number";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, facultyId);
            stmt.setString(2, facultyId);  // second ? for attendance UNION branch
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                com.studentmanagement.model.Student student =
                    new com.studentmanagement.model.Student();
                student.setRollNumber(rs.getString("roll_number"));
                student.setName(rs.getString("name"));
                student.setBranch(rs.getString("branch"));
                student.setYear(rs.getInt("year"));
                student.setSection(rs.getString("section"));
                student.setStatus(rs.getString("status"));
                try { student.setEmail(rs.getString("email")); }
                catch (SQLException ignored) {}
                students.add(student);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }
    
    /**
     * Assign subject to faculty
     * @param facultyId Faculty ID
     * @param subjectCode Subject code
     * @return true if successful
     */
    public boolean assignSubject(String facultyId, String subjectCode) {
        String sql = "INSERT INTO faculty_subjects (faculty_id, subject_code) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, facultyId);
            stmt.setString(2, subjectCode);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Extract Faculty object from ResultSet
     * @param rs ResultSet
     * @return Faculty object
     * @throws SQLException
     */
    private Faculty extractFacultyFromResultSet(ResultSet rs) throws SQLException {
        Faculty faculty = new Faculty();
        faculty.setFacultyId(rs.getString("faculty_id"));
        faculty.setName(rs.getString("name"));
        faculty.setDepartment(rs.getString("department"));
        faculty.setEmail(rs.getString("email"));
        faculty.setPhone(rs.getString("phone"));
        faculty.setPassword(rs.getString("password"));
        faculty.setStatus(rs.getString("status"));
        return faculty;
    }
}