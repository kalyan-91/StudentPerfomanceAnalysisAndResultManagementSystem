package com.studentmanagement.gui.panels;

import com.studentmanagement.model.Student;
import com.studentmanagement.dao.BacklogDAO;
import com.studentmanagement.model.Backlog;
import com.studentmanagement.gui.MainDashboard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentBacklogPanel extends JPanel {

    private Student student;
    private BacklogDAO backlogDAO = new BacklogDAO();
    private JTable backlogTable;
    private DefaultTableModel tableModel;

    // Live-updatable labels — refreshed every time loadBacklogs() runs
    private JLabel countLabel;        // top-right "⚠ N Pending" / "✔ All Clear"
    private JLabel statTotal;         // bottom stat box values
    private JLabel statPending;
    private JLabel statCleared;

    // ── Ocean #006994 palette ─────────────────────────────────────────────────
    private static final Color CONTENT_BG   = new Color(0,   42,  62);
    private static final Color CARD_BG      = new Color(0,   28,  42);
    private static final Color TABLE_BG     = new Color(0,   18,  30);
    private static final Color TABLE_ALT    = new Color(0,   28,  45);
    private static final Color FIELD_BORDER = new Color(0,  148, 180, 120);
    private static final Color TEXT_PRIMARY = new Color(220, 245, 255);
    private static final Color TEXT_MUTED   = new Color(100, 175, 195);
    private static final Color ACCENT_AQUA  = new Color(0,  185, 210);
    private static final Color DIVIDER      = new Color(0,   90, 130,  60);
    private static final Color CLR_PENDING  = new Color(255, 130, 100);
    private static final Color CLR_CLEARED  = new Color(50,  230, 180);
    private static final Color CLR_TOTAL    = new Color(0,  185, 210);

    public StudentBacklogPanel(Student student) {
        this.student = student;
        setLayout(new BorderLayout(0, 14));
        setBackground(CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createTopPanel(),   BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createStatsPanel(), BorderLayout.SOUTH);

        // ── KEY FIX: load data AFTER panel is fully built, no popup ──────────
        SwingUtilities.invokeLater(this::loadBacklogs);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel titleLabel = new JLabel("My Backlogs  —  " + student.getName());
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 22));
        titleLabel.setForeground(ACCENT_AQUA);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setBackground(CONTENT_BG);

        // Store as field so loadBacklogs() can update it
        int pendingCount = backlogDAO.getPendingBacklogCount(student.getRollNumber());
        countLabel = new JLabel(
            pendingCount > 0 ? "⚠  " + pendingCount + " Pending" : "✔  All Clear");
        countLabel.setFont(new Font("Verdana", Font.BOLD, 14));
        countLabel.setForeground(pendingCount > 0 ? CLR_PENDING : CLR_CLEARED);

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(new Font("Verdana", Font.BOLD, 12));
        refreshBtn.setForeground(ACCENT_AQUA);
        refreshBtn.setBackground(CARD_BG);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadBacklogs());

        rightPanel.add(countLabel);
        rightPanel.add(refreshBtn);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    // ── Table panel ───────────────────────────────────────────────────────────
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);

        String[] columns = {"Subject Code", "Subject Name",
                            "Semester", "Reason", "Status", "Cleared Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        backlogTable = new JTable(tableModel);
        backlogTable.setFont(new Font("Verdana", Font.PLAIN, 13));
        backlogTable.setRowHeight(32);
        backlogTable.setBackground(TABLE_BG);
        backlogTable.setForeground(TEXT_PRIMARY);
        backlogTable.setSelectionBackground(new Color(0, 55, 100));
        backlogTable.setSelectionForeground(ACCENT_AQUA);
        backlogTable.setGridColor(DIVIDER);
        backlogTable.setShowHorizontalLines(true);
        backlogTable.setShowVerticalLines(false);
        backlogTable.setIntercellSpacing(new Dimension(0, 1));

        backlogTable.getTableHeader().setFont(new Font("Verdana", Font.BOLD, 12));
        backlogTable.getTableHeader().setBackground(new Color(0, 45, 70));
        backlogTable.getTableHeader().setForeground(ACCENT_AQUA);
        backlogTable.getTableHeader().setBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_AQUA));

        // Colour-code Status column
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
                        setBackground(row % 2 == 0 ? TABLE_BG : TABLE_ALT);
                        if (col == 4 && v != null) {          // Status column
                            String s = v.toString().toLowerCase();
                            setForeground(s.contains("clear") ? CLR_CLEARED : CLR_PENDING);
                            setFont(new Font("Verdana", Font.BOLD, 12));
                        } else {
                            setForeground(TEXT_PRIMARY);
                            setFont(new Font("Verdana", Font.PLAIN, 12));
                        }
                    }
                    setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                    return this;
                }
            });

        JScrollPane sp = new JScrollPane(backlogTable);
        sp.setBackground(TABLE_BG);
        sp.getViewport().setBackground(TABLE_BG);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0, 185, 210, 70), 1));
        sp.getVerticalScrollBar()  .setUI(new OceanScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new OceanScrollBarUI());
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // ── Stats panel ───────────────────────────────────────────────────────────
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 16, 0));
        panel.setBackground(CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        BacklogDAO.BacklogStats stats =
            backlogDAO.getStudentBacklogStats(student.getRollNumber());

        int total   = stats != null ? stats.totalBacklogs : 0;
        int pending = stats != null ? stats.pendingCount  : 0;
        int cleared = stats != null ? stats.clearedCount  : 0;

        // Store labels as fields so loadBacklogs() can update them live
        statTotal   = new JLabel(String.valueOf(total));
        statPending = new JLabel(String.valueOf(pending));
        statCleared = new JLabel(String.valueOf(cleared));

        panel.add(statBox("Total Backlogs", statTotal,   CLR_TOTAL));
        panel.add(statBox("Pending",        statPending, CLR_PENDING));
        panel.add(statBox("Cleared",        statCleared, CLR_CLEARED));
        return panel;
    }

    private JPanel statBox(String label, JLabel valLbl, Color accent) {
        JPanel box = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(accent.getRed(), accent.getGreen(),
                    accent.getBlue(), 80));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                // top accent stripe
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 3, 3, 3);
                g2.dispose();
            }
        };
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));
        box.setPreferredSize(new Dimension(0, 85));

        valLbl.setFont(new Font("Georgia", Font.BOLD, 34));
        valLbl.setForeground(accent);
        valLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblLbl = new JLabel(label);
        lblLbl.setFont(new Font("Verdana", Font.PLAIN, 12));
        lblLbl.setForeground(TEXT_MUTED);
        lblLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(Box.createVerticalGlue());
        box.add(valLbl);
        box.add(Box.createRigidArea(new Dimension(0, 4)));
        box.add(lblLbl);
        box.add(Box.createVerticalGlue());
        return box;
    }

    // ── Data load — updates table AND all stat labels on every call ───────────
    private void loadBacklogs() {
        tableModel.setRowCount(0);
        List<Backlog> backlogs =
            backlogDAO.getBacklogsByStudent(student.getRollNumber());

        for (Backlog b : backlogs) {
            tableModel.addRow(new Object[]{
                b.getSubjectCode(),
                b.getSubjectName(),
                b.getSemester(),
                b.getReason(),
                b.getClearanceStatus(),
                b.getClearanceDate() != null ? b.getClearanceDate() : "—"
            });
        }

        // ── Refresh stat boxes ────────────────────────────────────────────────
        BacklogDAO.BacklogStats stats =
            backlogDAO.getStudentBacklogStats(student.getRollNumber());
        int total   = stats != null ? stats.totalBacklogs : 0;
        int pending = stats != null ? stats.pendingCount  : 0;
        int cleared = stats != null ? stats.clearedCount  : 0;

        if (statTotal   != null) statTotal  .setText(String.valueOf(total));
        if (statPending != null) statPending.setText(String.valueOf(pending));
        if (statCleared != null) statCleared.setText(String.valueOf(cleared));

        // ── Refresh top-right count label ─────────────────────────────────────
        if (countLabel != null) {
            countLabel.setText(pending > 0 ? "⚠  " + pending + " Pending" : "✔  All Clear");
            countLabel.setForeground(pending > 0 ? CLR_PENDING : CLR_CLEARED);
        }
    }

    // ── Ocean scroll bar
    private static class OceanScrollBarUI
            extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor           = new Color(0,  90, 130, 160);
            thumbDarkShadowColor = new Color(0,  60, 100);
            thumbHighlightColor  = new Color(0, 160, 200, 100);
            trackColor           = new Color(0,  18,  30);
            trackHighlightColor  = new Color(0,  48,  82,  60);
        }
        @Override protected JButton createDecreaseButton(int o) { return zero(); }
        @Override protected JButton createIncreaseButton(int o) { return zero(); }
        private JButton zero() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0,0));
            b.setMinimumSize(new Dimension(0,0));
            b.setMaximumSize(new Dimension(0,0));
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