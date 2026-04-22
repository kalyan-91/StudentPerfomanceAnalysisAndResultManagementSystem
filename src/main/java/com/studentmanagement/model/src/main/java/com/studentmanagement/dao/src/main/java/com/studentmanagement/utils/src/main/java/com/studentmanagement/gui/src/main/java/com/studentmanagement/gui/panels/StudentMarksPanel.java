package com.studentmanagement.gui.panels;

import com.studentmanagement.dao.MarksDAO;
import com.studentmanagement.model.Marks;
import com.studentmanagement.model.Student;
import java.awt.*;
import java.awt.print.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;

/**
 * Student Marks Panel — Semester-wise result view with SGPA per semester and CGPA
 */
public class StudentMarksPanel extends JPanel {

    private final Student   student;
    private final MarksDAO  marksDAO = new MarksDAO();

    // Ocean theme
    private static final Color BG         = new Color(10,  25,  47);
    private static final Color CARD       = new Color(13,  31,  61);
    private static final Color HEADER     = new Color(0,  105, 148);
    private static final Color ACCENT     = new Color(0,  191, 255);
    private static final Color PASS_CLR   = new Color(0,  200, 120);
    private static final Color FAIL_CLR   = new Color(255, 80,  80);
    private static final Color TEXT       = Color.WHITE;
    private static final Color TEXT_DIM   = new Color(160, 200, 220);

    private static final Font TITLE_FONT  = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font CELL_FONT   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font SEM_FONT    = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font GPA_FONT    = new Font("Segoe UI", Font.BOLD, 14);

