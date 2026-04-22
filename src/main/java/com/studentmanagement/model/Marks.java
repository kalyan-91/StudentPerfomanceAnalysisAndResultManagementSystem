package com.studentmanagement.model;

/**
 * Marks Entity — University Grading System (10-point scale)
 * S(≥95,10) A1(90-94,9.5) A2(85-89,9.0) A3(80-84,8.5)
 * B1(75-79,8.0) B2(70-74,7.5) C1(65-69,7.0) C2(60-64,6.5)
 * D1(55-59,6.0) D2(50-54,5.5) F(<50,0)
 */
public class Marks {

    private String rollNumber;
    private String subjectCode;
    private String subjectName;   // from JOIN
    private int    semester;      // from JOIN
    private int    credits;       // from JOIN
    private int    internalMarks;
    private int    externalMarks;
    private int    totalMarks;
    private String grade;
    private double gradePoint;
    private String status;

    public Marks() {}

    public Marks(String rollNumber, String subjectCode,
                 int internalMarks, int externalMarks) {
        this.rollNumber    = rollNumber;
        this.subjectCode   = subjectCode;
        this.internalMarks = internalMarks;
        this.externalMarks = externalMarks;
        this.totalMarks    = calculateTotal();
    }

    public int calculateTotal() {
        return internalMarks + externalMarks;
    }

    /** University letter grade from total out of 100 */
    public String calculateGrade(int total) {
        if (total >= 95) return "S";
        if (total >= 90) return "A1";
        if (total >= 85) return "A2";
        if (total >= 80) return "A3";
        if (total >= 75) return "B1";
        if (total >= 70) return "B2";
        if (total >= 65) return "C1";
        if (total >= 60) return "C2";
        if (total >= 55) return "D1";
        if (total >= 50) return "D2";
        return "F";
    }

    /** Grade point on 10-point scale */
    public double calculateGradePoint(int total) {
        if (total >= 95) return 10.0;
        if (total >= 90) return 9.5;
        if (total >= 85) return 9.0;
        if (total >= 80) return 8.5;
        if (total >= 75) return 8.0;
        if (total >= 70) return 7.5;
        if (total >= 65) return 7.0;
        if (total >= 60) return 6.5;
        if (total >= 55) return 6.0;
        if (total >= 50) return 5.5;
        return 0.0;
    }

    public String determineStatus(int total, int passMarks) {
        return total >= passMarks ? "Pass" : "Fail";
    }

    public void computeResult(int passMarks) {
    this.totalMarks = calculateTotal();
    this.grade      = calculateGrade(this.totalMarks);
    this.gradePoint = calculateGradePoint(this.totalMarks);
    this.status     = determineStatus(this.totalMarks, passMarks);
    
    // Debug output
    System.out.println("DEBUG computeResult: total=" + totalMarks + 
                      ", grade=" + grade + ", gradePoint=" + gradePoint + 
                      ", status=" + status);
}

    public boolean validateMarks(int maxInternal, int maxExternal) {
        return internalMarks >= 0 && internalMarks <= maxInternal &&
               externalMarks >= 0 && externalMarks <= maxExternal;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public String getRollNumber()         { return rollNumber; }
    public void   setRollNumber(String v) { this.rollNumber = v; }
    public String getSubjectCode()         { return subjectCode; }
    public void   setSubjectCode(String v) { this.subjectCode = v; }
    public String getSubjectName()         { return subjectName != null ? subjectName : ""; }
    public void   setSubjectName(String v) { this.subjectName = v; }
    public int    getSemester()            { return semester; }
    public void   setSemester(int v)       { this.semester = v; }
    public int    getCredits()             { return credits > 0 ? credits : 4; }
    public void   setCredits(int v)        { this.credits = v; }
    public int    getInternalMarks()       { return internalMarks; }
    public void   setInternalMarks(int v)  { this.internalMarks = v; this.totalMarks = calculateTotal(); }
    public int    getExternalMarks()       { return externalMarks; }
    public void   setExternalMarks(int v)  { this.externalMarks = v; this.totalMarks = calculateTotal(); }
    public int    getTotalMarks()          { return totalMarks; }
    public void   setTotalMarks(int v)     { this.totalMarks = v; }
    public String getGrade()              { return grade != null ? grade : "-"; }
    public void   setGrade(String v)      { this.grade = v; }
    public double getGradePoint()         { return gradePoint; }
    public void   setGradePoint(double v) { this.gradePoint = v; }
    public String getStatus()             { return status != null ? status : "-"; }
    public void   setStatus(String v)     { this.status = v; }

    @Override
    public String toString() {
        return "Marks{roll=" + rollNumber + ", subject=" + subjectCode +
               ", total=" + totalMarks + ", grade=" + grade +
               ", gp=" + gradePoint + ", status=" + status + "}";
    }
}