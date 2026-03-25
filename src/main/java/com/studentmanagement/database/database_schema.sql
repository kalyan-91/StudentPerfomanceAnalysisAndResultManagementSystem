-- ============================================================================
-- Student Management System - Complete Database Schema
-- Updated: February 2026
-- Includes: Role-based access, Faculty management, Enrollments, Grade Points
-- ============================================================================

-- Create Database
CREATE DATABASE IF NOT EXISTS student_management_db;
USE student_management_db;

-- Drop existing tables (in correct order to handle foreign keys)
DROP TABLE IF EXISTS student_enrollments;
DROP TABLE IF EXISTS faculty_subjects;
DROP TABLE IF EXISTS backlogs;
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS marks;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS faculty;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS users;

-- ============================================================================
-- TABLE 1: Students
-- ============================================================================
CREATE TABLE students (
    roll_number VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(15),
    branch VARCHAR(50) NOT NULL,
    year INT NOT NULL CHECK (year BETWEEN 1 AND 4),
    section VARCHAR(10) NOT NULL,
    password VARCHAR(255) NOT NULL DEFAULT 'student123',
    status VARCHAR(20) NOT NULL DEFAULT 'Active',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_student_branch ON students(branch);
CREATE INDEX idx_student_year ON students(year);
CREATE INDEX idx_student_name ON students(name);
CREATE INDEX idx_student_status ON students(status);

-- ============================================================================
-- TABLE 2: Faculty
-- ============================================================================
CREATE TABLE faculty (
    faculty_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(15),
    password VARCHAR(255) NOT NULL DEFAULT 'faculty123',
    status VARCHAR(20) NOT NULL DEFAULT 'Active',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_faculty_department ON faculty(department);
CREATE INDEX idx_faculty_name ON faculty(name);

-- ============================================================================
-- TABLE 3: Subjects
-- ============================================================================
CREATE TABLE subjects (
    subject_code VARCHAR(20) PRIMARY KEY,
    subject_name VARCHAR(100) NOT NULL,
    branch VARCHAR(50) NOT NULL,
    year INT NOT NULL CHECK (year BETWEEN 1 AND 4),
    semester INT NOT NULL CHECK (semester BETWEEN 1 AND 8),
    credits INT NOT NULL DEFAULT 4,
    max_marks INT NOT NULL DEFAULT 100,
    pass_marks INT NOT NULL DEFAULT 40,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_subject_year_sem ON subjects(year, semester);
CREATE INDEX idx_subject_branch ON subjects(branch);

-- ============================================================================
-- TABLE 4: Faculty-Subject Mapping
-- Defines which faculty teaches which subjects
-- ============================================================================
CREATE TABLE faculty_subjects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    faculty_id VARCHAR(20) NOT NULL,
    subject_code VARCHAR(20) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id) ON DELETE CASCADE,
    FOREIGN KEY (subject_code) REFERENCES subjects(subject_code) ON DELETE CASCADE,
    UNIQUE KEY unique_faculty_subject (faculty_id, subject_code)
);

CREATE INDEX idx_fs_faculty ON faculty_subjects(faculty_id);
CREATE INDEX idx_fs_subject ON faculty_subjects(subject_code);

-- ============================================================================
-- TABLE 5: Student Enrollments
-- Defines which students are enrolled in which subjects
-- ============================================================================
CREATE TABLE student_enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(20) NOT NULL,
    subject_code VARCHAR(20) NOT NULL,
    year INT NOT NULL,
    semester INT NOT NULL,
    enrollment_date DATE DEFAULT (CURDATE()),
    status VARCHAR(20) DEFAULT 'Active',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (roll_number) REFERENCES students(roll_number) ON DELETE CASCADE,
    FOREIGN KEY (subject_code) REFERENCES subjects(subject_code) ON DELETE CASCADE,
    UNIQUE KEY unique_enrollment (roll_number, subject_code)
);

CREATE INDEX idx_enrollment_student ON student_enrollments(roll_number);
CREATE INDEX idx_enrollment_subject ON student_enrollments(subject_code);
CREATE INDEX idx_enrollment_status ON student_enrollments(status);

