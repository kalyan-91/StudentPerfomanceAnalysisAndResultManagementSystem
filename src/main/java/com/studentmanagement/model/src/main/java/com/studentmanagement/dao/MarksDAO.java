package com.studentmanagement.dao;

import com.studentmanagement.database.DatabaseConnection;
import com.studentmanagement.model.Marks;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Marks Data Access Object
 * Complete implementation with CGPA/SGPA calculation
 */
public class MarksDAO {
    
    /**
     * Add new marks record
     */
    public boolean addMarks(Marks marks) {
        // Get pass marks from subject
        int passMarks = getPassMarks(marks.getSubjectCode());
        
        // Calculate everything before saving
        marks.computeResult(passMarks);
        
        String sql = "INSERT INTO marks (roll_number, subject_code, internal_marks, external_marks, " +
                     "total_marks, grade, grade_point, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, marks.getRollNumber());
            pstmt.setString(2, marks.getSubjectCode());
            pstmt.setInt(3, marks.getInternalMarks());
            pstmt.setInt(4, marks.getExternalMarks());
            pstmt.setInt(5, marks.getTotalMarks());
            pstmt.setString(6, marks.getGrade());
            pstmt.setDouble(7, marks.getGradePoint());
            pstmt.setString(8, marks.getStatus());
            
            int rows = pstmt.executeUpdate();
            System.out.println("Marks added: " + marks.getRollNumber() + " - " + marks.getSubjectCode() + 
                             " | Grade: " + marks.getGrade() + " | GP: " + marks.getGradePoint());
            return rows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding marks: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update existing marks record
     */
    public boolean updateMarks(Marks marks) {
        // Get pass marks from subject
        int passMarks = getPassMarks(marks.getSubjectCode());
        
        // Calculate everything before updating
        marks.computeResult(passMarks);
        
        String sql = "UPDATE marks SET internal_marks = ?, external_marks = ?, " +
                     "total_marks = ?, grade = ?, grade_point = ?, status = ? " +
                     "WHERE roll_number = ? AND subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, marks.getInternalMarks());
            pstmt.setInt(2, marks.getExternalMarks());
            pstmt.setInt(3, marks.getTotalMarks());
            pstmt.setString(4, marks.getGrade());
            pstmt.setDouble(5, marks.getGradePoint());
            pstmt.setString(6, marks.getStatus());
            pstmt.setString(7, marks.getRollNumber());
            pstmt.setString(8, marks.getSubjectCode());
            
            int rows = pstmt.executeUpdate();
            System.out.println("Marks updated: " + marks.getRollNumber() + " - " + marks.getSubjectCode() + 
                             " | Grade: " + marks.getGrade() + " | GP: " + marks.getGradePoint());
            return rows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating marks: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get marks for a specific student and subject
     */
    public Marks getMarks(String rollNumber, String subjectCode) {
        String sql = "SELECT m.*, s.subject_name, s.semester, s.credits " +
                     "FROM marks m " +
                     "LEFT JOIN subjects s ON m.subject_code = s.subject_code " +
                     "WHERE m.roll_number = ? AND m.subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            pstmt.setString(2, subjectCode);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractMarksFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching marks: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all marks for a student
     */
    public List<Marks> getMarksByStudent(String rollNumber) {
        List<Marks> marksList = new ArrayList<>();
        String sql = "SELECT m.*, s.subject_name, s.semester, s.credits " +
                     "FROM marks m " +
                     "LEFT JOIN subjects s ON m.subject_code = s.subject_code " +
                     "WHERE m.roll_number = ? " +
                     "ORDER BY s.semester, m.subject_code";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                marksList.add(extractMarksFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching student marks: " + e.getMessage());
            e.printStackTrace();
        }
        
        return marksList;
    }
    
    /**
     * Get all marks for a student with full subject details
     */
    public List<Marks> getMarksByStudentWithDetails(String rollNumber) {
        return getMarksByStudent(rollNumber);
    }
    
    /**
     * Get all marks for a subject
     */
    public List<Marks> getMarksBySubject(String subjectCode) {
        List<Marks> marksList = new ArrayList<>();
        String sql = "SELECT m.*, s.subject_name, s.semester, s.credits, st.name as student_name " +
                     "FROM marks m " +
                     "LEFT JOIN subjects s ON m.subject_code = s.subject_code " +
                     "LEFT JOIN students st ON m.roll_number = st.roll_number " +
                     "WHERE m.subject_code = ? " +
                     "ORDER BY m.roll_number";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, subjectCode);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                marksList.add(extractMarksFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching subject marks: " + e.getMessage());
            e.printStackTrace();
        }
        
        return marksList;
    }
    
    /**
     * Get all marks records
     */
    public List<Marks> getAllMarks() {
        List<Marks> marksList = new ArrayList<>();
        String sql = "SELECT m.*, s.subject_name, s.semester, s.credits " +
                     "FROM marks m " +
                     "LEFT JOIN subjects s ON m.subject_code = s.subject_code " +
                     "ORDER BY m.roll_number, s.semester, m.subject_code";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                marksList.add(extractMarksFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching all marks: " + e.getMessage());
            e.printStackTrace();
        }
        
        return marksList;
    }
    
    /**
     * Delete marks record
     */
    public boolean deleteMarks(String rollNumber, String subjectCode) {
        String sql = "DELETE FROM marks WHERE roll_number = ? AND subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            pstmt.setString(2, subjectCode);
            
            int rows = pstmt.executeUpdate();
            return rows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting marks: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Calculate CGPA for a student (Cumulative Grade Point Average)
     */
    public double calculateCGPA(String rollNumber) {
        String sql = "SELECT SUM(m.grade_point * s.credits) / SUM(s.credits) as cgpa " +
                     "FROM marks m " +
                     "INNER JOIN subjects s ON m.subject_code = s.subject_code " +
                     "WHERE m.roll_number = ? AND m.status = 'Pass'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                double cgpa = rs.getDouble("cgpa");
                return rs.wasNull() ? 0.0 : cgpa;
            }
            
        } catch (SQLException e) {
            System.err.println("Error calculating CGPA: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    /**
     * Calculate SGPA for a student in a specific semester
     */
    public double calculateSGPA(String rollNumber, int semester) {
        String sql = "SELECT SUM(m.grade_point * s.credits) / SUM(s.credits) as sgpa " +
                     "FROM marks m " +
                     "INNER JOIN subjects s ON m.subject_code = s.subject_code " +
                     "WHERE m.roll_number = ? AND s.semester = ? AND m.status = 'Pass'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            pstmt.setInt(2, semester);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                double sgpa = rs.getDouble("sgpa");
                return rs.wasNull() ? 0.0 : sgpa;
            }
            
        } catch (SQLException e) {
            System.err.println("Error calculating SGPA: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    /**
     * Get total credits earned by student (only passed subjects)
     */
    public int getTotalCreditsEarned(String rollNumber) {
        String sql = "SELECT SUM(s.credits) as total_credits " +
                     "FROM marks m " +
                     "INNER JOIN subjects s ON m.subject_code = s.subject_code " +
                     "WHERE m.roll_number = ? AND m.status = 'Pass'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("total_credits");
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting total credits: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Get average marks for a student
     */
    public double getAverageMarks(String rollNumber) {
        String sql = "SELECT AVG(total_marks) as avg_marks FROM marks WHERE roll_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("avg_marks");
            }
            
        } catch (SQLException e) {
            System.err.println("Error calculating average marks: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    /**
     * Get count of subjects passed by student
     */
    public int getPassedSubjectCount(String rollNumber) {
        String sql = "SELECT COUNT(*) as count FROM marks WHERE roll_number = ? AND status = 'Pass'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            
        } catch (SQLException e) {
            System.err.println("Error counting passed subjects: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Get count of subjects failed by student
     */
    public int getFailedSubjectCount(String rollNumber) {
        String sql = "SELECT COUNT(*) as count FROM marks WHERE roll_number = ? AND status = 'Fail'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            
        } catch (SQLException e) {
            System.err.println("Error counting failed subjects: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Get pass marks for a subject
     */
    private int getPassMarks(String subjectCode) {
        String sql = "SELECT pass_marks FROM subjects WHERE subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, subjectCode);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("pass_marks");
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching pass marks: " + e.getMessage());
        }
        
        return 40; // Default pass marks
    }
    
    /**
     * Extract Marks object from ResultSet
     */
    private Marks extractMarksFromResultSet(ResultSet rs) throws SQLException {
        Marks marks = new Marks();
        marks.setRollNumber(rs.getString("roll_number"));
        marks.setSubjectCode(rs.getString("subject_code"));
        marks.setInternalMarks(rs.getInt("internal_marks"));
        marks.setExternalMarks(rs.getInt("external_marks"));
        marks.setTotalMarks(rs.getInt("total_marks"));
        marks.setGrade(rs.getString("grade"));
        marks.setGradePoint(rs.getDouble("grade_point"));
        marks.setStatus(rs.getString("status"));
        
        // Try to get subject details if available from JOIN
        try {
            marks.setSubjectName(rs.getString("subject_name"));
            marks.setSemester(rs.getInt("semester"));
            marks.setCredits(rs.getInt("credits"));
        } catch (SQLException e) {
            // Ignore if columns don't exist (not a JOIN query)
        }
        
        return marks;
    }
    
    /**
     * Get top performers across all students
     */
    public List<StudentPerformance> getTopPerformers(int limit) {
        List<StudentPerformance> performers = new ArrayList<>();
        String sql = "SELECT st.roll_number, st.name, " +
                     "SUM(m.grade_point * s.credits) / SUM(s.credits) as cgpa, " +
                     "AVG(m.total_marks) as avg_marks " +
                     "FROM students st " +
                     "INNER JOIN marks m ON st.roll_number = m.roll_number " +
                     "INNER JOIN subjects s ON m.subject_code = s.subject_code " +
                     "WHERE m.status = 'Pass' " +
                     "GROUP BY st.roll_number, st.name " +
                     "ORDER BY cgpa DESC " +
                     "LIMIT ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                performers.add(new StudentPerformance(
                    rs.getString("roll_number"),
                    rs.getString("name"),
                    rs.getDouble("cgpa"),
                    rs.getDouble("avg_marks")
                ));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching top performers: " + e.getMessage());
            e.printStackTrace();
        }
        
        return performers;
    }
    
    /**
     * Inner class for student performance summary
     */
    public static class StudentPerformance {
        public String rollNumber;
        public String name;
        public double cgpa;
        public double averageMarks;
        
        public StudentPerformance(String rollNumber, String name, double cgpa, double averageMarks) {
            this.rollNumber = rollNumber;
            this.name = name;
            this.cgpa = cgpa;
            this.averageMarks = averageMarks;
        }
    }
}