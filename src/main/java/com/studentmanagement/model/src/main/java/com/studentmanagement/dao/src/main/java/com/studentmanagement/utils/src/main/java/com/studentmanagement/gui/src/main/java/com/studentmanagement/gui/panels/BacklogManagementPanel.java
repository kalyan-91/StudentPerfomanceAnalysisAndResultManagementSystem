 package com.studentmanagement.gui.panels;

import com.studentmanagement.dao.BacklogDAO;
import com.studentmanagement.model.Backlog;
import com.studentmanagement.model.Student;
import com.studentmanagement.gui.MainDashboard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BacklogManagementPanel extends JPanel {

    private BacklogDAO backlogDAO = new BacklogDAO();
    private JTable backlogTable;
    private DefaultTableModel tableModel;
    private JTextField rollNumberField, subjectCodeField;

    // null = Admin mode, non-null = Student view (read-only)
    private Student currentStudent;

    // ── Ocean #006994 palette ─────────────────────────────────────────────────
    private static final Color CONTENT_BG   = new Color(0,   42,  62);
    private static final Color FORM_BG      = new Color(0,   28,  42);
    private static final Color FIELD_BG     = new Color(0,   35,  52);
    private static final Color FIELD_BORDER = new Color(0,  148, 180, 120);
    private static final Color TEXT_PRIMARY = new Color(220, 245, 255);
    private static final Color TEXT_MUTED   = new Color(100, 175, 195);
    private static final Color ACCENT_AQUA  = new Color(0,  185, 210);
    private static final Color DIVIDER      = new Color(0,   90, 130,  60);
    private static final Color BTN_GREEN    = new Color(30,  140, 100);
    private static final Color BTN_BLUE     = new Color(0,   65, 105);
    private static final Color BTN_RED      = new Color(160,  40,  55);
    private static final Color BTN_GREY     = new Color(60,   90, 130);

    // ── Admin constructor ─────────────────────────────────────────────────────
    public BacklogManagementPanel() {
        this(null);
    }

    // ── Student / Faculty constructor ─────────────────────────────────────────
    public BacklogManagementPanel(Student student) {
        this.currentStudent = student;

        setLayout(new BorderLayout(10, 10));
        setBackground(CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createTopPanel(),   BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        // Wrap form in scroll pane so nothing is cut off
        JPanel formContent = createFormPanel();
        JScrollPane formScroll = new JScrollPane(formContent,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.setPreferredSize(new Dimension(300, 0));
        formScroll.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, DIVIDER));
        formScroll.setBackground(FORM_BG);
        formScroll.getViewport().setBackground(FORM_BG);
        formScroll.getVerticalScrollBar().setBackground(FORM_BG);
        formScroll.getVerticalScrollBar().setUI(new OceanScrollBarUI());
        add(formScroll, BorderLayout.EAST);

        // ── KEY FIX: if student logged in, pre-fill and auto-load immediately ─
        if (currentStudent != null) {
            rollNumberField.setText(currentStudent.getRollNumber());
            rollNumberField.setEditable(false);
            loadBacklogsForStudent(currentStudent.getRollNumber());
        }
    }

    // ── Top panel ─────────────────────────────────────────────────────────────
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Backlog Management");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 22));
        titleLabel.setForeground(ACCENT_AQUA);

        // Admin-only buttons in top bar
        if (currentStudent == null) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttonPanel.setBackground(CONTENT_BG);

            JButton detectButton = mkBtn("Auto-Detect Backlogs", BTN_RED);
            detectButton.addActionListener(e -> detectBacklogs());

            JButton refreshButton = mkBtn("Refresh", BTN_GREEN);
            refreshButton.addActionListener(e -> {
                String roll = rollNumberField.getText().trim();
                if (!roll.isEmpty()) loadBacklogsForStudent(roll);
                else tableModel.setRowCount(0);
            });

            buttonPanel.add(detectButton);
            buttonPanel.add(refreshButton);
            panel.add(buttonPanel, BorderLayout.EAST);
        } else {
            // Student gets a simple refresh
            JButton refreshButton = mkBtn("Refresh", BTN_BLUE);
            refreshButton.addActionListener(
                e -> loadBacklogsForStudent(currentStudent.getRollNumber()));
            panel.add(refreshButton, BorderLayout.EAST);
        }

        panel.add(titleLabel, BorderLayout.WEST);
        return panel;
    }

    // ── Table panel ───────────────────────────────────────────────────────────
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);

        String[] columns = {"ID", "Roll No", "Subject Code",
                            "Subject Name", "Semester", "Reason",
                            "Status", "Cleared Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        backlogTable = new JTable(tableModel);
        backlogTable.setFont(new Font("Verdana", Font.PLAIN, 12));
        backlogTable.setRowHeight(28);
        backlogTable.setBackground(new Color(0, 18, 30));
        backlogTable.setForeground(TEXT_PRIMARY);
        backlogTable.setSelectionBackground(new Color(0, 55, 100));
        backlogTable.setSelectionForeground(ACCENT_AQUA);
        backlogTable.setGridColor(DIVIDER);
        backlogTable.setShowHorizontalLines(true);
        backlogTable.setShowVerticalLines(false);

        backlogTable.getTableHeader().setFont(new Font("Verdana", Font.BOLD, 12));
        backlogTable.getTableHeader().setBackground(new Color(0, 45, 70));
        backlogTable.getTableHeader().setForeground(ACCENT_AQUA);
        backlogTable.getTableHeader().setBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_AQUA));

        backlogTable.setDefaultRenderer(Object.class,
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v,
                        boolean sel, boolean focus, int row, int col) {
                    super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                    if (sel) {
                        setBackground(new Color(0, 55, 100));
                        setForeground(ACCENT_AQUA);
                    } else {
                        setBackground(row % 2 == 0
                            ? new Color(0, 18, 30) : new Color(0, 28, 45));
                        // Colour-code status column
                        if (col == 6 && v != null) {
                            String status = v.toString().toLowerCase();
                            if (status.contains("clear"))
                                setForeground(new Color(50, 230, 180));
                            else
                                setForeground(new Color(255, 130, 100));
                        } else {
                            setForeground(TEXT_PRIMARY);
                        }
                    }
                    setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                    return this;
                }
            });

        backlogTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromTable();
        });

        JScrollPane sp = new JScrollPane(backlogTable);
        sp.setBackground(new Color(0, 18, 30));
        sp.getViewport().setBackground(new Color(0, 18, 30));
        sp.setBorder(BorderFactory.createLineBorder(new Color(0, 185, 210, 70), 1));
        sp.getVerticalScrollBar()  .setUI(new OceanScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new OceanScrollBarUI());
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // ── Form / side panel ─────────────────────────────────────────────────────
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(FORM_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel formTitle = new JLabel(
            currentStudent != null ? "Your Backlogs" : "Backlog Details");
        formTitle.setFont(new Font("Georgia", Font.BOLD, 17));
        formTitle.setForeground(ACCENT_AQUA);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(formTitle);
        gap(panel, 18);

        rollNumberField  = field(panel, "Roll Number:");
        subjectCodeField = field(panel, "Subject Code:");
        gap(panel, 20);

        if (currentStudent == null) {
            // Admin buttons
            JButton viewBtn   = mkBtn("View Student Backlogs", BTN_BLUE);
            JButton clearBtn  = mkBtn("Mark as Cleared",       BTN_GREEN);
            JButton deleteBtn = mkBtn("Delete Backlog",        BTN_RED);
            JButton resetBtn  = mkBtn("Clear Form",            BTN_GREY);

            viewBtn  .addActionListener(e -> viewStudentBacklogs());
            clearBtn .addActionListener(e -> clearBacklog());
            deleteBtn.addActionListener(e -> deleteBacklog());
            resetBtn .addActionListener(e -> clearForm());

            for (JButton b : new JButton[]{viewBtn, clearBtn, deleteBtn, resetBtn}) {
                b.setAlignmentX(Component.LEFT_ALIGNMENT);
                b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
                panel.add(b);
                gap(panel, 8);
            }
        } else {
            // Student: read-only notice
            JLabel note = new JLabel(
                "<html><body style='width:220px;color:#64AFBF'>"
                + "Select a row in the table to see details. "
                + "Contact your faculty to resolve backlogs.</body></html>");
            note.setFont(new Font("Verdana", Font.PLAIN, 11));
            note.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(note);
        }

        return panel;
    }

    // ── Data helpers ──────────────────────────────────────────────────────────
    private void loadBacklogsForStudent(String rollNumber) {
        tableModel.setRowCount(0);
        List<Backlog> backlogs = backlogDAO.getBacklogsByStudent(rollNumber);
        for (Backlog b : backlogs) {
            tableModel.addRow(new Object[]{
                b.getBacklogId(),   b.getRollNumber(),
                b.getSubjectCode(), b.getSubjectName(),
                b.getSemester(),    b.getReason(),
                b.getClearanceStatus(), b.getClearanceDate()
            });
        }
        if (backlogs.isEmpty() && currentStudent != null) {
            // Show a friendly empty-state message in the table area
            // (don't pop a dialog for student — they might just have no backlogs)
            tableModel.addRow(new Object[]{
                "", "", "", "✔  No backlogs recorded", "", "", "", ""});
        }
    }

    private void viewStudentBacklogs() {
        String rollNumber = rollNumberField.getText().trim();
        if (rollNumber.isEmpty()) {
            MainDashboard.OceanDialog.showError(this, "Error",
                "Please enter a roll number!");
            return;
        }
        tableModel.setRowCount(0);
        List<Backlog> backlogs = backlogDAO.getBacklogsByStudent(rollNumber);
        for (Backlog b : backlogs) {
            tableModel.addRow(new Object[]{
                b.getBacklogId(),   b.getRollNumber(),
                b.getSubjectCode(), b.getSubjectName(),
                b.getSemester(),    b.getReason(),
                b.getClearanceStatus(), b.getClearanceDate()
            });
        }
        if (backlogs.isEmpty()) {
            MainDashboard.OceanDialog.showMessage(this, "No Backlogs",
                "No backlogs found for " + rollNumber);
        }
    }

    private void detectBacklogs() {
        if (MainDashboard.OceanDialog.showConfirm(this, "Auto-Detect",
                "This will automatically detect backlogs based on marks and attendance.\nContinue?")) {
            int fromMarks      = backlogDAO.autoDetectBacklogsFromMarks();
            int fromAttendance = backlogDAO.autoDetectBacklogsFromAttendance();
            int total          = fromMarks + fromAttendance;

            MainDashboard.OceanDialog.showSuccess(this, "Done",
                "Backlog detection completed!\n"
                + fromMarks      + " backlog(s) detected from Marks\n"
                + fromAttendance + " backlog(s) detected from Attendance\n"
                + total          + " new backlog(s) total.");

            // Refresh table if a roll number is already typed in
            String roll = rollNumberField.getText().trim();
            if (!roll.isEmpty()) loadBacklogsForStudent(roll);
        }
    }

    private void clearBacklog() {
        int row = backlogTable.getSelectedRow();
        if (row < 0) {
            MainDashboard.OceanDialog.showError(this, "Error",
                "Please select a backlog to clear!");
            return;
        }
        int backlogId = (int) tableModel.getValueAt(row, 0);
        if (backlogDAO.markBacklogAsCleared(backlogId)) {
            MainDashboard.OceanDialog.showSuccess(this, "Success",
                "Backlog marked as cleared!");
            viewStudentBacklogs();
        } else {
            MainDashboard.OceanDialog.showError(this, "Error",
                "Failed to clear backlog!");
        }
    }

    private void deleteBacklog() {
        int row = backlogTable.getSelectedRow();
        if (row < 0) {
            MainDashboard.OceanDialog.showError(this, "Error",
                "Please select a backlog to delete!");
            return;
        }
        int backlogId = (int) tableModel.getValueAt(row, 0);
        if (MainDashboard.OceanDialog.showConfirm(this, "Confirm Delete",
                "Delete this backlog record?")) {
            if (backlogDAO.deleteBacklog(backlogId)) {
                MainDashboard.OceanDialog.showSuccess(this, "Deleted",
                    "Backlog deleted!");
                viewStudentBacklogs();
            } else {
                MainDashboard.OceanDialog.showError(this, "Error",
                    "Failed to delete backlog!");
            }
        }
    }

    private void populateFormFromTable() {
        int row = backlogTable.getSelectedRow();
        if (row < 0) return;
        Object rollVal = tableModel.getValueAt(row, 1);
        Object subVal  = tableModel.getValueAt(row, 2);
        if (rollVal != null) rollNumberField .setText(rollVal.toString());
        if (subVal  != null) subjectCodeField.setText(subVal.toString());
    }

    private void clearForm() {
        if (currentStudent == null) rollNumberField.setText("");
        subjectCodeField.setText("");
        tableModel.setRowCount(0);
        backlogTable.clearSelection();
    }

    // ── Field / label helpers ─────────────────────────────────────────────────
    private JTextField field(JPanel panel, String labelText) {
        lbl(panel, labelText);
        gap(panel, 5);
        JTextField f = new JTextField();
        f.setFont(new Font("Verdana", Font.PLAIN, 13));
        f.setBackground(FIELD_BG);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT_AQUA);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(FIELD_BORDER, 1),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        panel.add(f);
        gap(panel, 12);
        return f;
    }

    private void lbl(JPanel panel, String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Verdana", Font.PLAIN, 12));
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(l);
    }

    private void gap(JPanel panel, int h) {
        panel.add(Box.createRigidArea(new Dimension(0, h)));
    }

    private JButton mkBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                java.awt.geom.RoundRectangle2D r =
                    new java.awt.geom.RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fill(r);
                g2.setColor(new Color(255, 255, 255, 30));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(r);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setFont(new Font("Verdana", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 38));
        return btn;
    }

    // ── Ocean scroll bar ──────────────────────────────────────────────────────
    private static class OceanScrollBarUI
            extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor           = new Color(0,  90, 130, 160);
            thumbDarkShadowColor = new Color(0,  60, 100);
            thumbHighlightColor  = new Color(0, 160, 200, 100);
            trackColor           = new Color(0,  20,  32);
            trackHighlightColor  = new Color(0,  48,  82,  60);
        }
        @Override protected JButton createDecreaseButton(int o) { return zero(); }
        @Override protected JButton createIncreaseButton(int o) { return zero(); }
        private JButton zero() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x+2, r.y+2, r.width-4, r.height-4, 6, 6);
            g2.dispose();
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }
}