-- ============================================================================
-- TABLE 6: Marks (with Grade Point System)
-- University Grading: S(10.0) A1(9.5) A2(9.0) A3(8.5) B1(8.0) B2(7.5) 
--                     C1(7.0) C2(6.5) D1(6.0) D2(5.5) F(0.0)
-- ============================================================================
CREATE TABLE marks (
    marks_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(20) NOT NULL,
    subject_code VARCHAR(20) NOT NULL,
    internal_marks INT NOT NULL DEFAULT 0,
    external_marks INT NOT NULL DEFAULT 0,
    total_marks INT DEFAULT 0,
    grade VARCHAR(5),
    grade_point DECIMAL(3,1) DEFAULT 0.0,
    status VARCHAR(10),
    exam_date DATE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (roll_number) REFERENCES students(roll_number) ON DELETE CASCADE,
    FOREIGN KEY (subject_code) REFERENCES subjects(subject_code) ON DELETE CASCADE,
    UNIQUE KEY unique_marks (roll_number, subject_code)
);

CREATE INDEX idx_marks_student ON marks(roll_number);
CREATE INDEX idx_marks_subject ON marks(subject_code);
CREATE INDEX idx_marks_grade ON marks(grade);
CREATE INDEX idx_marks_status ON marks(status);
CREATE INDEX idx_marks_grade_point ON marks(grade_point);

-- ============================================================================
-- TABLE 7: Attendance
-- ============================================================================
CREATE TABLE attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(20) NOT NULL,
    subject_code VARCHAR(20) NOT NULL,
    total_classes INT NOT NULL DEFAULT 0,
    classes_attended INT NOT NULL DEFAULT 0,
    attendance_percentage DECIMAL(5,2) DEFAULT 0.0,
    eligibility_status VARCHAR(20),
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (roll_number) REFERENCES students(roll_number) ON DELETE CASCADE,
    FOREIGN KEY (subject_code) REFERENCES subjects(subject_code) ON DELETE CASCADE,
    UNIQUE KEY unique_attendance (roll_number, subject_code)
);

CREATE INDEX idx_attendance_student ON attendance(roll_number);
CREATE INDEX idx_attendance_subject ON attendance(subject_code);
CREATE INDEX idx_attendance_percentage ON attendance(attendance_percentage);

-- ============================================================================
-- TABLE 8: Backlogs
-- ============================================================================
CREATE TABLE backlogs (
    backlog_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(20) NOT NULL,
    subject_code VARCHAR(20) NOT NULL,
    subject_name VARCHAR(100) NOT NULL,
    semester INT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    clearance_status VARCHAR(20) DEFAULT 'Pending',
    clearance_date DATE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (roll_number) REFERENCES students(roll_number) ON DELETE CASCADE,
    FOREIGN KEY (subject_code) REFERENCES subjects(subject_code) ON DELETE CASCADE
);

CREATE INDEX idx_backlog_student ON backlogs(roll_number);
CREATE INDEX idx_backlog_subject ON backlogs(subject_code);
CREATE INDEX idx_backlog_status ON backlogs(clearance_status);

-- ============================================================================
-- TABLE 9: Users (Optional - for centralized authentication)
-- ============================================================================
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100),
    status VARCHAR(20) DEFAULT 'Active',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- TRIGGERS: Auto-calculate marks
-- ============================================================================

DELIMITER $$

