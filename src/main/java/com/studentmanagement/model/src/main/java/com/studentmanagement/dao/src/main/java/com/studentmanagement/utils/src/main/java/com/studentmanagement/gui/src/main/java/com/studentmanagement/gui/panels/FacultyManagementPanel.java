package com.studentmanagement.gui.panels;

import com.studentmanagement.dao.FacultyDAO;
import com.studentmanagement.model.Faculty;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FacultyManagementPanel extends JPanel {
    
    private FacultyDAO facultyDAO = new FacultyDAO();
    private JTable facultyTable;
    private DefaultTableModel tableModel;
    private JTextField facultyIdField, nameField, departmentField, emailField, phoneField, passwordField;
    private JComboBox<String> statusCombo;
    
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color ACCENT_COLOR = new Color(46, 204, 113);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    
    public FacultyManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        add(createTopPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createFormPanel(), BorderLayout.EAST);
        
        loadFaculty();
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("Faculty Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBackground(ACCENT_COLOR);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.addActionListener(e -> loadFaculty());
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(refreshButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        String[] columns = {"Faculty ID", "Name", "Department", "Email", "Phone", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        facultyTable = new JTable(tableModel);
        facultyTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        facultyTable.setRowHeight(30);
        facultyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        facultyTable.getTableHeader().setBackground(PRIMARY_COLOR);
        facultyTable.getTableHeader().setForeground(Color.WHITE);
        
        facultyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromTable();
        });
        
        JScrollPane scrollPane = new JScrollPane(facultyTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(320, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel formTitle = new JLabel("Faculty Details");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(PRIMARY_COLOR);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(formTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        facultyIdField = addFormField(panel, "Faculty ID:");
        nameField = addFormField(panel, "Name:");
        departmentField = addFormField(panel, "Department:");
        departmentField.setText("MCA");
        departmentField.setEditable(false);
        emailField = addFormField(panel, "Email:");
        phoneField = addFormField(panel, "Phone:");
        passwordField = addFormField(panel, "Password:");
        
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(statusLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        statusCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(statusCombo);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        panel.add(createButton("Add Faculty", ACCENT_COLOR, e -> addFaculty()));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createButton("Update Faculty", PRIMARY_COLOR, e -> updateFaculty()));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createButton("Delete Faculty", DANGER_COLOR, e -> deleteFaculty()));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createButton("Clear Form", new Color(149, 165, 166), e -> clearForm()));
        
        return panel;
    }
    
    private JTextField addFormField(JPanel panel, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(field);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        return field;
    }
    
    private JButton createButton(String text, Color bgColor, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(listener);
        return button;
    }
    
    private void loadFaculty() {
        tableModel.setRowCount(0);
        List<Faculty> facultyList = facultyDAO.getAllFaculty();
        
        for (Faculty faculty : facultyList) {
            Object[] row = {
                faculty.getFacultyId(),
                faculty.getName(),
                faculty.getDepartment(),
                faculty.getEmail(),
                faculty.getPhone(),
                faculty.getStatus()
            };
            tableModel.addRow(row);
        }
    }
    
    private void populateFormFromTable() {
        int selectedRow = facultyTable.getSelectedRow();
        if (selectedRow >= 0) {
            facultyIdField.setText(tableModel.getValueAt(selectedRow, 0).toString());
            nameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
            departmentField.setText(tableModel.getValueAt(selectedRow, 2).toString());
            emailField.setText(tableModel.getValueAt(selectedRow, 3).toString());
            phoneField.setText(tableModel.getValueAt(selectedRow, 4).toString());
            statusCombo.setSelectedItem(tableModel.getValueAt(selectedRow, 5).toString());
            facultyIdField.setEditable(false);
        }
    }
    
    private void addFaculty() {
        try {
            Faculty faculty = new Faculty(
                facultyIdField.getText().trim(),
                nameField.getText().trim(),
                departmentField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                passwordField.getText().trim(),
                (String) statusCombo.getSelectedItem()
            );
            
            if (facultyDAO.addFaculty(faculty)) {
                JOptionPane.showMessageDialog(this, "Faculty added successfully!");
                loadFaculty();
                clearForm();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    
    private void updateFaculty() {
        try {
            Faculty faculty = new Faculty(
                facultyIdField.getText().trim(),
                nameField.getText().trim(),
                departmentField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                passwordField.getText().trim(),
                (String) statusCombo.getSelectedItem()
            );
            
            if (facultyDAO.updateFaculty(faculty)) {
                JOptionPane.showMessageDialog(this, "Faculty updated successfully!");
                loadFaculty();
                clearForm();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    
    private void deleteFaculty() {
        String id = facultyIdField.getText().trim();
        if (id.isEmpty()) return;
        
        int confirm = JOptionPane.showConfirmDialog(this, "Delete faculty " + id + "?");
        if (confirm == JOptionPane.YES_OPTION && facultyDAO.deleteFaculty(id)) {
            JOptionPane.showMessageDialog(this, "Faculty deleted successfully!");
            loadFaculty();
            clearForm();
        }
    }
    
    private void clearForm() {
        facultyIdField.setText("");
        nameField.setText("");
        departmentField.setText("");
        emailField.setText("");
        phoneField.setText("");
        passwordField.setText("");
        statusCombo.setSelectedIndex(0);
        facultyIdField.setEditable(true);
        facultyTable.clearSelection();
    }
}