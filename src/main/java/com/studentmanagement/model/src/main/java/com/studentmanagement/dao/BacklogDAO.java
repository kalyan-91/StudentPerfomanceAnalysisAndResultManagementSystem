 package com.studentmanagement.dao;

import com.studentmanagement.database.DatabaseConnection;
import com.studentmanagement.model.Backlog;
import com.studentmanagement.model.Marks;
import com.studentmanagement.model.Attendance;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Backlog Data Access Object
 * Complete implementation with automatic backlog detection and tracking
 */
public class BacklogDAO {
    
    private MarksDAO marksDAO = new MarksDAO();
    private AttendanceDAO attendanceDAO = new AttendanceDAO();
    
    /**
     * Add a new backlog record
     */
    public boolean addBacklog(Backlog backlog) {
        // Guarantee clearance_status is never NULL — a manually created backlog
        // using the default constructor won't have this set, which breaks all
        // COUNT queries that filter on clearance_status = 'Pending'.
        if (backlog.getClearanceStatus() == null || backlog.getClearanceStatus().isEmpty()) {
            backlog.setClearanceStatus("Pending");
        }

        String sql = "INSERT INTO backlogs (roll_number, subject_code, subject_name, semester, " +
                     "reason, clearance_status) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, backlog.getRollNumber());
            pstmt.setString(2, backlog.getSubjectCode());
            pstmt.setString(3, backlog.getSubjectName());
            pstmt.setInt(4, backlog.getSemester());
            pstmt.setString(5, backlog.getReason());
            pstmt.setString(6, backlog.getClearanceStatus());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    backlog.setBacklogId(rs.getInt(1));
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error adding backlog: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get backlog by ID
     */
    public Backlog getBacklogById(int backlogId) {
        String sql = "SELECT * FROM backlogs WHERE backlog_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, backlogId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractBacklogFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching backlog: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all backlogs for a student
     */
    public List<Backlog> getBacklogsByStudent(String rollNumber) {
        List<Backlog> backlogs = new ArrayList<>();
        String sql = "SELECT * FROM backlogs WHERE roll_number = ? ORDER BY semester, subject_code";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                backlogs.add(extractBacklogFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching student backlogs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return backlogs;
    }
    
    /**
     * Get pending backlogs for a student
     */
    public List<Backlog> getPendingBacklogs(String rollNumber) {
        List<Backlog> backlogs = new ArrayList<>();
        String sql = "SELECT * FROM backlogs WHERE roll_number = ? AND clearance_status = 'Pending' " +
                     "ORDER BY semester, subject_code";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                backlogs.add(extractBacklogFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching pending backlogs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return backlogs;
    }
    
    /**
     * Get cleared backlogs for a student
     */
    public List<Backlog> getClearedBacklogs(String rollNumber) {
        List<Backlog> backlogs = new ArrayList<>();
        String sql = "SELECT * FROM backlogs WHERE roll_number = ? AND clearance_status = 'Cleared' " +
                     "ORDER BY clearance_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                backlogs.add(extractBacklogFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching cleared backlogs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return backlogs;
    }
    
    /**
     * Mark backlog as cleared
     */
    public boolean markBacklogAsCleared(int backlogId) {
        String sql = "UPDATE backlogs SET clearance_status = 'Cleared', clearance_date = ? " +
                     "WHERE backlog_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setInt(2, backlogId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error clearing backlog: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update backlog status
     */
    public boolean updateBacklogStatus(int backlogId, String status) {
        String sql = "UPDATE backlogs SET clearance_status = ? WHERE backlog_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, backlogId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating backlog status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete backlog record
     */
    public boolean deleteBacklog(int backlogId) {
        String sql = "DELETE FROM backlogs WHERE backlog_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, backlogId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting backlog: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // NEW: AUTOMATIC BACKLOG DETECTION METHODS
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Automatically detect and create backlogs based on marks (all students)
     * Called from admin panel or scheduled task
     */
    public int autoDetectBacklogsFromMarks() {
        String sql = "INSERT INTO backlogs (roll_number, subject_code, subject_name, semester, reason, clearance_status) " +
                     "SELECT m.roll_number, m.subject_code, " +
                     "COALESCE((SELECT subject_name FROM subjects WHERE subject_code = m.subject_code), 'Unknown'), " +
                     "COALESCE((SELECT semester FROM subjects WHERE subject_code = m.subject_code), 0), " +
                     "'Marks', 'Pending' " +
                     "FROM marks m " +
                     "WHERE m.status = 'Fail' " +
                     "AND NOT EXISTS (" +
                     "    SELECT 1 FROM backlogs b " +
                     "    WHERE b.roll_number = m.roll_number " +
                     "    AND b.subject_code = m.subject_code " +
                     "    AND b.reason = 'Marks' " +
                     "    AND b.clearance_status = 'Pending'" +
                     ")";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int count = stmt.executeUpdate();
            System.out.println("Auto-detected " + count + " backlogs from marks");
            return count;
            
        } catch (SQLException e) {
            System.err.println("Error auto-detecting backlogs from marks: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Automatically detect and create backlogs based on attendance (all students)
     */
    public int autoDetectBacklogsFromAttendance() {
        String sql = "INSERT INTO backlogs (roll_number, subject_code, subject_name, semester, reason, clearance_status) " +
                     "SELECT a.roll_number, a.subject_code, " +
                     "COALESCE((SELECT subject_name FROM subjects WHERE subject_code = a.subject_code), 'Unknown'), " +
                     "COALESCE((SELECT semester FROM subjects WHERE subject_code = a.subject_code), 0), " +
                     "'Attendance', 'Pending' " +
                     "FROM attendance a " +
                     "WHERE a.attendance_percentage < 75.0 " +
                     "AND NOT EXISTS (" +
                     "    SELECT 1 FROM backlogs b " +
                     "    WHERE b.roll_number = a.roll_number " +
                     "    AND b.subject_code = a.subject_code " +
                     "    AND b.reason = 'Attendance' " +
                     "    AND b.clearance_status = 'Pending'" +
                     ")";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int count = stmt.executeUpdate();
            System.out.println("Auto-detected " + count + " backlogs from attendance");
            return count;
            
        } catch (SQLException e) {
            System.err.println("Error auto-detecting backlogs from attendance: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Detect backlogs for a specific student and subject
     * Called immediately after entering marks or attendance
     */
    public void detectBacklogForStudent(String rollNumber, String subjectCode) {

    try (Connection conn = DatabaseConnection.getConnection()) {

        String getMarksSQL =
                "SELECT status FROM marks WHERE roll_number = ? AND subject_code = ?";

        PreparedStatement ps = conn.prepareStatement(getMarksSQL);
        ps.setString(1, rollNumber);
        ps.setString(2, subjectCode);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            String status = rs.getString("status");

            if ("Fail".equalsIgnoreCase(status)) {

                String checkSQL =
                        "SELECT backlog_id FROM backlogs WHERE roll_number = ? AND subject_code = ? AND clearance_status = 'Pending'";

                PreparedStatement checkPs = conn.prepareStatement(checkSQL);
                checkPs.setString(1, rollNumber);
                checkPs.setString(2, subjectCode);

                ResultSet checkRs = checkPs.executeQuery();

                if (!checkRs.next()) {

                    String insertSQL =
                            "INSERT INTO backlogs (roll_number, subject_code, subject_name, semester, reason, clearance_status) "
                                    + "VALUES (?, ?, ?, ?, ?, ?)";

                    PreparedStatement insertPs = conn.prepareStatement(insertSQL);
                    insertPs.setString(1, rollNumber);
                    insertPs.setString(2, subjectCode);
                    insertPs.setString(3, getSubjectName(subjectCode));
                    insertPs.setInt(4, getSubjectSemester(subjectCode));
                    insertPs.setString(5, "Marks");
                    insertPs.setString(6, "Pending");

                    insertPs.executeUpdate();
                }

            } else {

                String deleteSQL =
                        "DELETE FROM backlogs WHERE roll_number = ? AND subject_code = ? AND reason = 'Marks'";

                PreparedStatement deletePs = conn.prepareStatement(deleteSQL);
                deletePs.setString(1, rollNumber);
                deletePs.setString(2, subjectCode);

                deletePs.executeUpdate();
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    /**
     * Clear backlog automatically when student passes
     * Called after updating marks to passing grade
     */
    public void clearBacklogIfPassed(String rollNumber, String subjectCode) {
        // Check if student now has passing marks
        String marksSql = "SELECT status FROM marks WHERE roll_number = ? AND subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(marksSql)) {
            
            stmt.setString(1, rollNumber);
            stmt.setString(2, subjectCode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && "Pass".equals(rs.getString("status"))) {
                // Clear the marks-related backlog
                String clearSql = "UPDATE backlogs SET clearance_status = 'Cleared', clearance_date = ? " +
                                 "WHERE roll_number = ? AND subject_code = ? " +
                                 "AND reason = 'Marks' AND clearance_status = 'Pending'";
                
                try (PreparedStatement clearStmt = conn.prepareStatement(clearSql)) {
                    clearStmt.setDate(1, Date.valueOf(LocalDate.now()));
                    clearStmt.setString(2, rollNumber);
                    clearStmt.setString(3, subjectCode);
                    
                    int cleared = clearStmt.executeUpdate();
                    if (cleared > 0) {
                        System.out.println("Cleared marks backlog for " + rollNumber + " - " + subjectCode);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error clearing backlog: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Check if attendance is now >= 75%
        String attendanceSql = "SELECT attendance_percentage FROM attendance WHERE roll_number = ? AND subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(attendanceSql)) {
            
            stmt.setString(1, rollNumber);
            stmt.setString(2, subjectCode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getDouble("attendance_percentage") >= 75.0) {
                // Clear the attendance-related backlog
                String clearSql = "UPDATE backlogs SET clearance_status = 'Cleared', clearance_date = ? " +
                                 "WHERE roll_number = ? AND subject_code = ? " +
                                 "AND reason = 'Attendance' AND clearance_status = 'Pending'";
                
                try (PreparedStatement clearStmt = conn.prepareStatement(clearSql)) {
                    clearStmt.setDate(1, Date.valueOf(LocalDate.now()));
                    clearStmt.setString(2, rollNumber);
                    clearStmt.setString(3, subjectCode);
                    
                    int cleared = clearStmt.executeUpdate();
                    if (cleared > 0) {
                        System.out.println("Cleared attendance backlog for " + rollNumber + " - " + subjectCode);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error clearing attendance backlog: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Check if backlog exists for a student-subject-reason combination
     */
    private boolean backlogExists(String rollNumber, String subjectCode, String reason) {
        String sql = "SELECT COUNT(*) FROM backlogs WHERE roll_number = ? AND subject_code = ? " +
                     "AND reason = ? AND clearance_status = 'Pending'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            pstmt.setString(2, subjectCode);
            pstmt.setString(3, reason);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking backlog existence: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get subject semester
     */
    private int getSubjectSemester(String subjectCode) {
        String sql = "SELECT semester FROM subjects WHERE subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, subjectCode);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("semester");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // END OF NEW AUTO-DETECTION METHODS
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * AUTOMATIC BACKLOG DETECTION (Legacy method - kept for compatibility)
     * Detect backlogs based on marks and attendance
     */
    public int detectAndRecordBacklogs(String rollNumber, int year, int semester) {
        int backlogsDetected = 0;
        
        // Get all marks for the student in this semester
        List<Marks> marksList = marksDAO.getMarksByStudent(rollNumber);
        
        for (Marks marks : marksList) {
            // Check if backlog already exists for this subject
            if (backlogExists(rollNumber, marks.getSubjectCode())) {
                continue;
            }
            
            // Check for marks-based backlog
            if ("Fail".equals(marks.getStatus())) {
                Backlog backlog = new Backlog(
                    0, // ID will be auto-generated
                    rollNumber,
                    marks.getSubjectCode(),
                    getSubjectName(marks.getSubjectCode()),
                    semester,
                    "Marks"
                );
                
                if (addBacklog(backlog)) {
                    backlogsDetected++;
                }
            }
            
            // Check for attendance-based backlog
            Attendance attendance = attendanceDAO.getAttendance(rollNumber, marks.getSubjectCode());
            if (attendance != null && "Not Eligible".equals(attendance.getEligibilityStatus())) {
                Backlog backlog = new Backlog(
                    0,
                    rollNumber,
                    marks.getSubjectCode(),
                    getSubjectName(marks.getSubjectCode()),
                    semester,
                    "Attendance"
                );
                
                if (addBacklog(backlog)) {
                    backlogsDetected++;
                }
            }
        }
        
        return backlogsDetected;
    }
    
    /**
     * Check if backlog exists for a student-subject combination (any reason)
     */
    public boolean backlogExists(String rollNumber, String subjectCode) {
        String sql = "SELECT COUNT(*) FROM backlogs WHERE roll_number = ? AND subject_code = ? " +
                     "AND clearance_status = 'Pending'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            pstmt.setString(2, subjectCode);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking backlog existence: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get total pending backlog count for a student
     */
    public int getPendingBacklogCount(String rollNumber) {
        String sql = "SELECT COUNT(*) FROM backlogs WHERE roll_number = ? AND clearance_status = 'Pending'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error counting backlogs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Get total cleared backlog count for a student
     */
    public int getClearedBacklogCount(String rollNumber) {
        String sql = "SELECT COUNT(*) FROM backlogs WHERE roll_number = ? AND clearance_status = 'Cleared'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error counting cleared backlogs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Get all backlogs by subject
     */
    public List<Backlog> getBacklogsBySubject(String subjectCode) {
        List<Backlog> backlogs = new ArrayList<>();
        String sql = "SELECT b.*, st.name as student_name FROM backlogs b " +
                     "JOIN students st ON b.roll_number = st.roll_number " +
                     "WHERE b.subject_code = ? ORDER BY b.clearance_status, b.roll_number";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, subjectCode);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                backlogs.add(extractBacklogFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching subject backlogs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return backlogs;
    }
    
    /**
     * Get backlog statistics for a student
     */
    public BacklogStats getStudentBacklogStats(String rollNumber) {
        String sql = "SELECT " +
                     "COUNT(*) as total_backlogs, " +
                     "SUM(CASE WHEN clearance_status = 'Pending' THEN 1 ELSE 0 END) as pending_count, " +
                     "SUM(CASE WHEN clearance_status = 'Cleared' THEN 1 ELSE 0 END) as cleared_count, " +
                     "SUM(CASE WHEN reason = 'Marks' THEN 1 ELSE 0 END) as marks_backlogs, " +
                     "SUM(CASE WHEN reason = 'Attendance' THEN 1 ELSE 0 END) as attendance_backlogs " +
                     "FROM backlogs WHERE roll_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new BacklogStats(
                    rs.getInt("total_backlogs"),
                    rs.getInt("pending_count"),
                    rs.getInt("cleared_count"),
                    rs.getInt("marks_backlogs"),
                    rs.getInt("attendance_backlogs")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error calculating backlog stats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get students with most backlogs
     */
    public List<BacklogSummary> getStudentsWithMostBacklogs(int limit) {
        List<BacklogSummary> summaries = new ArrayList<>();
        String sql = "SELECT b.roll_number, st.name, COUNT(*) as backlog_count " +
                     "FROM backlogs b " +
                     "JOIN students st ON b.roll_number = st.roll_number " +
                     "WHERE b.clearance_status = 'Pending' " +
                     "GROUP BY b.roll_number, st.name " +
                     "ORDER BY backlog_count DESC LIMIT ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                summaries.add(new BacklogSummary(
                    rs.getString("roll_number"),
                    rs.getString("name"),
                    rs.getInt("backlog_count")
                ));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching backlog summary: " + e.getMessage());
            e.printStackTrace();
        }
        
        return summaries;
    }
    
    /**
     * Get backlog history for a student (complete record)
     */
    public List<Backlog> getBacklogHistory(String rollNumber) {
        List<Backlog> history = new ArrayList<>();
        String sql = "SELECT * FROM backlogs WHERE roll_number = ? " +
                     "ORDER BY created_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                history.add(extractBacklogFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching backlog history: " + e.getMessage());
            e.printStackTrace();
        }
        
        return history;
    }
    
    /**
     * Helper method to get subject name
     */
    private String getSubjectName(String subjectCode) {
        String sql = "SELECT subject_name FROM subjects WHERE subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, subjectCode);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("subject_name");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return "Unknown Subject";
    }
    
    /**
     * Helper method to extract Backlog object from ResultSet
     */
    private Backlog extractBacklogFromResultSet(ResultSet rs) throws SQLException {
        Backlog backlog = new Backlog();
        backlog.setBacklogId(rs.getInt("backlog_id"));
        backlog.setRollNumber(rs.getString("roll_number"));
        backlog.setSubjectCode(rs.getString("subject_code"));
        backlog.setSubjectName(rs.getString("subject_name"));
        backlog.setSemester(rs.getInt("semester"));
        backlog.setReason(rs.getString("reason"));
        backlog.setClearanceStatus(rs.getString("clearance_status"));
        
        Date clearanceDate = rs.getDate("clearance_date");
        if (clearanceDate != null) {
            backlog.setClearanceDate(clearanceDate.toString());
        }
        
        return backlog;
    }
    
    /**
     * Inner class for backlog statistics
     */
    public static class BacklogStats {
        public int totalBacklogs;
        public int pendingCount;
        public int clearedCount;
        public int marksBacklogs;
        public int attendanceBacklogs;
        
        public BacklogStats(int totalBacklogs, int pendingCount, int clearedCount,
                           int marksBacklogs, int attendanceBacklogs) {
            this.totalBacklogs = totalBacklogs;
            this.pendingCount = pendingCount;
            this.clearedCount = clearedCount;
            this.marksBacklogs = marksBacklogs;
            this.attendanceBacklogs = attendanceBacklogs;
        }
    }
    
    /**
     * Inner class for backlog summary
     */
    public static class BacklogSummary {
        public String rollNumber;
        public String studentName;
        public int backlogCount;
        
        public BacklogSummary(String rollNumber, String studentName, int backlogCount) {
            this.rollNumber = rollNumber;
            this.studentName = studentName;
            this.backlogCount = backlogCount;
        }
    }
}