-- Trigger: Before inserting marks, calculate total, grade, grade_point, status
CREATE TRIGGER before_marks_insert 
BEFORE INSERT ON marks
FOR EACH ROW
BEGIN
    DECLARE pass_marks_val INT;
    
    -- Calculate total marks
    SET NEW.total_marks = NEW.internal_marks + NEW.external_marks;
    
    -- Calculate grade
    SET NEW.grade = CASE
        WHEN NEW.total_marks >= 95 THEN 'S'
        WHEN NEW.total_marks >= 90 THEN 'A1'
        WHEN NEW.total_marks >= 85 THEN 'A2'
        WHEN NEW.total_marks >= 80 THEN 'A3'
        WHEN NEW.total_marks >= 75 THEN 'B1'
        WHEN NEW.total_marks >= 70 THEN 'B2'
        WHEN NEW.total_marks >= 65 THEN 'C1'
        WHEN NEW.total_marks >= 60 THEN 'C2'
        WHEN NEW.total_marks >= 55 THEN 'D1'
        WHEN NEW.total_marks >= 50 THEN 'D2'
        ELSE 'F'
    END;
    
    -- Calculate grade point
    SET NEW.grade_point = CASE
        WHEN NEW.total_marks >= 95 THEN 10.0
        WHEN NEW.total_marks >= 90 THEN 9.5
        WHEN NEW.total_marks >= 85 THEN 9.0
        WHEN NEW.total_marks >= 80 THEN 8.5
        WHEN NEW.total_marks >= 75 THEN 8.0
        WHEN NEW.total_marks >= 70 THEN 7.5
        WHEN NEW.total_marks >= 65 THEN 7.0
        WHEN NEW.total_marks >= 60 THEN 6.5
        WHEN NEW.total_marks >= 55 THEN 6.0
        WHEN NEW.total_marks >= 50 THEN 5.5
        ELSE 0.0
    END;
    
    -- Get pass marks for subject
    SELECT pass_marks INTO pass_marks_val 
    FROM subjects 
    WHERE subject_code = NEW.subject_code;
    
    IF pass_marks_val IS NULL THEN
        SET pass_marks_val = 40;
    END IF;
    
    -- Determine status
    SET NEW.status = IF(NEW.total_marks >= pass_marks_val, 'Pass', 'Fail');
END$$

-- Trigger: Before updating marks
CREATE TRIGGER before_marks_update 
BEFORE UPDATE ON marks
FOR EACH ROW
BEGIN
    DECLARE pass_marks_val INT;
    
    SET NEW.total_marks = NEW.internal_marks + NEW.external_marks;
    
    SET NEW.grade = CASE
        WHEN NEW.total_marks >= 95 THEN 'S'
        WHEN NEW.total_marks >= 90 THEN 'A1'
        WHEN NEW.total_marks >= 85 THEN 'A2'
        WHEN NEW.total_marks >= 80 THEN 'A3'
        WHEN NEW.total_marks >= 75 THEN 'B1'
        WHEN NEW.total_marks >= 70 THEN 'B2'
        WHEN NEW.total_marks >= 65 THEN 'C1'
        WHEN NEW.total_marks >= 60 THEN 'C2'
        WHEN NEW.total_marks >= 55 THEN 'D1'
        WHEN NEW.total_marks >= 50 THEN 'D2'
        ELSE 'F'
    END;
    
    SET NEW.grade_point = CASE
        WHEN NEW.total_marks >= 95 THEN 10.0
        WHEN NEW.total_marks >= 90 THEN 9.5
        WHEN NEW.total_marks >= 85 THEN 9.0
        WHEN NEW.total_marks >= 80 THEN 8.5
        WHEN NEW.total_marks >= 75 THEN 8.0
        WHEN NEW.total_marks >= 70 THEN 7.5
        WHEN NEW.total_marks >= 65 THEN 7.0
        WHEN NEW.total_marks >= 60 THEN 6.5
        WHEN NEW.total_marks >= 55 THEN 6.0
        WHEN NEW.total_marks >= 50 THEN 5.5
        ELSE 0.0
    END;
    
    SELECT pass_marks INTO pass_marks_val 
    FROM subjects 
    WHERE subject_code = NEW.subject_code;
    
    IF pass_marks_val IS NULL THEN
        SET pass_marks_val = 40;
    END IF;
    
    SET NEW.status = IF(NEW.total_marks >= pass_marks_val, 'Pass', 'Fail');
END$$

DELIMITER ;

-- ============================================================================
-- TRIGGERS: Auto-calculate attendance
-- ============================================================================

DELIMITER $$

