package com.studentmanagement.gui.panels;

import com.studentmanagement.dao.*;
import com.studentmanagement.model.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Marks Management Panel — Ocean Theme
 * Manual entry form + Smart Import (Excel / Photo OCR)
 */
public class MarksManagementPanel extends JPanel {

    private StudentDAO studentDAO = new StudentDAO();
    private SubjectDAO subjectDAO = new SubjectDAO();
    private MarksDAO   marksDAO   = new MarksDAO();
    private FacultyDAO facultyDAO = new FacultyDAO();
    private BacklogDAO backlogDAO = new BacklogDAO();

    private Faculty currentFaculty;

    private JTable            marksTable;
    private DefaultTableModel tableModel;

    private JComboBox<String> yearFilterCombo;
    private JComboBox<String> semesterFilterCombo;

    private JComboBox<String> rollNumberCombo;
    private JComboBox<String> subjectCodeCombo;
    private JTextField        internalMarksField;
    private JTextField        externalMarksField;
    private JLabel            totalMarksLabel;
    private JLabel            gradeLabel;
    private JLabel            statusLabel;

    // Ocean theme
    private static final Color BG       = new Color(10,  25,  47);
    private static final Color CARD     = new Color(13,  31,  61);
    private static final Color HDR      = new Color(0,  105, 148);
    private static final Color ACCENT   = new Color(0,  191, 255);
    private static final Color GREEN    = new Color(0,  200, 120);
    private static final Color RED      = new Color(231,  76,  60);
    private static final Color YELLOW   = new Color(241, 196,  15);
    private static final Color ORANGE   = new Color(230, 126,  34);
    private static final Color TEXT     = Color.WHITE;
    private static final Color DIM      = new Color(160, 200, 220);
    private static final Color FIELD_BG = new Color(5,   30,  65);

    public MarksManagementPanel()            { this(null); }
    public MarksManagementPanel(Faculty fac) {
        this.currentFaculty = fac;
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(buildTopPanel(),   BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFormPanel(),  BorderLayout.EAST);
        loadMarks();
    }

    // ── Top panel ─────────────────────────────────────────────────────────────
    private JPanel buildTopPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JLabel title = new JLabel("Marks Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(ACCENT);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(BG);

        right.add(dimLbl("Year:"));
        yearFilterCombo = dCombo(new String[]{"All","1","2","3","4"});
        yearFilterCombo.addActionListener(e -> { updateSemesterOptions(); loadMarks(); });
        right.add(yearFilterCombo);

        right.add(Box.createHorizontalStrut(10));
        right.add(dimLbl("Semester:"));
        semesterFilterCombo = dCombo(new String[]{"All","1","2","3","4","5","6","7","8"});
        semesterFilterCombo.addActionListener(e -> loadMarks());
        right.add(semesterFilterCombo);

        right.add(Box.createHorizontalStrut(14));

        JButton importBtn = mkBtn("Smart Import", new Color(0, 140, 70), e -> openSmartImport());
        importBtn.setPreferredSize(new Dimension(140, 36));
        right.add(importBtn);

        JButton refreshBtn = mkBtn("Refresh", HDR, e -> loadMarks());
        refreshBtn.setPreferredSize(new Dimension(100, 36));
        right.add(refreshBtn);

        p.add(title, BorderLayout.WEST);
        p.add(right,  BorderLayout.EAST);
        return p;
    }

    private void updateSemesterOptions() {
        String yr = (String) yearFilterCombo.getSelectedItem();
        semesterFilterCombo.removeAllItems();
        semesterFilterCombo.addItem("All");
        if ("All".equals(yr)) {
            for (int i = 1; i <= 8; i++) semesterFilterCombo.addItem(String.valueOf(i));
        } else {
            int y = Integer.parseInt(yr), s = (y-1)*2+1;
            semesterFilterCombo.addItem(String.valueOf(s));
            semesterFilterCombo.addItem(String.valueOf(s+1));
        }
    }