    public StudentMarksPanel(Student student) {
        this.student = student;
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel title = new JLabel("Academic Results  —  " + student.getName());
        title.setFont(TITLE_FONT);
        title.setForeground(ACCENT);

        JButton refresh = new JButton("Refresh");
        refresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        refresh.setForeground(TEXT);
        refresh.setBackground(HEADER);
        refresh.setFocusPainted(false);
        refresh.setBorderPainted(false);
        refresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refresh.addActionListener(e -> {
            removeAll();
            add(buildHeader(), BorderLayout.NORTH);
            add(buildContent(), BorderLayout.CENTER);
            revalidate(); repaint();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(BG);
        JButton exportBtn = new JButton("Export PDF");
        exportBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        exportBtn.setForeground(TEXT);
        exportBtn.setBackground(new Color(0, 140, 70));
        exportBtn.setFocusPainted(false);
        exportBtn.setBorderPainted(false);
        exportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> exportToPDF());
        btnPanel.add(exportBtn);
        btnPanel.add(refresh);
        p.add(title,    BorderLayout.WEST);
        p.add(btnPanel, BorderLayout.EAST);
        return p;
    }

    // ── Main content (scrollable) ─────────────────────────────────────────────

    private JScrollPane buildContent() {
        List<Marks> allMarks = marksDAO.getMarksByStudentWithDetails(student.getRollNumber());

        // Group by semester
        Map<Integer, List<Marks>> bySem = new LinkedHashMap<>();
        for (Marks m : allMarks) {
            bySem.computeIfAbsent(m.getSemester(), k -> new ArrayList<>()).add(m);
        }

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(BG);

        if (bySem.isEmpty()) {
            JLabel empty = new JLabel("No marks records found.");
            empty.setForeground(TEXT_DIM);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            wrapper.add(Box.createRigidArea(new Dimension(0, 40)));
            wrapper.add(empty);
        } else {
            for (Map.Entry<Integer, List<Marks>> entry : bySem.entrySet()) {
                wrapper.add(buildSemesterCard(entry.getKey(), entry.getValue()));
                wrapper.add(Box.createRigidArea(new Dimension(0, 20)));
            }
            wrapper.add(buildCGPACard(allMarks));
            wrapper.add(Box.createRigidArea(new Dimension(0, 20)));
        }

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // ── Semester card ─────────────────────────────────────────────────────────

    private JPanel buildSemesterCard(int semester, List<Marks> marks) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(HEADER, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Semester header row
        card.add(buildSemHeader(semester, marks), BorderLayout.NORTH);
        // Marks table
        card.add(buildMarksTable(marks), BorderLayout.CENTER);
        // SGPA row
        card.add(buildSGPARow(semester, marks), BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildSemHeader(int semester, List<Marks> marks) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(HEADER);
        p.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel semLabel = new JLabel("  Semester " + semester);
        semLabel.setFont(SEM_FONT);
        semLabel.setForeground(Color.WHITE);

        // Pass/Fail badge
        long failed = marks.stream().filter(m -> "Fail".equals(m.getStatus())).count();
        String badge = failed == 0 ? "  PASS  " : "  FAIL (" + failed + " backlog)";
        Color badgeClr = failed == 0 ? PASS_CLR : FAIL_CLR;
        JLabel result = new JLabel(badge + "  ");
        result.setFont(new Font("Segoe UI", Font.BOLD, 13));
        result.setForeground(badgeClr);

        p.add(semLabel, BorderLayout.WEST);
        p.add(result,   BorderLayout.EAST);
        return p;
    }

    private JScrollPane buildMarksTable(List<Marks> marks) {
        String[] cols = {"Subject Code", "Subject Name", "Internal", "External",
                         "Total /100", "Grade", "Grade Point", "Credits", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Marks m : marks) {
            model.addRow(new Object[]{
                m.getSubjectCode(),
                m.getSubjectName(),
                m.getInternalMarks(),
                m.getExternalMarks(),
                m.getTotalMarks(),
                m.getGrade(),
                String.format("%.1f", m.getGradePoint()),
                m.getCredits(),
                m.getStatus()
            });
        }

        JTable table = new JTable(model);
        table.setFont(CELL_FONT);
        table.setRowHeight(30);
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setSelectionBackground(HEADER);
        table.setGridColor(new Color(30, 60, 100));
        table.setShowGrid(true);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(new Color(0, 70, 110));
        header.setForeground(Color.WHITE);

        // Center-align numeric columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i : new int[]{2, 3, 4, 6, 7}) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Grade column — color coded
        table.getColumnModel().getColumn(5).setCellRenderer(new GradeRenderer());
        // Status column — color coded
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusRenderer());

        // Column widths
        int[] widths = {100, 200, 70, 70, 80, 60, 90, 65, 70};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(CARD);
        sp.getViewport().setBackground(CARD);
        sp.setBorder(BorderFactory.createLineBorder(new Color(30, 60, 100)));
        sp.setPreferredSize(new Dimension(0, 30 + marks.size() * 30 + 10));
        return sp;
    }

    private JPanel buildSGPARow(int semester, List<Marks> marks) {
        double sgpa = marksDAO.calculateSGPA(student.getRollNumber(), semester);
        int totalCr = marks.stream().mapToInt(Marks::getCredits).sum();
        long passed  = marks.stream().filter(m -> "Pass".equals(m.getStatus())).count();
        int earnedCr = marks.stream()
                .filter(m -> "Pass".equals(m.getStatus()))
                .mapToInt(Marks::getCredits).sum();

        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 6));
        p.setBackground(new Color(0, 50, 80));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, HEADER));

        p.add(infoChip("Subjects: " + passed + "/" + marks.size()));
        p.add(infoChip("Credits Earned: " + earnedCr + "/" + totalCr));
        p.add(sgpaChip(sgpa));

        return p;
    }

    // ── CGPA card ────────────────────────────────────────────────────────────

    private JPanel buildCGPACard(List<Marks> allMarks) {
        final double cgpa    = marksDAO.calculateCGPA(student.getRollNumber());
        final int    credits = marksDAO.getTotalCreditsEarned(student.getRollNumber());
        final long   sems    = allMarks.stream().mapToInt(Marks::getSemester).distinct().count();
        final long   cleared = allMarks.stream().filter(m -> "Pass".equals(m.getStatus())).count();

        final String[] labels = {"CGPA", "Credits Earned", "Semesters", "Subjects Cleared"};
        final String[] values = {String.format("%.2f", cgpa), String.valueOf(credits),
                                 String.valueOf(sems), String.valueOf(cleared)};
        final Color[]  colors = {new Color(0, 210, 255), new Color(0, 220, 130),
                                 new Color(180, 160, 255), new Color(0, 220, 130)};

        JPanel card = new JPanel(null) {
            @Override public Dimension getPreferredSize() { return new Dimension(0, 130); }
            @Override public Dimension getMinimumSize()   { return new Dimension(0, 130); }
            @Override public Dimension getMaximumSize()   { return new Dimension(Integer.MAX_VALUE, 130); }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int W = getWidth(), H = getHeight(), n = 4, gap = 12;
                int tileW = (W - gap * (n + 1)) / n;

                for (int i = 0; i < n; i++) {
                    int tx = gap + i * (tileW + gap);
                    int ty = 8, th = H - 16;

                    // Dark tile background
                    g2.setColor(new Color(5, 20, 45));
                    g2.fillRoundRect(tx, ty, tileW, th, 14, 14);

                    // Glowing colored border
                    g2.setColor(colors[i]);
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawRoundRect(tx + 1, ty + 1, tileW - 2, th - 2, 13, 13);

                    // Top accent bar
                    g2.setColor(colors[i]);
                    g2.fillRoundRect(tx, ty, tileW, 6, 7, 7);
                    g2.fillRect(tx, ty + 3, tileW, 3);

                    // BIG VALUE — pure white so always visible
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 30));
                    g2.setColor(Color.WHITE);
                    FontMetrics fv = g2.getFontMetrics();
                    int vx = tx + (tileW - fv.stringWidth(values[i])) / 2;
                    g2.drawString(values[i], vx, ty + th / 2 + 8);

                    // LABEL — light blue, clearly visible
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    g2.setColor(colors[i]);
                    FontMetrics fl = g2.getFontMetrics();
                    int lx = tx + (tileW - fl.stringWidth(labels[i])) / 2;
                    g2.drawString(labels[i], lx, ty + th / 2 + 26);
                }
                g2.dispose();
            }
        };
        card.setBackground(new Color(8, 20, 42));
        card.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 200), 2));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JLabel infoChip(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_DIM);
        return l;
    }

    private JLabel sgpaChip(double sgpa) {
        JLabel l = new JLabel("SGPA:  " + String.format("%.2f", sgpa));
        l.setFont(GPA_FONT);
        l.setForeground(ACCENT);
        return l;
    }

    private JPanel summaryTile(String title, String value, Color clr) {
        JPanel p = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 40, 80));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(clr.darker());
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                // Top accent bar
                g2.setColor(clr);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.setForeground(TEXT_DIM);

        JLabel v = new JLabel(value, SwingConstants.CENTER);
        v.setFont(new Font("Segoe UI", Font.BOLD, 30));
        v.setForeground(clr);

        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    // ── Custom cell renderers ─────────────────────────────────────────────────

    private class GradeRenderer extends DefaultTableCellRenderer {
        GradeRenderer() { setHorizontalAlignment(JLabel.CENTER); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            setBackground(CARD); setForeground(TEXT);
            String grade = v != null ? v.toString() : "";
            switch (grade) {
                case "S":             setForeground(new Color(255, 215, 0)); break;
                case "A1": case "A2": setForeground(new Color(0,   220, 120)); break;
                case "A3": case "B1": setForeground(new Color(0,   191, 255)); break;
                case "B2": case "C1": setForeground(new Color(180, 220, 100)); break;
                case "C2": case "D1": setForeground(new Color(255, 180, 50)); break;
                case "D2":            setForeground(new Color(255, 140, 0)); break;
                case "F":             setForeground(FAIL_CLR); setFont(getFont().deriveFont(Font.BOLD)); break;
            }
            if (sel) setBackground(HEADER);
            return this;
        }
    }

    private class StatusRenderer extends DefaultTableCellRenderer {
        StatusRenderer() { setHorizontalAlignment(JLabel.CENTER); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            setBackground(CARD);
            String status = v != null ? v.toString() : "";
            if ("Pass".equals(status)) {
                setForeground(PASS_CLR);
                setFont(getFont().deriveFont(Font.BOLD));
            } else if ("Fail".equals(status)) {
                setForeground(FAIL_CLR);
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setForeground(TEXT_DIM);
            }
            if (sel) setBackground(HEADER);
            return this;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // PDF EXPORT  — uses Java built-in PrinterJob, no external library
    // ═══════════════════════════════════════════════════════════════════









    private Color pdfGradeColor(String grade) {
        if (grade == null) return new Color(25, 25, 45);
        switch (grade) {
            case "S":             return new Color(160, 110,   0);
            case "A1": case "A2": return new Color(  0, 120,  50);
            case "A3": case "B1": return new Color(  0,  90, 150);
            case "B2": case "C1": return new Color( 70, 110,   0);
            case "C2": case "D1": return new Color(170,  90,   0);
            case "D2":            return new Color(150,  70,   0);
            case "F":             return new Color(190,   0,   0);
            default:              return new Color( 25,  25,  45);
        }
    }


    // ── Button helper ─────────────────────────────────────────────────────────
    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() :
                            getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                              (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(130, 36));
        return b;
    }

    // ── PDF Export with custom save dialog ───────────────────────────────────
    private void exportToPDF() {
        // Custom interactive save dialog
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Export Result Card", true);
        dlg.setSize(480, 300);
        dlg.setLocationRelativeTo(this);
        dlg.setUndecorated(true);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(8, 20, 45));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(0, 150, 200));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 20, 20);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Title
        JLabel title = new JLabel("Export Result Card as PDF");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(0, 210, 255));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Center content
        JPanel center = new JPanel(new GridLayout(3, 1, 0, 12));
        center.setOpaque(false);

        // File name row
        JPanel nameRow = new JPanel(new BorderLayout(10, 0));
        nameRow.setOpaque(false);
        JLabel nameLbl = new JLabel("File Name:");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLbl.setForeground(new Color(160, 200, 220));
        nameLbl.setPreferredSize(new Dimension(90, 0));
        String defaultName = student.getRollNumber() + "_ResultCard_" +
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        JTextField nameField = new JTextField(defaultName);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameField.setBackground(new Color(5, 30, 65));
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(Color.WHITE);
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 120, 180), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        nameRow.add(nameLbl, BorderLayout.WEST);
        nameRow.add(nameField, BorderLayout.CENTER);

        // Save location row
        JPanel locRow = new JPanel(new BorderLayout(10, 0));
        locRow.setOpaque(false);
        JLabel locLbl = new JLabel("Save To:");
        locLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        locLbl.setForeground(new Color(160, 200, 220));
        locLbl.setPreferredSize(new Dimension(90, 0));
        String desktop = System.getProperty("user.home") + File.separator + "Desktop";
        JTextField locField = new JTextField(desktop);
        locField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        locField.setBackground(new Color(5, 30, 65));
        locField.setForeground(Color.WHITE);
        locField.setCaretColor(Color.WHITE);
        locField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 120, 180), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        JButton browseBtn = makeBtn("Browse", new Color(0, 100, 150));
        browseBtn.setPreferredSize(new Dimension(90, 36));
        browseBtn.addActionListener(e -> showDarkFolderPicker(dlg, locField));
        locRow.add(locLbl,   BorderLayout.WEST);
        locRow.add(locField, BorderLayout.CENTER);
        locRow.add(browseBtn, BorderLayout.EAST);

        // Info row
        JLabel info = new JLabel("PDF will include all semesters, grades, SGPA and CGPA summary.");
        info.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        info.setForeground(new Color(120, 160, 200));

        center.add(nameRow);
        center.add(locRow);
        center.add(info);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton cancelBtn = makeBtn("Cancel", new Color(80, 40, 40));
        cancelBtn.addActionListener(e -> dlg.dispose());

        JButton saveBtn = makeBtn("Save PDF", new Color(0, 140, 70));
        saveBtn.setPreferredSize(new Dimension(120, 36));
        saveBtn.addActionListener(e -> {
            dlg.dispose();
            String fname = nameField.getText().trim();
            if (!fname.toLowerCase().endsWith(".pdf")) fname += ".pdf";
            File finalFile = new File(locField.getText().trim(), fname);
            doExport(finalFile);
        });

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        root.add(title,  BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private void doExport(File finalFile) {
        final List<Marks> allMarks = marksDAO.getMarksByStudentWithDetails(student.getRollNumber());
        final Map<Integer, List<Marks>> bySem = new LinkedHashMap<>();
        for (Marks m : allMarks) bySem.computeIfAbsent(m.getSemester(), k -> new ArrayList<>()).add(m);
        final double cgpa    = marksDAO.calculateCGPA(student.getRollNumber());
        final int    credits = marksDAO.getTotalCreditsEarned(student.getRollNumber());

        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pf  = job.defaultPage();
        Paper paper    = new Paper();
        double a4W=595, a4H=842, mgn=36;
        paper.setSize(a4W, a4H);
        paper.setImageableArea(mgn, mgn, a4W-mgn*2, a4H-mgn*2);
        pf.setPaper(paper); pf.setOrientation(PageFormat.PORTRAIT);
        final List<List<Integer>> pages = paginateSems(bySem, (float)(a4H-mgn*2));

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex >= pages.size()) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            float pw=(float)pageFormat.getImageableWidth(), ph=(float)pageFormat.getImageableHeight();
            float y=0;
            if (pageIndex==0) y=pdfHeader(g2,pw,y,cgpa,credits,allMarks.size(),bySem.size());
            for (int sn : pages.get(pageIndex)) { List<Marks> mx=bySem.get(sn); if(mx!=null) y=pdfSemTable(g2,pw,y,sn,mx); }
            if (pageIndex==pages.size()-1) pdfFooter(g2,pw,y,cgpa,credits);
            g2.setFont(new Font("Segoe UI",Font.PLAIN,8)); g2.setColor(new Color(120,120,140));
            String pg="Page "+(pageIndex+1)+" of "+pages.size();
            g2.drawString(pg,pw-g2.getFontMetrics().stringWidth(pg),ph-2);
            return Printable.PAGE_EXISTS;
        }, pf);

        PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
        attrs.add(new Destination(finalFile.toURI()));
        attrs.add(MediaSizeName.ISO_A4);

        try {
            job.print(attrs);
            // Success dialog
            showSuccessDialog(finalFile.getAbsolutePath());
        } catch (PrinterException ex) {
            showErrorDialog("Export failed: " + ex.getMessage());
        }
    }

    private List<List<Integer>> paginateSems(Map<Integer,List<Marks>> bySem, float ph) {
        List<List<Integer>> pages=new ArrayList<>(); List<Integer> cur=new ArrayList<>(); float used=175f;
        for (Map.Entry<Integer,List<Marks>> e : bySem.entrySet()) {
            float h=22f+17f+e.getValue().size()*15f+12f;
            if (used+h>ph-80 && !cur.isEmpty()) { pages.add(cur); cur=new ArrayList<>(); used=0; }
            cur.add(e.getKey()); used+=h;
        }
        if (!cur.isEmpty()) pages.add(cur);
        if (pages.isEmpty()) pages.add(new ArrayList<>());
        return pages;
    }

    private float pdfHeader(Graphics2D g2,float w,float y,double cgpa,int credits,int subj,int sems) {
        g2.setColor(new Color(0,55,110)); g2.fillRoundRect(0,(int)y,(int)w,46,8,8);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI",Font.BOLD,16));
        pdfC(g2,"STUDENT RESULT CARD",w,y+19);
        g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
        pdfC(g2,"Academic Transcript  -  Student Management System",w,y+35); y+=54;
        g2.setColor(new Color(234,244,255)); g2.fillRect(0,(int)y,(int)w,66);
        g2.setColor(new Color(0,80,150)); g2.setStroke(new BasicStroke(1f)); g2.drawRect(0,(int)y,(int)w-1,65);
        float ry=y+16;
        pdfKV(g2,"Name:",student.getName(),8f,ry); pdfKV(g2,"Roll No:",student.getRollNumber(),w/2+8f,ry); ry+=18;
        pdfKV(g2,"Branch:","MCA",8f,ry); pdfKV(g2,"Year/Sec:",student.getYear()+" / "+student.getSection(),w/2+8f,ry); ry+=18;
        pdfKV(g2,"Status:",student.getStatus(),8f,ry); pdfKV(g2,"Date:",LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),w/2+8f,ry);
        y+=74;
        g2.setColor(new Color(0,100,55)); g2.fillRoundRect(0,(int)y,(int)w,26,4,4);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI",Font.BOLD,10));
        String[] cols={"CGPA: "+String.format("%.2f",cgpa),"Credits: "+credits,"Subjects: "+subj,"Semesters: "+sems};
        float seg=w/cols.length;
        for(int i=0;i<cols.length;i++){int sw=g2.getFontMetrics().stringWidth(cols[i]);g2.drawString(cols[i],i*seg+(seg-sw)/2f,y+18);}
        return y+34;
    }

    private float pdfSemTable(Graphics2D g2,float w,float y,int sem,List<Marks> marks) {
        float mx=8f,tw=w-mx*2;
        g2.setColor(new Color(0,75,125)); g2.fillRect((int)mx,(int)y,(int)tw,20);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI",Font.BOLD,10));
        long failed=marks.stream().filter(m->"Fail".equals(m.getStatus())).count();
        g2.drawString("  SEMESTER "+sem+(failed==0?"   [ PASS ]":"   [ FAIL - "+failed+" backlog(s) ]"),mx+4,y+14);
        double sgpa=marksDAO.calculateSGPA(student.getRollNumber(),sem);
        String sg="SGPA: "+String.format("%.2f",sgpa)+"  ";
        g2.drawString(sg,mx+tw-g2.getFontMetrics().stringWidth(sg),y+14); y+=22;
        float[] cw={55,tw-55-26-26-34-34-28-22-40,26,26,34,34,28,22,40};
        String[] ch={"Code","Subject Name","Int","Ext","Total","Grade","GP","Cr","Status"};
        g2.setColor(new Color(215,230,250)); g2.fillRect((int)mx,(int)y,(int)tw,15);
        g2.setColor(new Color(0,50,100)); g2.setFont(new Font("Segoe UI",Font.BOLD,8));
        float cx=mx+2; for(int c=0;c<ch.length;c++){g2.drawString(ch[c],cx+2,y+11);cx+=cw[c];} y+=17;
        boolean alt=false;
        for (Marks m : marks) {
            g2.setColor(alt?new Color(243,248,255):Color.WHITE); g2.fillRect((int)mx,(int)y,(int)tw,14); alt=!alt;
            boolean fail="Fail".equals(m.getStatus());
            g2.setFont(new Font("Segoe UI",fail?Font.BOLD:Font.PLAIN,8));
            cx=mx+2;
            String[] v={m.getSubjectCode(),pdfTrim(m.getSubjectName(),24),String.valueOf(m.getInternalMarks()),
                String.valueOf(m.getExternalMarks()),String.valueOf(m.getTotalMarks()),m.getGrade(),
                String.format("%.1f",m.getGradePoint()),String.valueOf(m.getCredits()),m.getStatus()};
            for(int c=0;c<v.length;c++){
                if(c==5) g2.setColor(pdfGC(m.getGrade()));
                else if(c==8) g2.setColor(fail?new Color(190,0,0):new Color(0,130,50));
                else g2.setColor(new Color(25,25,45));
                g2.drawString(v[c],cx+2,y+10); cx+=cw[c];
            }
            y+=15;
        }
        g2.setColor(new Color(180,200,230)); g2.setStroke(new BasicStroke(0.5f));
        g2.drawLine((int)mx,(int)y,(int)(mx+tw),(int)y); return y+9;
    }

    private void pdfFooter(Graphics2D g2,float w,float y,double cgpa,int credits) {
        y+=6; g2.setColor(new Color(0,85,45)); g2.fillRoundRect(8,(int)y,(int)(w-16),30,6,6);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI",Font.BOLD,11));
        pdfC(g2,"Overall CGPA:  "+String.format("%.2f",cgpa)+"     |     Total Credits Earned:  "+credits,w,y+21); y+=38;
        g2.setColor(new Color(80,90,110)); g2.setFont(new Font("Segoe UI",Font.PLAIN,7));
        g2.drawString("Grade Scale: S(>=95,10.0) A1(90-94,9.5) A2(85-89,9.0) A3(80-84,8.5) B1(75-79,8.0) B2(70-74,7.5) C1(65-69,7.0) C2(60-64,6.5) D1(55-59,6.0) D2(50-54,5.5) F(<50,0)",10,y+8); y+=18;
        g2.setColor(new Color(160,160,180)); g2.setStroke(new BasicStroke(0.5f));
        g2.drawLine((int)(w*0.6f),(int)y,(int)(w-20),(int)y);
        g2.setFont(new Font("Segoe UI",Font.PLAIN,8)); g2.drawString("Authorized Signature",(int)(w*0.6f),(int)y+12); y+=22;
        g2.setColor(new Color(150,150,170)); g2.setFont(new Font("Segoe UI",Font.ITALIC,7));
        pdfC(g2,"This is a computer-generated document.  -  Student Management System",w,y);
    }

    private void pdfC(Graphics2D g2,String s,float w,float y){g2.drawString(s,(w-g2.getFontMetrics().stringWidth(s))/2f,y);}
    private void pdfKV(Graphics2D g2,String k,String v,float x,float y){
        g2.setFont(new Font("Segoe UI",Font.BOLD,10)); g2.setColor(new Color(0,55,110)); g2.drawString(k,x,y);
        float kw=g2.getFontMetrics().stringWidth(k)+4;
        g2.setFont(new Font("Segoe UI",Font.PLAIN,10)); g2.setColor(new Color(25,25,55));
        g2.drawString(v!=null?v:"-",x+kw,y);
    }
    private String pdfTrim(String s,int max){if(s==null)return "-";return s.length()>max?s.substring(0,max-1)+"~":s;}
    private Color pdfGC(String g){
        if(g==null)return new Color(25,25,45);
        switch(g){
            case "S": return new Color(160,110,0); case "A1":case "A2": return new Color(0,120,50);
            case "A3":case "B1": return new Color(0,90,150); case "B2":case "C1": return new Color(70,110,0);
            case "C2":case "D1": return new Color(170,90,0); case "D2": return new Color(150,70,0);
            case "F": return new Color(190,0,0); default: return new Color(25,25,45);
        }
    }





    // ── Dark themed folder picker (replaces JFileChooser) ────────────────────
    private void showDarkFolderPicker(JDialog parent, JTextField locField) {
        Color BG2    = new Color(8,  20,  45);
        Color CARD2  = new Color(12, 30,  65);
        Color BORDER = new Color(0,  130, 190);
        Color FG     = new Color(200, 225, 255);
        Color SEL    = new Color(0,   80, 140);

        JDialog picker = new JDialog(parent, "Choose Save Location", true);
        picker.setUndecorated(true);
        picker.setSize(520, 420);
        picker.setLocationRelativeTo(parent);

        JPanel root = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 16, 16);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ── Title bar ──
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        JLabel titleLbl = new JLabel("  Choose Save Location");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(new Color(0, 210, 255));
        JButton closeX = new JButton("x");
        closeX.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeX.setForeground(FG);
        closeX.setBackground(new Color(150, 30, 30));
        closeX.setFocusPainted(false);
        closeX.setBorderPainted(false);
        closeX.setPreferredSize(new Dimension(28, 28));
        closeX.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeX.addActionListener(e -> picker.dispose());
        titleBar.add(titleLbl, BorderLayout.WEST);
        titleBar.add(closeX,   BorderLayout.EAST);

        // ── Current path bar ──
        JPanel pathBar = new JPanel(new BorderLayout(8, 0));
        pathBar.setOpaque(false);
        pathBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        JLabel pathLbl = new JLabel("Path:");
        pathLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pathLbl.setForeground(FG);
        pathLbl.setPreferredSize(new Dimension(40, 0));
        JTextField pathField = new JTextField(locField.getText());
        pathField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pathField.setBackground(CARD2);
        pathField.setForeground(Color.WHITE);
        pathField.setCaretColor(Color.WHITE);
        pathField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        pathBar.add(pathLbl,  BorderLayout.WEST);
        pathBar.add(pathField, BorderLayout.CENTER);

        // ── Folder list ──
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> folderList = new JList<>(listModel);
        folderList.setBackground(CARD2);
        folderList.setForeground(FG);
        folderList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        folderList.setSelectionBackground(SEL);
        folderList.setSelectionForeground(Color.WHITE);
        folderList.setFixedCellHeight(30);
        folderList.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JScrollPane listScroll = new JScrollPane(folderList);
        listScroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        listScroll.setBackground(CARD2);
        listScroll.getViewport().setBackground(CARD2);
        listScroll.getVerticalScrollBar().setBackground(BG2);

        // Load folders helper
        java.util.function.Consumer<String> loadFolders = (path) -> {
            listModel.clear();
            File dir = new File(path);
            File parent2 = dir.getParentFile();
            if (parent2 != null) listModel.addElement(".. (Up)");
            File[] files = dir.listFiles();
            if (files != null) {
                java.util.Arrays.sort(files);
                for (File f : files) {
                    if (f.isDirectory() && !f.isHidden())
                        listModel.addElement("  " + f.getName());
                }
            }
        };

        // Initial load
        loadFolders.accept(locField.getText());

        // Double click to navigate
        folderList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String sel = folderList.getSelectedValue();
                    if (sel == null) return;
                    String cur = pathField.getText();
                    if (sel.equals(".. (Up)")) {
                        File up = new File(cur).getParentFile();
                        if (up != null) { pathField.setText(up.getAbsolutePath()); loadFolders.accept(up.getAbsolutePath()); }
                    } else {
                        String name = sel.trim();
                        File next = new File(cur, name);
                        if (next.isDirectory()) { pathField.setText(next.getAbsolutePath()); loadFolders.accept(next.getAbsolutePath()); }
                    }
                }
            }
        });

        // Single click to select
        folderList.addListSelectionListener(e2 -> {
            String sel = folderList.getSelectedValue();
            if (sel != null && !sel.equals(".. (Up)")) {
                String name = sel.trim();
                File f = new File(pathField.getText(), name);
                if (f.isDirectory()) pathField.setText(f.getAbsolutePath());
            }
        });

        // Refresh when path manually edited
        pathField.addActionListener(e -> loadFolders.accept(pathField.getText()));

        // ── Quick access panel ──
        JPanel quickPanel = new JPanel();
        quickPanel.setLayout(new BoxLayout(quickPanel, BoxLayout.Y_AXIS));
        quickPanel.setBackground(new Color(6, 16, 38));
        quickPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        quickPanel.setPreferredSize(new Dimension(110, 0));

        String[][] quickPaths = {
            {"Desktop",   System.getProperty("user.home") + File.separator + "Desktop"},
            {"Documents", System.getProperty("user.home") + File.separator + "Documents"},
            {"Downloads", System.getProperty("user.home") + File.separator + "Downloads"},
            {"Home",      System.getProperty("user.home")},
        };
        JLabel quickTitle = new JLabel("Quick");
        quickTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        quickTitle.setForeground(new Color(0, 180, 220));
        quickTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        quickTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        quickPanel.add(quickTitle);

        for (String[] qp : quickPaths) {
            JButton qb = new JButton(qp[0]) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(getModel().isRollover() ? SEL : new Color(10, 28, 60));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                    g2.setColor(FG);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                    g2.dispose();
                }
            };
            qb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            qb.setFocusPainted(false); qb.setBorderPainted(false); qb.setContentAreaFilled(false);
            qb.setCursor(new Cursor(Cursor.HAND_CURSOR));
            qb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            qb.setAlignmentX(Component.CENTER_ALIGNMENT);
            String qPath = qp[1];
            qb.addActionListener(e -> { pathField.setText(qPath); loadFolders.accept(qPath); });
            quickPanel.add(qb);
            quickPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, quickPanel, listScroll);
        split.setDividerSize(2);
        split.setBackground(BG2);
        split.setBorder(null);

        // ── Bottom buttons ──
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        btns.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton cancelB = makeBtn("Cancel", new Color(100, 30, 30));
        cancelB.addActionListener(e -> picker.dispose());

        JButton selectB = makeBtn("Select Folder", new Color(0, 120, 60));
        selectB.setPreferredSize(new Dimension(130, 36));
        selectB.addActionListener(e -> {
            locField.setText(pathField.getText());
            picker.dispose();
        });

        btns.add(cancelB);
        btns.add(selectB);

        root.add(titleBar,  BorderLayout.NORTH);
        root.add(pathBar,   BorderLayout.CENTER);

        JPanel mainArea = new JPanel(new BorderLayout(0, 8));
        mainArea.setOpaque(false);
        mainArea.add(pathBar, BorderLayout.NORTH);
        mainArea.add(split,   BorderLayout.CENTER);
        mainArea.add(btns,    BorderLayout.SOUTH);

        root.add(titleBar, BorderLayout.NORTH);
        root.add(mainArea, BorderLayout.CENTER);

        picker.setContentPane(root);
        picker.setVisible(true);
    }


    // ── Custom success dialog ─────────────────────────────────────────────────
    private void showSuccessDialog(String filePath) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dlg.setUndecorated(true);
        dlg.setSize(460, 220);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Dark gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(5, 18, 40), 0, getHeight(), new Color(0, 35, 25));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                // Green border
                g2.setColor(new Color(0, 200, 100));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 18, 18);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Icon + title row
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topRow.setOpaque(false);

        // Green circle tick icon (drawn)
        JPanel iconPanel = new JPanel() {
            @Override public Dimension getPreferredSize() { return new Dimension(42, 42); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 200, 100));
                g2.fillOval(0, 0, 40, 40);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(10, 21, 17, 30);
                g2.drawLine(17, 30, 31, 12);
                g2.dispose();
            }
        };
        iconPanel.setOpaque(false);

        JLabel titleLbl = new JLabel("Export Successful!");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(new Color(0, 220, 110));

        topRow.add(iconPanel);
        topRow.add(titleLbl);

        // File path label
        JLabel pathLbl = new JLabel("<html><span style='color:#a0c8e0'>File saved to:</span><br>" +
            "<span style='color:#ffffff; font-family:monospace'>" +
            (filePath.length() > 55 ? "..." + filePath.substring(filePath.length()-52) : filePath) +
            "</span></html>");
        pathLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pathLbl.setBorder(BorderFactory.createEmptyBorder(12, 0, 16, 0));

        // OK button — large, clearly visible
        JButton okBtn = new JButton("  OK  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()  ? new Color(0, 150, 60) :
                           getModel().isRollover() ? new Color(0, 210, 90) :
                                                     new Color(0, 180, 80);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText().trim(),
                    (getWidth()-fm.stringWidth(getText().trim()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        okBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        okBtn.setPreferredSize(new Dimension(120, 42));
        okBtn.setFocusPainted(false);
        okBtn.setBorderPainted(false);
        okBtn.setContentAreaFilled(false);
        okBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okBtn.addActionListener(e -> dlg.dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setOpaque(false);
        btnRow.add(okBtn);

        root.add(topRow,  BorderLayout.NORTH);
        root.add(pathLbl, BorderLayout.CENTER);
        root.add(btnRow,  BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.getRootPane().setDefaultButton(okBtn);
        dlg.setVisible(true);
    }

    private void showErrorDialog(String message) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dlg.setUndecorated(true);
        dlg.setSize(420, 190);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(5, 18, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(220, 50, 50));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 18, 18);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        JLabel titleLbl = new JLabel("Export Failed");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLbl.setForeground(new Color(255, 80, 80));

        JLabel msgLbl = new JLabel("<html>" + message + "</html>");
        msgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        msgLbl.setForeground(new Color(200, 210, 230));
        msgLbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 14, 0));

        JButton okBtn = new JButton("OK") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(220, 60, 60) : new Color(180, 40, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("OK", (getWidth()-fm.stringWidth("OK"))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        okBtn.setPreferredSize(new Dimension(100, 38));
        okBtn.setFocusPainted(false);
        okBtn.setBorderPainted(false);
        okBtn.setContentAreaFilled(false);
        okBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okBtn.addActionListener(e -> dlg.dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setOpaque(false);
        btnRow.add(okBtn);

        root.add(titleLbl, BorderLayout.NORTH);
        root.add(msgLbl,   BorderLayout.CENTER);
        root.add(btnRow,   BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.getRootPane().setDefaultButton(okBtn);
        dlg.setVisible(true);
    }

}