CREATE TRIGGER before_attendance_insert
BEFORE INSERT ON attendance
FOR EACH ROW
BEGIN
    IF NEW.total_classes > 0 THEN
        SET NEW.attendance_percentage = (NEW.classes_attended * 100.0) / NEW.total_classes;
    ELSE
        SET NEW.attendance_percentage = 0.0;
    END IF;
    
    SET NEW.eligibility_status = IF(NEW.attendance_percentage >= 75, 'Eligible', 'Not Eligible');
END$$

CREATE TRIGGER before_attendance_update
BEFORE UPDATE ON attendance
FOR EACH ROW
BEGIN
    IF NEW.total_classes > 0 THEN
        SET NEW.attendance_percentage = (NEW.classes_attended * 100.0) / NEW.total_classes;
    ELSE
        SET NEW.attendance_percentage = 0.0;
    END IF;
    
    SET NEW.eligibility_status = IF(NEW.attendance_percentage >= 75, 'Eligible', 'Not Eligible');
END$$

DELIMITER ;

-- ============================================================================
-- STORED PROCEDURES
-- ============================================================================

DELIMITER $$

-- Calculate CGPA for a student
CREATE PROCEDURE calculate_cgpa(IN student_roll VARCHAR(20), OUT cgpa DECIMAL(4,2))
BEGIN
    SELECT 
        COALESCE(SUM(m.grade_point * s.credits) / SUM(s.credits), 0.0)
    INTO cgpa
    FROM marks m
    INNER JOIN subjects s ON m.subject_code = s.subject_code
    WHERE m.roll_number = student_roll AND m.status = 'Pass';
END$$

-- Calculate SGPA for a student in a semester
CREATE PROCEDURE calculate_sgpa(
    IN student_roll VARCHAR(20), 
    IN sem INT, 
    OUT sgpa DECIMAL(4,2)
)
BEGIN
    SELECT 
        COALESCE(SUM(m.grade_point * s.credits) / SUM(s.credits), 0.0)
    INTO sgpa
    FROM marks m
    INNER JOIN subjects s ON m.subject_code = s.subject_code
    WHERE m.roll_number = student_roll 
      AND s.semester = sem 
      AND m.status = 'Pass';
END$$

-- Auto-detect backlogs from marks and attendance
CREATE PROCEDURE detect_backlogs()
BEGIN
    -- Detect backlogs from failed marks
    INSERT INTO backlogs (roll_number, subject_code, subject_name, semester, reason, clearance_status)
    SELECT 
        m.roll_number,
        m.subject_code,
        s.subject_name,
        s.semester,
        'Marks',
        'Pending'
    FROM marks m
    INNER JOIN subjects s ON m.subject_code = s.subject_code
    WHERE m.status = 'Fail'
      AND NOT EXISTS (
          SELECT 1 FROM backlogs b 
          WHERE b.roll_number = m.roll_number 
            AND b.subject_code = m.subject_code 
            AND b.reason = 'Marks'
            AND b.clearance_status = 'Pending'
      );
    
    -- Detect backlogs from low attendance
    INSERT INTO backlogs (roll_number, subject_code, subject_name, semester, reason, clearance_status)
    SELECT 
        a.roll_number,
        a.subject_code,
        s.subject_name,
        s.semester,
        'Attendance',
        'Pending'
    FROM attendance a
    INNER JOIN subjects s ON a.subject_code = s.subject_code
    WHERE a.attendance_percentage < 75.0
      AND NOT EXISTS (
          SELECT 1 FROM backlogs b 
          WHERE b.roll_number = a.roll_number 
            AND b.subject_code = a.subject_code 
            AND b.reason = 'Attendance'
            AND b.clearance_status = 'Pending'
      );
END$$

DELIMITER ;

-- ============================================================================
-- VIEWS: Performance Analysis
-- ============================================================================