    // ── Table panel ───────────────────────────────────────────────────────────
    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);

        String[] cols = {"Roll No","Student Name","Subject Code","Subject Name",
                         "Semester","Internal","External","Total","Grade","GP","Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        marksTable = new JTable(tableModel);
        marksTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        marksTable.setRowHeight(30);
        marksTable.setBackground(CARD);
        marksTable.setForeground(TEXT);
        marksTable.setSelectionBackground(HDR);
        marksTable.setGridColor(new Color(20, 50, 80));
        marksTable.setShowGrid(true);
        marksTable.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader th = marksTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));
        th.setBackground(new Color(0, 70, 110));
        th.setForeground(TEXT);
        th.setPreferredSize(new Dimension(0, 34));

        marksTable.getColumnModel().getColumn(10).setCellRenderer(new StatusRenderer());
        marksTable.getColumnModel().getColumn(8).setCellRenderer(new GradeRenderer());

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        center.setBackground(CARD);
        center.setForeground(TEXT);
        for (int i : new int[]{0,2,4,5,6,7,9})
            marksTable.getColumnModel().getColumn(i).setCellRenderer(center);

        marksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromTable();
        });

        JScrollPane sp = new JScrollPane(marksTable);
        sp.setBackground(CARD);
        sp.getViewport().setBackground(CARD);
        sp.setBorder(BorderFactory.createLineBorder(HDR, 1));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ── Form panel ────────────────────────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(HDR); g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel formTitle = new JLabel("Enter Marks");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(ACCENT);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(formTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 18)));

        panel.add(fLbl("Roll Number:"));
        rollNumberCombo = new JComboBox<>();
        sCombo(rollNumberCombo);
        panel.add(rollNumberCombo);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));

        panel.add(fLbl("Subject:"));
        subjectCodeCombo = new JComboBox<>();
        sCombo(subjectCodeCombo);
        subjectCodeCombo.addActionListener(e -> calculatePreview());
        panel.add(subjectCodeCombo);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));

        panel.add(fLbl("Internal Marks (Max 30):"));
        internalMarksField = dField();
        internalMarksField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { calculatePreview(); }
        });
        panel.add(internalMarksField);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));

        panel.add(fLbl("External Marks (Max 70):"));
        externalMarksField = dField();
        externalMarksField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { calculatePreview(); }
        });
        panel.add(externalMarksField);
        panel.add(Box.createRigidArea(new Dimension(0, 16)));

        panel.add(buildPreviewCard());
        panel.add(Box.createRigidArea(new Dimension(0, 18)));

        panel.add(fmBtn("Save Marks", GREEN, e -> saveMarks()));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(fmBtn("Clear Form", new Color(60, 80, 100), e -> clearForm()));
        panel.add(Box.createVerticalGlue());

        loadStudentsToCombo();
        loadSubjectsToCombo();
        return panel;
    }

    private JPanel buildPreviewCard() {
        JPanel p = new JPanel(new GridLayout(3, 2, 8, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(5, 20, 45));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(0, 120, 180));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 102));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalMarksLabel = new JLabel("0");
        totalMarksLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalMarksLabel.setForeground(TEXT);

        gradeLabel = new JLabel("-");
        gradeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gradeLabel.setForeground(ACCENT);

        statusLabel = new JLabel("-");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(DIM);

        p.add(fLbl("Total:")); p.add(totalMarksLabel);
        p.add(fLbl("Grade:")); p.add(gradeLabel);
        p.add(fLbl("Status:")); p.add(statusLabel);
        return p;
    }

    // ── Data methods ──────────────────────────────────────────────────────────
    private void loadStudentsToCombo() {
        rollNumberCombo.removeAllItems();
        List<Student> students = currentFaculty != null ?
            facultyDAO.getStudentsForFaculty(currentFaculty.getFacultyId()) :
            studentDAO.getAllStudents();
        for (Student s : students)
            rollNumberCombo.addItem(s.getRollNumber() + " - " + s.getName());
    }

    private void loadSubjectsToCombo() {
        subjectCodeCombo.removeAllItems();
        List<Subject> subjects = subjectDAO.getAllSubjects();
        if (currentFaculty != null) {
            List<String> codes = facultyDAO.getFacultySubjects(currentFaculty.getFacultyId());
            for (Subject s : subjects)
                if (codes.contains(s.getSubjectCode()))
                    subjectCodeCombo.addItem(s.getSubjectCode()+" - "+s.getSubjectName()+" (Sem "+s.getSemester()+")");
        } else {
            for (Subject s : subjects)
                subjectCodeCombo.addItem(s.getSubjectCode()+" - "+s.getSubjectName()+" (Sem "+s.getSemester()+")");
        }
    }

    private void loadMarks() {
        tableModel.setRowCount(0);
        String yf = (String) yearFilterCombo.getSelectedItem();
        String sf = (String) semesterFilterCombo.getSelectedItem();

        List<Marks> list;
        if (currentFaculty != null) {
            list = new java.util.ArrayList<>();
            for (String sc : facultyDAO.getFacultySubjects(currentFaculty.getFacultyId()))
                list.addAll(marksDAO.getMarksBySubject(sc));
        } else {
            list = marksDAO.getAllMarks();
        }

        for (Marks m : list) {
            if (!"All".equals(yf)) {
                Subject sub = subjectDAO.getSubjectByCode(m.getSubjectCode());
                if (sub != null && sub.getYear() != Integer.parseInt(yf)) continue;
            }
            if (!"All".equals(sf) && m.getSemester() != Integer.parseInt(sf)) continue;
            Student st = studentDAO.getStudentByRollNumber(m.getRollNumber());
            tableModel.addRow(new Object[]{
                m.getRollNumber(), st!=null?st.getName():"-",
                m.getSubjectCode(), m.getSubjectName(), m.getSemester(),
                m.getInternalMarks(), m.getExternalMarks(), m.getTotalMarks(),
                m.getGrade(), String.format("%.1f", m.getGradePoint()), m.getStatus()
            });
        }
    }

    private void populateFormFromTable() {
        int row = marksTable.getSelectedRow();
        if (row < 0) return;
        String roll = tableModel.getValueAt(row, 0).toString();
        String code = tableModel.getValueAt(row, 2).toString();
        for (int i=0; i<rollNumberCombo.getItemCount(); i++)
            if (rollNumberCombo.getItemAt(i).startsWith(roll)) { rollNumberCombo.setSelectedIndex(i); break; }
        for (int i=0; i<subjectCodeCombo.getItemCount(); i++)
            if (subjectCodeCombo.getItemAt(i).startsWith(code)) { subjectCodeCombo.setSelectedIndex(i); break; }
        internalMarksField.setText(tableModel.getValueAt(row, 5).toString());
        externalMarksField.setText(tableModel.getValueAt(row, 6).toString());
        calculatePreview();
    }

    private void calculatePreview() {
        try {
            String it = internalMarksField.getText().trim();
            String et = externalMarksField.getText().trim();
            if (it.isEmpty() || et.isEmpty()) {
                totalMarksLabel.setText("0"); gradeLabel.setText("-"); statusLabel.setText("-"); return;
            }
            Marks m = new Marks();
            m.setInternalMarks(Integer.parseInt(it));
            m.setExternalMarks(Integer.parseInt(et));
            m.computeResult(40);
            totalMarksLabel.setText(String.valueOf(m.getTotalMarks()));
            gradeLabel.setText(m.getGrade());
            gradeLabel.setForeground(gradeColor(m.getGrade()));
            statusLabel.setText(m.getStatus());
            statusLabel.setForeground("Pass".equals(m.getStatus()) ? GREEN : RED);
        } catch (NumberFormatException ex) {
            totalMarksLabel.setText("0"); gradeLabel.setText("-"); statusLabel.setText("-");
        }
    }

    private Color gradeColor(String g) {
        if (g == null) return TEXT;
        switch (g) {
            case "S": case "A1": case "A2": return GREEN;
            case "A3": case "B1": case "B2": return ACCENT;
            case "C1": case "C2": return YELLOW;
            case "D1": case "D2": return ORANGE;
            case "F":  return RED;
            default:   return TEXT;
        }
    }

    private void saveMarks() {
        try {
            if (rollNumberCombo.getSelectedItem()==null || subjectCodeCombo.getSelectedItem()==null) {
                showErr("Please select student and subject!"); return;
            }
            String roll = rollNumberCombo.getSelectedItem().toString().split(" - ")[0];
            String code = subjectCodeCombo.getSelectedItem().toString().split(" - ")[0];
            int intM = Integer.parseInt(internalMarksField.getText().trim());
            int extM = Integer.parseInt(externalMarksField.getText().trim());
            if (intM<0||intM>30) { showErr("Internal marks must be 0-30!"); return; }
            if (extM<0||extM>70) { showErr("External marks must be 0-70!"); return; }
            Marks marks = new Marks();
            marks.setRollNumber(roll); marks.setSubjectCode(code);
            marks.setInternalMarks(intM); marks.setExternalMarks(extM);
            marks.computeResult(40);
            boolean ok = marksDAO.getMarks(roll,code)!=null ?
                         marksDAO.updateMarks(marks) : marksDAO.addMarks(marks);
            if (ok) {
                backlogDAO.detectBacklogForStudent(roll, code);
                backlogDAO.clearBacklogIfPassed(roll, code);
                JOptionPane.showMessageDialog(this,
                    "Marks saved!\n\nTotal: "+marks.getTotalMarks()+"/100\n"
                    +"Grade: "+marks.getGrade()+" (GP: "+marks.getGradePoint()+")\n"
                    +"Status: "+marks.getStatus());
                loadMarks(); clearForm();
            } else { showErr("Failed to save marks!"); }
        } catch (NumberFormatException ex) { showErr("Enter valid numbers for marks!"); }
        catch (Exception ex) { showErr("Error: "+ex.getMessage()); }
    }

    private void clearForm() {
        internalMarksField.setText(""); externalMarksField.setText("");
        totalMarksLabel.setText("0"); gradeLabel.setText("-"); statusLabel.setText("-");
        marksTable.clearSelection();
    }

    private void openSmartImport() {
        JDialog d = new JDialog(
            (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
            "Smart Marks Import", true);
        d.setSize(920, 720);
        d.setLocationRelativeTo(this);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.setContentPane(new SmartMarksImportPanel(currentFaculty));
        d.setVisible(true);
        loadMarks();
    }

    // ── Renderers ─────────────────────────────────────────────────────────────
    class StatusRenderer extends DefaultTableCellRenderer {
        StatusRenderer() { setHorizontalAlignment(CENTER); }
        public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c) {
            super.getTableCellRendererComponent(t,v,sel,foc,r,c);
            setBackground(sel?HDR:CARD); setForeground("Pass".equals(v)?GREEN:RED);
            setFont(getFont().deriveFont(Font.BOLD)); return this;
        }
    }
    class GradeRenderer extends DefaultTableCellRenderer {
        GradeRenderer() { setHorizontalAlignment(CENTER); }
        public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c) {
            super.getTableCellRendererComponent(t,v,sel,foc,r,c);
            setBackground(sel?HDR:CARD); setForeground(gradeColor(v!=null?v.toString():null));
            setFont(getFont().deriveFont(Font.BOLD)); return this;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JLabel dimLbl(String t) { JLabel l=new JLabel(t); l.setFont(new Font("Segoe UI",Font.PLAIN,13)); l.setForeground(DIM); return l; }
    private JLabel fLbl(String t)   { JLabel l=dimLbl(t); l.setAlignmentX(Component.LEFT_ALIGNMENT); return l; }
    private JComboBox<String> dCombo(String[] items) {
        JComboBox<String> cb=new JComboBox<>(items);
        cb.setFont(new Font("Segoe UI",Font.PLAIN,13)); cb.setBackground(FIELD_BG); cb.setForeground(TEXT); return cb;
    }
    private void sCombo(JComboBox<String> cb) {
        cb.setFont(new Font("Segoe UI",Font.PLAIN,13)); cb.setBackground(FIELD_BG); cb.setForeground(TEXT);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); cb.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    private JTextField dField() {
        JTextField f=new JTextField();
        f.setFont(new Font("Segoe UI",Font.PLAIN,14)); f.setBackground(FIELD_BG); f.setForeground(TEXT); f.setCaretColor(TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0,120,180),1),BorderFactory.createEmptyBorder(5,8,5,8)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); f.setAlignmentX(Component.LEFT_ALIGNMENT); return f;
    }
    private JButton mkBtn(String text, Color bg, java.awt.event.ActionListener a) {
        JButton b=new JButton(text){
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()?bg.darker():getModel().isRollover()?bg.brighter():bg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(Color.WHITE); g2.setFont(getFont());
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setFont(new Font("Segoe UI",Font.BOLD,13));
        b.setOpaque(false); b.setContentAreaFilled(false); b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); if(a!=null) b.addActionListener(a); return b;
    }
    private JButton fmBtn(String t,Color bg,java.awt.event.ActionListener a){
        JButton b=mkBtn(t,bg,a); b.setMaximumSize(new Dimension(Integer.MAX_VALUE,40)); b.setAlignmentX(Component.LEFT_ALIGNMENT); return b;
    }
    private void showErr(String m){JOptionPane.showMessageDialog(this,m,"Error",JOptionPane.ERROR_MESSAGE);}
}