package com.studentmanagement.gui.panels;

import com.studentmanagement.model.Faculty;
import com.studentmanagement.model.Subject;
import com.studentmanagement.dao.FacultyDAO;
import com.studentmanagement.dao.SubjectDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Faculty portal — My Subjects view.
 * Shows only the subjects assigned to the logged-in faculty (read-only).
 */
public class FacultySubjectPanel extends JPanel {

    private Faculty    faculty;
    private FacultyDAO facultyDAO = new FacultyDAO();
    private SubjectDAO subjectDAO = new SubjectDAO();

    private JTable            subjectTable;
    private DefaultTableModel tableModel;

    // ── Ocean palette ─────────────────────────────────────────────────────────
    private static final Color CONTENT_BG   = new Color(0,  42,  62);
    private static final Color ACCENT_AQUA  = new Color(0, 185, 210);
    private static final Color TEXT_PRIMARY = new Color(220, 245, 255);
    private static final Color TEXT_MUTED   = new Color(100, 175, 195);
    private static final Color TABLE_BG     = new Color(0,  18,  30);
    private static final Color TABLE_ALT    = new Color(0,  28,  45);
    private static final Color DIVIDER      = new Color(0,  90, 130, 60);

    public FacultySubjectPanel(Faculty faculty) {
        this.faculty = faculty;
        setLayout(new BorderLayout(0, 14));
        setBackground(CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createTopPanel(),   BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::loadSubjects);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel title = new JLabel("My Subjects");
        title.setFont(new Font("Georgia", Font.BOLD, 22));
        title.setForeground(ACCENT_AQUA);

        JLabel sub = new JLabel("Subjects currently assigned to you by admin");
        sub.setFont(new Font("Verdana", Font.PLAIN, 12));
        sub.setForeground(TEXT_MUTED);

        JButton refreshBtn = mkBtn("Refresh", new Color(0, 65, 105));
        refreshBtn.addActionListener(e -> loadSubjects());

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(title);
        left.add(Box.createRigidArea(new Dimension(0, 4)));
        left.add(sub);

        panel.add(left,       BorderLayout.WEST);
        panel.add(refreshBtn, BorderLayout.EAST);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);

        String[] cols = {"Subject Code", "Subject Name", "Year",
                         "Semester", "Credits", "Pass Marks"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        subjectTable = new JTable(tableModel);
        subjectTable.setFont(new Font("Verdana", Font.PLAIN, 12));
        subjectTable.setRowHeight(30);
        subjectTable.setBackground(TABLE_BG);
        subjectTable.setForeground(TEXT_PRIMARY);
        subjectTable.setGridColor(DIVIDER);
        subjectTable.setShowHorizontalLines(true);
        subjectTable.setShowVerticalLines(false);
        subjectTable.setSelectionBackground(new Color(0, 55, 100));
        subjectTable.setSelectionForeground(ACCENT_AQUA);

        subjectTable.setDefaultRenderer(Object.class,
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                    super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                    setBackground(sel ? new Color(0, 55, 100)
                        : (row % 2 == 0 ? TABLE_BG : TABLE_ALT));
                    setForeground(sel ? ACCENT_AQUA : TEXT_PRIMARY);
                    setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                    return this;
                }
            });

        subjectTable.getTableHeader().setFont(new Font("Verdana", Font.BOLD, 12));
        subjectTable.getTableHeader().setBackground(new Color(0, 45, 70));
        subjectTable.getTableHeader().setForeground(ACCENT_AQUA);
        subjectTable.getTableHeader().setBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_AQUA));

        JScrollPane sp = new JScrollPane(subjectTable);
        sp.setBackground(TABLE_BG);
        sp.getViewport().setBackground(TABLE_BG);
        sp.setBorder(BorderFactory.createLineBorder(
            new Color(0, 185, 210, 60), 1));
        sp.getVerticalScrollBar()  .setUI(new OceanScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new OceanScrollBarUI());

        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private void loadSubjects() {
        tableModel.setRowCount(0);
        List<String> codes = facultyDAO.getFacultySubjects(faculty.getFacultyId());

        for (String code : codes) {
            Subject s = subjectDAO.getSubjectByCode(code);
            if (s != null) {
                tableModel.addRow(new Object[]{
                    s.getSubjectCode(), s.getSubjectName(),
                    s.getYear(),        s.getSemester(),    s.getCredits(),
                    s.getPassMarks()
                });
            }
        }

        if (codes.isEmpty()) {
            tableModel.addRow(new Object[]{
                "", "No subjects assigned yet. Contact admin to assign subjects.", "",
                "", "", "", ""
            });
        }
    }

    private JButton mkBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setFont(new Font("Verdana", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 36));
        return btn;
    }

    private static class OceanScrollBarUI
            extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(0, 90, 130, 160);
            trackColor = new Color(0, 18, 30);
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