-- Student Performance Summary
CREATE VIEW student_performance AS
SELECT 
    s.roll_number,
    s.name,
    s.branch,
    s.year,
    s.section,
    COUNT(m.marks_id) as subjects_taken,
    AVG(m.total_marks) as average_marks,
    COALESCE(SUM(m.grade_point * sub.credits) / SUM(sub.credits), 0.0) as cgpa,
    SUM(CASE WHEN m.status = 'Pass' THEN 1 ELSE 0 END) as subjects_passed,
    SUM(CASE WHEN m.status = 'Fail' THEN 1 ELSE 0 END) as subjects_failed
FROM students s
LEFT JOIN marks m ON s.roll_number = m.roll_number
LEFT JOIN subjects sub ON m.subject_code = sub.subject_code AND m.status = 'Pass'
GROUP BY s.roll_number;

-- Attendance Summary
CREATE VIEW attendance_summary AS
SELECT 
    s.roll_number,
    s.name,
    COUNT(a.attendance_id) as subjects_enrolled,
    COALESCE(AVG(a.attendance_percentage), 0.0) as overall_attendance,
    SUM(CASE WHEN a.eligibility_status = 'Eligible' THEN 1 ELSE 0 END) as eligible_subjects,
    SUM(CASE WHEN a.eligibility_status = 'Not Eligible' THEN 1 ELSE 0 END) as shortage_subjects
FROM students s
LEFT JOIN attendance a ON s.roll_number = a.roll_number
GROUP BY s.roll_number;

-- Subject Performance
CREATE VIEW subject_performance AS
SELECT 
    sub.subject_code,
    sub.subject_name,
    sub.branch,
    sub.year,
    sub.semester,
    COUNT(m.marks_id) as students_enrolled,
    AVG(m.total_marks) as average_marks,
    AVG(m.grade_point) as average_gp,
    SUM(CASE WHEN m.status = 'Pass' THEN 1 ELSE 0 END) as pass_count,
    SUM(CASE WHEN m.status = 'Fail' THEN 1 ELSE 0 END) as fail_count,
    CASE 
        WHEN COUNT(m.marks_id) > 0 
        THEN (SUM(CASE WHEN m.status = 'Pass' THEN 1 ELSE 0 END) * 100.0 / COUNT(m.marks_id))
        ELSE 0.0 
    END as pass_percentage
FROM subjects sub
LEFT JOIN marks m ON sub.subject_code = m.subject_code
GROUP BY sub.subject_code;

-- Faculty Workload
CREATE VIEW faculty_workload AS
SELECT 
    f.faculty_id,
    f.name,
    f.department,
    COUNT(DISTINCT fs.subject_code) as subjects_teaching,
    COUNT(DISTINCT se.roll_number) as total_students
FROM faculty f
LEFT JOIN faculty_subjects fs ON f.faculty_id = fs.faculty_id
LEFT JOIN student_enrollments se ON fs.subject_code = se.subject_code
GROUP BY f.faculty_id;

-- ============================================================================
-- SAMPLE DATA: Admin User
-- ============================================================================
INSERT INTO users (username, password, role, full_name, email) VALUES
('admin', 'admin123', 'Admin', 'System Administrator', 'admin@college.edu');

-- ============================================================================
-- SAMPLE DATA: Faculty
-- ============================================================================
INSERT INTO faculty (faculty_id, name, department, email, phone, password, status) VALUES
('FAC001', 'Dr. Priya Sharma', 'CSE', 'priya.sharma@college.edu', '9876543210', 'faculty123', 'Active'),
('FAC002', 'Prof. Rajesh Kumar', 'CSE', 'rajesh.kumar@college.edu', '9876543211', 'faculty123', 'Active'),
('FAC003', 'Dr. Anita Singh', 'ECE', 'anita.singh@college.edu', '9876543212', 'faculty123', 'Active'),
('FAC004', 'Prof. Vikram Rao', 'EEE', 'vikram.rao@college.edu', '9876543213', 'faculty123', 'Active'),
('FAC005', 'Dr. Meena Verma', 'MECH', 'meena.verma@college.edu', '9876543214', 'faculty123', 'Active');

-- ============================================================================
-- SAMPLE DATA: Students
-- ============================================================================
INSERT INTO students (roll_number, name, email, phone, branch, year, section, password, status) VALUES
('21CSE001', 'E.N.RACHANA', 'rachana@student.edu', '9876501001', 'CSE', 3, 'A', 'student123', 'Active'),
('21CSE002', 'B.PRIYADARSHINI', 'priya@student.edu', '9876501002', 'CSE', 3, 'A', 'student123', 'Active'),
('21CSE003', 'SHAIK SOHAIL BASHA', 'sohail@student.edu', '9876501003', 'CSE', 3, 'A', 'student123', 'Active'),
('21CSE004', 'K.TARUN', 'tarun@student.edu', '9876501004', 'CSE', 3, 'A', 'student123', 'Active'),
('21CSE005', 'M.NARAYANA SWAMY', 'narayana@student.edu', '9876501005', 'CSE', 3, 'A', 'student123', 'Active'),
('21CSE006', 'D.PAVAN KALYAN', 'pavan@student.edu', '9876501006', 'CSE', 3, 'A', 'student123', 'Active'),
('21CSE007', 'S.BHAVANI', 'bhavani@student.edu', '9876501007', 'CSE', 3, 'A', 'student123', 'Active'),
('21CSE016', 'B.Abhirudh', 'abhirudh@student.edu', '9876501016', 'CSE', 3, 'A', 'student123', 'Active'),
('21CSE010', 'D.Krishna', 'krishna@student.edu', '9876501010', 'CSE', 3, 'A', 'student123', 'Active'),
('21CSE011', 'D.Nandini', 'nandini@student.edu', '9876501011', 'CSE', 3, 'A', 'student123', 'Active'),
('21ECE001', 'Rahul Sharma', 'rahul@student.edu', '9876502001', 'ECE', 2, 'B', 'student123', 'Active'),
('21EEE002', 'Priya Gupta', 'priyag@student.edu', '9876503001', 'EEE', 2, 'A', 'student123', 'Active');

-- ============================================================================
-- SAMPLE DATA: Subjects (Year 1-4, All Branches)
-- ============================================================================
INSERT INTO subjects (subject_code, subject_name, branch, year, semester, credits, max_marks, pass_marks) VALUES
-- Year 1 Subjects
('CS101', 'Programming in C', 'CSE', 1, 1, 4, 100, 40),
('CS102', 'Engineering Mathematics-I', 'CSE', 1, 1, 4, 100, 40),
('CS103', 'Engineering Physics', 'CSE', 1, 1, 3, 100, 40),
('CS104', 'Data Structures', 'CSE', 1, 2, 4, 100, 40),

-- Year 2 Subjects
('CS201', 'Java Programming', 'CSE', 2, 3, 4, 100, 40),
('CS202', 'Database Management Systems', 'CSE', 2, 3, 4, 100, 40),
('CS203', 'Computer Organization', 'CSE', 2, 4, 4, 100, 40),
('CS204', 'Operating Systems', 'CSE', 2, 4, 4, 100, 40),

-- Year 3 Subjects
('CS301', 'Software Engineering', 'CSE', 3, 5, 4, 100, 40),
('CS302', 'Computer Networks', 'CSE', 3, 5, 4, 100, 40),
('CS303', 'Web Technologies', 'CSE', 3, 6, 4, 100, 40),
('CS304', 'Machine Learning', 'CSE', 3, 6, 4, 100, 40),

-- Year 4 Subjects
('CS401', 'Artificial Intelligence', 'CSE', 4, 7, 4, 100, 40),
('CS402', 'Cloud Computing', 'CSE', 4, 7, 4, 100, 40),
('CS403', 'Blockchain Technology', 'CSE', 4, 8, 4, 100, 40);

-- ============================================================================
-- SAMPLE DATA: Faculty-Subject Assignments
-- ============================================================================
INSERT INTO faculty_subjects (faculty_id, subject_code) VALUES
('FAC001', 'CS301'),
('FAC001', 'CS302'),
('FAC001', 'CS303'),
('FAC002', 'CS201'),
('FAC002', 'CS202'),
('FAC002', 'CS304');

-- ============================================================================
-- SAMPLE DATA: Student Enrollments (Auto-enroll by year)
-- ============================================================================

-- Year 3 CSE Students in Year 3 Subjects
INSERT INTO student_enrollments (roll_number, subject_code, year, semester, status) 
SELECT s.roll_number, sub.subject_code, 3, sub.semester, 'Active'
FROM students s
CROSS JOIN subjects sub
WHERE s.branch = 'CSE' AND s.year = 3 AND sub.year = 3;

-- Year 2 Students in Year 2 Subjects
INSERT INTO student_enrollments (roll_number, subject_code, year, semester, status) 
SELECT s.roll_number, sub.subject_code, 2, sub.semester, 'Active'
FROM students s
CROSS JOIN subjects sub
WHERE s.year = 2 AND sub.year = 2
ON DUPLICATE KEY UPDATE status = 'Active';

-- ============================================================================
-- SAMPLE DATA: Marks (with auto-calculation via triggers)
-- ============================================================================
INSERT INTO marks (roll_number, subject_code, internal_marks, external_marks, exam_date) VALUES
-- Student: 21CSE016 (Top performer)
('21CSE016', 'CS301', 28, 67, '2024-12-10'),
('21CSE016', 'CS302', 29, 68, '2024-12-12'),
('21CSE016', 'CS303', 27, 66, '2024-12-14'),

-- Student: 21CSE001
('21CSE001', 'CS301', 25, 55, '2024-12-10'),
('21CSE001', 'CS302', 24, 56, '2024-12-12'),

-- Student: 21CSE002
('21CSE002', 'CS301', 20, 50, '2024-12-10'),
('21CSE002', 'CS302', 22, 48, '2024-12-12'),

-- Student: 21CSE003
('21CSE003', 'CS301', 18, 45, '2024-12-10'),

-- Student: 21CSE006
('21CSE006', 'CS301', 23, 56, '2024-12-10'),

-- Student with failing marks (will create backlog)
('21CSE002', 'CS303', 10, 15, '2024-12-14');

-- ============================================================================
-- SAMPLE DATA: Attendance
-- ============================================================================
INSERT INTO attendance (roll_number, subject_code, total_classes, classes_attended) VALUES
('21CSE001', 'CS301', 60, 50),
('21CSE001', 'CS302', 60, 48),
('21CSE002', 'CS301', 60, 55),
('21CSE002', 'CS302', 60, 42),
('21CSE003', 'CS301', 60, 52),
('21CSE006', 'CS301', 60, 58),
('21CSE016', 'CS301', 60, 60),
('21CSE016', 'CS302', 60, 59),
('21CSE016', 'CS303', 60, 58);

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Verify student count
SELECT 'Students' as Entity, COUNT(*) as Count FROM students
UNION ALL
SELECT 'Faculty', COUNT(*) FROM faculty
UNION ALL
SELECT 'Subjects', COUNT(*) FROM subjects
UNION ALL
SELECT 'Enrollments', COUNT(*) FROM student_enrollments
UNION ALL
SELECT 'Marks', COUNT(*) FROM marks
UNION ALL
SELECT 'Attendance', COUNT(*) FROM attendance;

-- Verify grade points are calculated
SELECT 
    roll_number,
    subject_code,
    internal_marks,
    external_marks,
    total_marks,
    grade,
    grade_point,
    status
FROM marks
ORDER BY grade_point DESC
LIMIT 10;

-- Verify CGPA calculation
SELECT 
    roll_number,
    SUM(grade_point * credits) / SUM(credits) as cgpa
FROM marks m
INNER JOIN subjects s ON m.subject_code = s.subject_code
WHERE m.status = 'Pass'
GROUP BY roll_number
ORDER BY cgpa DESC;

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================

-- Display success message
SELECT '✅ Database schema created successfully!' as Status,
       'Username: admin / Password: admin123' as Admin,
       'Faculty: FAC001-FAC005 / Password: faculty123' as Faculty,
       'Students: Roll numbers / Password: student123' as Students;
