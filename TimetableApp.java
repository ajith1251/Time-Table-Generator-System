import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class TimetableApp extends JFrame {

    // Main layout panels
    private JPanel contentPanel;
    private JPanel deptPage, teacherPage, roomPage, subjectPage, slotPage, generatePage;

    // Table models
    private DefaultTableModel deptModel, teacherModel, classModel, subModel, slotModel, timetableModel;

    // Shared combo boxes
    private JComboBox<String> teacherDeptCombo, classDeptCombo, subDeptCombo, subTeacherCombo;

    // Light theme colors (macOS / iOS style)
    private final Color BG = new Color(245, 246, 249);          // window background
    private final Color CARD = new Color(252, 252, 255);        // panels
    private final Color BORDER = new Color(220, 222, 230);      // light border
    private final Color TEXT = new Color(35, 38, 47);           // dark text
    private final Color MUTED = new Color(120, 124, 135);       // secondary text
    private final Color ACCENT = new Color(52, 120, 246);       // macOS blue
    private final Color BTN_BG = new Color(235, 238, 245);
    private final Color BTN_BG_HOVER = new Color(225, 230, 245);

    public TimetableApp() {
        setTitle("Automatic Timetable Generator");
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        // ---------- TOP BAR ----------
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        topBar.setBackground(CARD);

        JLabel title = new JLabel("Automatic Timetable Generator", SwingConstants.LEFT);
        title.setForeground(TEXT);
        title.setFont(new Font("SF Pro Display", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(12, 20, 8, 20));

        JLabel subtitle = new JLabel("Department • Teachers • Classrooms • Subjects • Timeslots • Auto Scheduling");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("SF Pro Text", Font.PLAIN, 12));
        subtitle.setBorder(BorderFactory.createEmptyBorder(0, 22, 10, 20));

        JPanel titleWrap = new JPanel();
        titleWrap.setLayout(new BoxLayout(titleWrap, BoxLayout.Y_AXIS));
        titleWrap.setOpaque(false);
        titleWrap.add(title);
        titleWrap.add(subtitle);

        topBar.add(titleWrap, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        // ---------- SIDEBAR ----------
        JPanel sideBar = new JPanel();
        sideBar.setLayout(new GridLayout(8, 1, 0, 6));
        sideBar.setBackground(BG);
        sideBar.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 6));
        sideBar.setPreferredSize(new Dimension(250, 0));

        addSidebarButton(sideBar, "Departments", 1);
        addSidebarButton(sideBar, "Teachers", 2);
        addSidebarButton(sideBar, "Classrooms", 3);
        addSidebarButton(sideBar, "Subjects", 4);
        addSidebarButton(sideBar, "Timeslots", 5);
        addSidebarButton(sideBar, "Generate Timetable", 6);

        add(sideBar, BorderLayout.WEST);

        // ---------- CONTENT CARD PANEL ----------
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(BG);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 16));

        deptPage = buildDepartmentPage();
        teacherPage = buildTeacherPage();
        roomPage = buildClassroomPage();
        subjectPage = buildSubjectPage();
        slotPage = buildTimeslotPage();
        generatePage = buildGeneratePage();

        contentPanel.add(deptPage, "page1");
        contentPanel.add(teacherPage, "page2");
        contentPanel.add(roomPage, "page3");
        contentPanel.add(subjectPage, "page4");
        contentPanel.add(slotPage, "page5");
        contentPanel.add(generatePage, "page6");

        add(contentPanel, BorderLayout.CENTER);

        // ---------- CREATE TABLES ----------
        createTables();

        // Pre-load combos
        loadDeptCombo(teacherDeptCombo);
        loadDeptCombo(classDeptCombo);
        loadDeptCombo(subDeptCombo);
        loadTeacherCombo(subTeacherCombo);
    }

    // ========================================================
    //  SIDEBAR BUTTONS & PAGE SWITCHING
    // ========================================================
    private void addSidebarButton(JPanel parent, String text, int pageIndex) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setForeground(TEXT);
        btn.setBackground(BG);
        btn.setFont(new Font("SF Pro Text", Font.BOLD, 15));
        btn.setBorder(new RoundedBorder(BORDER, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(10);
        btn.setMargin(new Insets(8, 18, 8, 18));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(BTN_BG);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(BG);
            }
        });

        btn.addActionListener(e -> switchPage(pageIndex));
        parent.add(btn);
    }

    private void switchPage(int pageIndex) {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "page" + pageIndex);

        if (pageIndex == 2) { // Teachers
            loadDeptCombo(teacherDeptCombo);
        } else if (pageIndex == 3) { // Classrooms
            loadDeptCombo(classDeptCombo);
        } else if (pageIndex == 4) { // Subjects
            loadDeptCombo(subDeptCombo);
            loadTeacherCombo(subTeacherCombo);
        } else if (pageIndex == 6) { // Generate
            loadOutputTimetable();
        }
    }

    // ========================================================
    //  DEPARTMENT PAGE
    // ========================================================
    private JPanel buildDepartmentPage() {
        JPanel main = createCardPanel(new BorderLayout(10, 10));

        JLabel header = makeHeaderLabel("Departments");
        main.add(header, BorderLayout.NORTH);

        JPanel center = createCardPanel(new BorderLayout(10, 10));
        main.add(center, BorderLayout.CENTER);

        // Left form
        JPanel form = createInnerCard(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(BORDER), "Department Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtCode = makeTextField();
        JTextField txtName = makeTextField();

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(makeLabel("Code"), gbc);
        gbc.gridx = 1;
        form.add(txtCode, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(makeLabel("Name"), gbc);
        gbc.gridx = 1;
        form.add(txtName, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnPanel.setOpaque(false);
        JButton btnAdd = makePrimaryButton("Add");
        JButton btnUpdate = makeButton("Update");
        JButton btnDelete = makeButton("Delete");
        JButton btnClear = makeButton("Clear");

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        form.add(btnPanel, gbc);

        center.add(form, BorderLayout.WEST);

        // Right table
        deptModel = new DefaultTableModel(new Object[]{"Code", "Name"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = makeTable(deptModel);
        JScrollPane scroll = new JScrollPane(table);
        styleScroll(scroll);

        center.add(scroll, BorderLayout.CENTER);

        loadDepartments();

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                txtCode.setText((String) deptModel.getValueAt(row, 0));
                txtName.setText((String) deptModel.getValueAt(row, 1));
            }
        });

        btnAdd.addActionListener(e -> {
            if (txtCode.getText().trim().isEmpty() || txtName.getText().trim().isEmpty()) {
                showError("Enter both department code and name.");
                return;
            }
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO dept_master(dept_code, dept_title) VALUES(?, ?)");
                ps.setString(1, txtCode.getText().trim());
                ps.setString(2, txtName.getText().trim());
                ps.executeUpdate();
                loadDepartments();
                showInfo("Department added.");
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        btnUpdate.addActionListener(e -> {
            if (txtCode.getText().trim().isEmpty()) {
                showError("Select a department to update.");
                return;
            }
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE dept_master SET dept_title=? WHERE dept_code=?");
                ps.setString(1, txtName.getText().trim());
                ps.setString(2, txtCode.getText().trim());
                ps.executeUpdate();
                loadDepartments();
                showInfo("Department updated.");
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
            if (txtCode.getText().trim().isEmpty()) {
                showError("Select a department to delete.");
                return;
            }
            int c = JOptionPane.showConfirmDialog(this,
                    "Delete department " + txtCode.getText().trim() + " ?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (c != JOptionPane.YES_OPTION) return;

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM dept_master WHERE dept_code=?");
                ps.setString(1, txtCode.getText().trim());
                ps.executeUpdate();
                loadDepartments();
                showInfo("Department deleted.");
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        btnClear.addActionListener(e -> {
            txtCode.setText("");
            txtName.setText("");
            table.clearSelection();
        });

        return main;
    }

    private void loadDepartments() {
        if (deptModel == null) return;
        deptModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT dept_code, dept_title FROM dept_master ORDER BY dept_code")) {

            while (rs.next()) {
                deptModel.addRow(new Object[]{
                        rs.getString("dept_code"),
                        rs.getString("dept_title")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ========================================================
    //  TEACHER PAGE (WITH CHECKBOX AVAILABILITY)
    // ========================================================
    private JPanel buildTeacherPage() {
        JPanel main = createCardPanel(new BorderLayout(10, 10));

        JLabel header = makeHeaderLabel("Teachers");
        main.add(header, BorderLayout.NORTH);

        JPanel center = createCardPanel(new BorderLayout(10, 10));
        main.add(center, BorderLayout.CENTER);

        JPanel form = createInnerCard(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(BORDER), "Teacher Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtId = makeTextField();
        JTextField txtName = makeTextField();
        teacherDeptCombo = makeComboBox();

        JCheckBox cbMon = makeCheckBox("Mon");
        JCheckBox cbTue = makeCheckBox("Tue");
        JCheckBox cbWed = makeCheckBox("Wed");
        JCheckBox cbThu = makeCheckBox("Thu");
        JCheckBox cbFri = makeCheckBox("Fri");

        JPanel daysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        daysPanel.setOpaque(false);
        daysPanel.add(cbMon); daysPanel.add(cbTue);
        daysPanel.add(cbWed); daysPanel.add(cbThu); daysPanel.add(cbFri);

        JCheckBox cbS1 = makeCheckBox("1");
        JCheckBox cbS2 = makeCheckBox("2");
        JCheckBox cbS3 = makeCheckBox("3");
        JCheckBox cbS4 = makeCheckBox("4");

        JPanel slotsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        slotsPanel.setOpaque(false);
        slotsPanel.add(cbS1); slotsPanel.add(cbS2);
        slotsPanel.add(cbS3); slotsPanel.add(cbS4);

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(makeLabel("Teacher ID"), gbc);
        gbc.gridx = 1;
        form.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(makeLabel("Name"), gbc);
        gbc.gridx = 1;
        form.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(makeLabel("Department"), gbc);
        gbc.gridx = 1;
        form.add(teacherDeptCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        form.add(makeLabel("Available Days"), gbc);
        gbc.gridx = 1;
        form.add(daysPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        form.add(makeLabel("Available Slots"), gbc);
        gbc.gridx = 1;
        form.add(slotsPanel, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnPanel.setOpaque(false);
        JButton btnAdd = makePrimaryButton("Add");
        JButton btnUpdate = makeButton("Update");
        JButton btnDelete = makeButton("Delete");
        JButton btnClear = makeButton("Clear");

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete); btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        form.add(btnPanel, gbc);

        center.add(form, BorderLayout.WEST);

        teacherModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Dept", "Days", "Slots"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = makeTable(teacherModel);
        JScrollPane scroll = new JScrollPane(table);
        styleScroll(scroll);
        center.add(scroll, BorderLayout.CENTER);

        loadTeachers();

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                txtId.setText((String) teacherModel.getValueAt(row, 0));
                txtName.setText((String) teacherModel.getValueAt(row, 1));
                teacherDeptCombo.setSelectedItem(teacherModel.getValueAt(row, 2));
                String days = (String) teacherModel.getValueAt(row, 3);
                String slots = (String) teacherModel.getValueAt(row, 4);
                setDayCheckboxes(days, cbMon, cbTue, cbWed, cbThu, cbFri);
                setSlotCheckboxes(slots, cbS1, cbS2, cbS3, cbS4);
            }
        });

        btnAdd.addActionListener(e -> {
            if (txtId.getText().trim().isEmpty() ||
                txtName.getText().trim().isEmpty() ||
                teacherDeptCombo.getSelectedItem() == null) {
                showError("Fill Teacher ID, Name, and Department.");
                return;
            }

            String days = buildDaysString(cbMon, cbTue, cbWed, cbThu, cbFri);
            String slots = buildSlotsString(cbS1, cbS2, cbS3, cbS4);

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO teacher_master(t_id, t_name, dept_code, avail_days, avail_slots) " +
                        "VALUES(?,?,?,?,?)");
                ps.setString(1, txtId.getText().trim());
                ps.setString(2, txtName.getText().trim());
                ps.setString(3, teacherDeptCombo.getSelectedItem().toString());
                ps.setString(4, days);
                ps.setString(5, slots);
                ps.executeUpdate();
                loadTeachers();
                showInfo("Teacher added.");
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        btnUpdate.addActionListener(e -> {
            if (txtId.getText().trim().isEmpty()) {
                showError("Select a teacher to update.");
                return;
            }

            String days = buildDaysString(cbMon, cbTue, cbWed, cbThu, cbFri);
            String slots = buildSlotsString(cbS1, cbS2, cbS3, cbS4);

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE teacher_master SET t_name=?, dept_code=?, avail_days=?, avail_slots=? WHERE t_id=?");
                ps.setString(1, txtName.getText().trim());
                ps.setString(2, teacherDeptCombo.getSelectedItem() != null
                        ? teacherDeptCombo.getSelectedItem().toString() : null);
                ps.setString(3, days);
                ps.setString(4, slots);
                ps.setString(5, txtId.getText().trim());
                ps.executeUpdate();
                loadTeachers();
                showInfo("Teacher updated.");
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
            if (txtId.getText().trim().isEmpty()) {
                showError("Select a teacher to delete.");
                return;
            }
            int c = JOptionPane.showConfirmDialog(this,
                    "Delete teacher " + txtId.getText().trim() + " ?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (c != JOptionPane.YES_OPTION) return;

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM teacher_master WHERE t_id=?");
                ps.setString(1, txtId.getText().trim());
                ps.executeUpdate();
                loadTeachers();
                showInfo("Teacher deleted.");
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        btnClear.addActionListener(e -> {
            txtId.setText("");
            txtName.setText("");
            teacherDeptCombo.setSelectedIndex(-1);
            clearDayCheckboxes(cbMon, cbTue, cbWed, cbThu, cbFri);
            clearSlotCheckboxes(cbS1, cbS2, cbS3, cbS4);
            table.clearSelection();
        });

        return main;
    }

    private void loadTeachers() {
        if (teacherModel == null) return;
        teacherModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM teacher_master ORDER BY t_id")) {

            while (rs.next()) {
                teacherModel.addRow(new Object[]{
                        rs.getString("t_id"),
                        rs.getString("t_name"),
                        rs.getString("dept_code"),
                        rs.getString("avail_days"),
                        rs.getString("avail_slots")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String buildDaysString(JCheckBox... cbs) {
        StringBuilder sb = new StringBuilder();
        for (JCheckBox cb : cbs) {
            if (cb.isSelected()) {
                sb.append(cb.getText()).append(",");
            }
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private String buildSlotsString(JCheckBox... cbs) {
        StringBuilder sb = new StringBuilder();
        for (JCheckBox cb : cbs) {
            if (cb.isSelected()) {
                sb.append(cb.getText()).append(",");
            }
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private void setDayCheckboxes(String csv, JCheckBox... cbs) {
        clearDayCheckboxes(cbs);
        if (csv == null) return;
        for (String p : csv.split(",")) {
            String trimmed = p.trim();
            for (JCheckBox cb : cbs) {
                if (cb.getText().equalsIgnoreCase(trimmed)) {
                    cb.setSelected(true);
                }
            }
        }
    }

    private void clearDayCheckboxes(JCheckBox... cbs) {
        for (JCheckBox cb : cbs) cb.setSelected(false);
    }

    private void setSlotCheckboxes(String csv, JCheckBox... cbs) {
        clearSlotCheckboxes(cbs);
        if (csv == null) return;
        for (String p : csv.split(",")) {
            String trimmed = p.trim();
            for (JCheckBox cb : cbs) {
                if (cb.getText().equals(trimmed)) {
                    cb.setSelected(true);
                }
            }
        }
    }

    private void clearSlotCheckboxes(JCheckBox... cbs) {
        for (JCheckBox cb : cbs) cb.setSelected(false);
    }

    // ========================================================
    //  CLASSROOM PAGE
    // ========================================================
    private JPanel buildClassroomPage() {
        JPanel main = createCardPanel(new BorderLayout(10, 10));

        JLabel header = makeHeaderLabel("Classrooms");
        main.add(header, BorderLayout.NORTH);

        JPanel center = createCardPanel(new BorderLayout(10, 10));
        main.add(center, BorderLayout.CENTER);

        JPanel form = createInnerCard(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(BORDER), "Classroom Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtCode = makeTextField();
        JTextField txtName = makeTextField();
        JTextField txtCap = makeTextField();
        classDeptCombo = makeComboBox();

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(makeLabel("Room Code"), gbc);
        gbc.gridx = 1;
        form.add(txtCode, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(makeLabel("Room Name"), gbc);
        gbc.gridx = 1;
        form.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(makeLabel("Capacity"), gbc);
        gbc.gridx = 1;
        form.add(txtCap, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        form.add(makeLabel("Department"), gbc);
        gbc.gridx = 1;
        form.add(classDeptCombo, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnPanel.setOpaque(false);
        JButton btnAdd = makePrimaryButton("Add");
        JButton btnUpdate = makeButton("Update");
        JButton btnDelete = makeButton("Delete");
        JButton btnClear = makeButton("Clear");

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete); btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        form.add(btnPanel, gbc);

        center.add(form, BorderLayout.WEST);

        classModel = new DefaultTableModel(
                new Object[]{"Code", "Name", "Capacity", "Dept"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = makeTable(classModel);
        JScrollPane scroll = new JScrollPane(table);
        styleScroll(scroll);
        center.add(scroll, BorderLayout.CENTER);

        loadClassrooms();

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                txtCode.setText((String) classModel.getValueAt(row, 0));
                txtName.setText((String) classModel.getValueAt(row, 1));
                txtCap.setText(classModel.getValueAt(row, 2).toString());
                classDeptCombo.setSelectedItem(classModel.getValueAt(row, 3));
            }
        });

        btnAdd.addActionListener(e -> {
            try {
                int cap = Integer.parseInt(txtCap.getText().trim());
                if (txtCode.getText().trim().isEmpty()
                        || txtName.getText().trim().isEmpty()
                        || classDeptCombo.getSelectedItem() == null) {
                    showError("Fill all fields.");
                    return;
                }

                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO room_master(room_code, room_title, room_capacity, dept_code)" +
                            " VALUES(?,?,?,?)");
                    ps.setString(1, txtCode.getText().trim());
                    ps.setString(2, txtName.getText().trim());
                    ps.setInt(3, cap);
                    ps.setString(4, classDeptCombo.getSelectedItem().toString());
                    ps.executeUpdate();
                    loadClassrooms();
                    showInfo("Classroom added.");
                }

            } catch (NumberFormatException ex) {
                showError("Capacity must be a number.");
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        btnUpdate.addActionListener(e -> {
            if (txtCode.getText().trim().isEmpty()) {
                showError("Select a classroom to update.");
                return;
            }
            try {
                int cap = Integer.parseInt(txtCap.getText().trim());
                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(
                            "UPDATE room_master SET room_title=?, room_capacity=?, dept_code=? WHERE room_code=?");
                    ps.setString(1, txtName.getText().trim());
                    ps.setInt(2, cap);
                    ps.setString(3, classDeptCombo.getSelectedItem() != null
                            ? classDeptCombo.getSelectedItem().toString() : null);
                    ps.setString(4, txtCode.getText().trim());
                    ps.executeUpdate();
                    loadClassrooms();
                    showInfo("Classroom updated.");
                }
            } catch (NumberFormatException ex) {
                showError("Capacity must be a number.");
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
            if (txtCode.getText().trim().isEmpty()) {
                showError("Select a classroom to delete.");
                return;
            }
            int c = JOptionPane.showConfirmDialog(this,
                    "Delete classroom " + txtCode.getText().trim() + " ?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (c != JOptionPane.YES_OPTION) return;

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM room_master WHERE room_code=?");
                ps.setString(1, txtCode.getText().trim());
                ps.executeUpdate();
                loadClassrooms();
                showInfo("Classroom deleted.");
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        btnClear.addActionListener(e -> {
            txtCode.setText("");
            txtName.setText("");
            txtCap.setText("");
            classDeptCombo.setSelectedIndex(-1);
            table.clearSelection();
        });

        return main;
    }

    private void loadClassrooms() {
        if (classModel == null) return;
        classModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM room_master ORDER BY room_code")) {

            while (rs.next()) {
                classModel.addRow(new Object[]{
                        rs.getString("room_code"),
                        rs.getString("room_title"),
                        rs.getInt("room_capacity"),
                        rs.getString("dept_code")
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ========================================================
    //  SUBJECT PAGE
    // ========================================================
    private JPanel buildSubjectPage() {
        JPanel main = createCardPanel(new BorderLayout(10, 10));

        JLabel header = makeHeaderLabel("Subjects");
        main.add(header, BorderLayout.NORTH);

        JPanel center = createCardPanel(new BorderLayout(10, 10));
        main.add(center, BorderLayout.CENTER);

        JPanel form = createInnerCard(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(BORDER), "Subject Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtCode = makeTextField();
        JTextField txtName = makeTextField();
        subDeptCombo = makeComboBox();
        subTeacherCombo = makeComboBox();
        JTextField txtSem = makeTextField();
        JTextField txtHours = makeTextField();

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(makeLabel("Code"), gbc);
        gbc.gridx = 1;
        form.add(txtCode, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(makeLabel("Name"), gbc);
        gbc.gridx = 1;
        form.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(makeLabel("Department"), gbc);
        gbc.gridx = 1;
        form.add(subDeptCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        form.add(makeLabel("Teacher"), gbc);
        gbc.gridx = 1;
        form.add(subTeacherCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        form.add(makeLabel("Semester"), gbc);
        gbc.gridx = 1;
        form.add(txtSem, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        form.add(makeLabel("Hours / Week"), gbc);
        gbc.gridx = 1;
        form.add(txtHours, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnPanel.setOpaque(false);
        JButton btnAdd = makePrimaryButton("Add");
        JButton btnUpdate = makeButton("Update");
        JButton btnDelete = makeButton("Delete");
        JButton btnClear = makeButton("Clear");

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete); btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        form.add(btnPanel, gbc);

        center.add(form, BorderLayout.WEST);

        subModel = new DefaultTableModel(
                new Object[]{"Code", "Name", "Dept", "Teacher", "Sem", "Hours"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = makeTable(subModel);
        JScrollPane scroll = new JScrollPane(table);
        styleScroll(scroll);
        center.add(scroll, BorderLayout.CENTER);

        loadSubjects();

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                txtCode.setText((String) subModel.getValueAt(row, 0));
                txtName.setText((String) subModel.getValueAt(row, 1));
                subDeptCombo.setSelectedItem(subModel.getValueAt(row, 2));
                subTeacherCombo.setSelectedItem(subModel.getValueAt(row, 3));
                txtSem.setText(subModel.getValueAt(row, 4).toString());
                txtHours.setText(subModel.getValueAt(row, 5).toString());
            }
        });

        btnAdd.addActionListener(e -> {
            if (txtCode.getText().trim().isEmpty()
                    || txtName.getText().trim().isEmpty()
                    || subDeptCombo.getSelectedItem() == null
                    || subTeacherCombo.getSelectedItem() == null
                    || txtSem.getText().trim().isEmpty()
                    || txtHours.getText().trim().isEmpty()) {
                showError("Fill all fields.");
                return;
            }
            try {
                int sem = Integer.parseInt(txtSem.getText().trim());
                int hours = Integer.parseInt(txtHours.getText().trim());

                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO subject_master(s_code, s_title, dept_code, t_id, semester, hours_week) " +
                            "VALUES(?,?,?,?,?,?)");
                    ps.setString(1, txtCode.getText().trim());
                    ps.setString(2, txtName.getText().trim());
                    ps.setString(3, subDeptCombo.getSelectedItem().toString());
                    ps.setString(4, subTeacherCombo.getSelectedItem().toString());
                    ps.setInt(5, sem);
                    ps.setInt(6, hours);
                    ps.executeUpdate();
                    loadSubjects();
                    showInfo("Subject added.");
                }

            } catch (NumberFormatException ex) {
                showError("Semester and Hours must be numbers.");
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        btnUpdate.addActionListener(e -> {
            if (txtCode.getText().trim().isEmpty()) {
                showError("Select a subject to update.");
                return;
            }
            try {
                int sem = Integer.parseInt(txtSem.getText().trim());
                int hours = Integer.parseInt(txtHours.getText().trim());

                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(
                            "UPDATE subject_master SET s_title=?, dept_code=?, t_id=?, semester=?, hours_week=? " +
                            "WHERE s_code=?");
                    ps.setString(1, txtName.getText().trim());
                    ps.setString(2, subDeptCombo.getSelectedItem() != null
                            ? subDeptCombo.getSelectedItem().toString() : null);
                    ps.setString(3, subTeacherCombo.getSelectedItem() != null
                            ? subTeacherCombo.getSelectedItem().toString() : null);
                    ps.setInt(4, sem);
                    ps.setInt(5, hours);
                    ps.setString(6, txtCode.getText().trim());
                    ps.executeUpdate();
                    loadSubjects();
                    showInfo("Subject updated.");
                }

            } catch (NumberFormatException ex) {
                showError("Semester and Hours must be numbers.");
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
            if (txtCode.getText().trim().isEmpty()) {
                showError("Select a subject to delete.");
                return;
            }
            int c = JOptionPane.showConfirmDialog(this,
                    "Delete subject " + txtCode.getText().trim() + " ?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (c != JOptionPane.YES_OPTION) return;

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM subject_master WHERE s_code=?");
                ps.setString(1, txtCode.getText().trim());
                ps.executeUpdate();
                loadSubjects();
                showInfo("Subject deleted.");
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        btnClear.addActionListener(e -> {
            txtCode.setText("");
            txtName.setText("");
            txtSem.setText("");
            txtHours.setText("");
            subDeptCombo.setSelectedIndex(-1);
            subTeacherCombo.setSelectedIndex(-1);
            table.clearSelection();
        });

        return main;
    }

    private void loadSubjects() {
        if (subModel == null) return;
        subModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM subject_master ORDER BY s_code")) {

            while (rs.next()) {
                subModel.addRow(new Object[]{
                        rs.getString("s_code"),
                        rs.getString("s_title"),
                        rs.getString("dept_code"),
                        rs.getString("t_id"),
                        rs.getInt("semester"),
                        rs.getInt("hours_week")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ========================================================
    //  TIMESLOT PAGE
    // ========================================================
    private JPanel buildTimeslotPage() {
        JPanel main = createCardPanel(new BorderLayout(10, 10));

        JLabel header = makeHeaderLabel("Timeslots");
        main.add(header, BorderLayout.NORTH);

        JPanel center = createCardPanel(new BorderLayout(10, 10));
        main.add(center, BorderLayout.CENTER);

        JPanel form = createInnerCard(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(BORDER), "Timeslot Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtCode = makeTextField();
        JTextField txtDay = makeTextField();
        JTextField txtStart = makeTextField();
        JTextField txtEnd = makeTextField();

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(makeLabel("Slot Code"), gbc);
        gbc.gridx = 1;
        form.add(txtCode, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(makeLabel("Day (e.g., Monday)"), gbc);
        gbc.gridx = 1;
        form.add(txtDay, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(makeLabel("Start Time (HH:MM)"), gbc);
        gbc.gridx = 1;
        form.add(txtStart, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        form.add(makeLabel("End Time (HH:MM)"), gbc);
        gbc.gridx = 1;
        form.add(txtEnd, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnPanel.setOpaque(false);
        JButton btnAdd = makePrimaryButton("Add");
        JButton btnUpdate = makeButton("Update");
        JButton btnDelete = makeButton("Delete");
        JButton btnClear = makeButton("Clear");

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete); btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        form.add(btnPanel, gbc);

        center.add(form, BorderLayout.WEST);

        slotModel = new DefaultTableModel(
                new Object[]{"Code", "Day", "Start", "End"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = makeTable(slotModel);
        JScrollPane scroll = new JScrollPane(table);
        styleScroll(scroll);
        center.add(scroll, BorderLayout.CENTER);

        loadSlots();

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                txtCode.setText((String) slotModel.getValueAt(row, 0));
                txtDay.setText((String) slotModel.getValueAt(row, 1));
                txtStart.setText((String) slotModel.getValueAt(row, 2));
                txtEnd.setText((String) slotModel.getValueAt(row, 3));
            }
        });

        btnAdd.addActionListener(e -> {
            if (txtCode.getText().trim().isEmpty()
                    || txtDay.getText().trim().isEmpty()
                    || txtStart.getText().trim().isEmpty()
                    || txtEnd.getText().trim().isEmpty()) {
                showError("Fill all fields.");
                return;
            }
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO slot_master(sl_code, sl_day, start_at, end_at) VALUES(?,?,?,?)");
                ps.setString(1, txtCode.getText().trim());
                ps.setString(2, txtDay.getText().trim());
                ps.setString(3, txtStart.getText().trim());
                ps.setString(4, txtEnd.getText().trim());
                ps.executeUpdate();
                loadSlots();
                showInfo("Timeslot added.");
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        btnUpdate.addActionListener(e -> {
            if (txtCode.getText().trim().isEmpty()) {
                showError("Select a timeslot to update.");
                return;
            }
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE slot_master SET sl_day=?, start_at=?, end_at=? WHERE sl_code=?");
                ps.setString(1, txtDay.getText().trim());
                ps.setString(2, txtStart.getText().trim());
                ps.setString(3, txtEnd.getText().trim());
                ps.setString(4, txtCode.getText().trim());
                ps.executeUpdate();
                loadSlots();
                showInfo("Timeslot updated.");
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
            if (txtCode.getText().trim().isEmpty()) {
                showError("Select a timeslot to delete.");
                return;
            }
            int c = JOptionPane.showConfirmDialog(this,
                    "Delete timeslot " + txtCode.getText().trim() + " ?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (c != JOptionPane.YES_OPTION) return;

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM slot_master WHERE sl_code=?");
                ps.setString(1, txtCode.getText().trim());
                ps.executeUpdate();
                loadSlots();
                showInfo("Timeslot deleted.");
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        btnClear.addActionListener(e -> {
            txtCode.setText("");
            txtDay.setText("");
            txtStart.setText("");
            txtEnd.setText("");
            table.clearSelection();
        });

        return main;
    }

    private void loadSlots() {
        if (slotModel == null) return;
        slotModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM slot_master ORDER BY sl_code")) {

            while (rs.next()) {
                slotModel.addRow(new Object[]{
                        rs.getString("sl_code"),
                        rs.getString("sl_day"),
                        rs.getString("start_at"),
                        rs.getString("end_at")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ========================================================
    //  GENERATE TIMETABLE PAGE
    // ========================================================
    private JPanel buildGeneratePage() {
        JPanel main = createCardPanel(new BorderLayout(10, 10));

        JLabel header = makeHeaderLabel("Generate Timetable");
        main.add(header, BorderLayout.NORTH);

        timetableModel = new DefaultTableModel(
                new Object[]{"Room", "Subject", "Teacher", "Slot", "Day"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = makeTable(timetableModel);
        JScrollPane scroll = new JScrollPane(table);
        styleScroll(scroll);
        main.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setOpaque(false);
        JButton btnGenerate = makePrimaryButton("Generate Timetable");
        JButton btnExportCSV = makeButton("Export CSV");
        JButton btnExportHTML = makeButton("Export HTML");
        bottom.add(btnGenerate);
        bottom.add(btnExportCSV);
        bottom.add(btnExportHTML);
        main.add(bottom, BorderLayout.SOUTH);

        btnGenerate.addActionListener(e -> {
            generateFinalTimetable();
            loadOutputTimetable();
        });
        
        btnExportCSV.addActionListener(e -> {
            exportTimetableToCSV();
        });
        
        btnExportHTML.addActionListener(e -> {
            exportTimetableToHTML();
        });

        return main;
    }

    private void loadOutputTimetable() {
        if (timetableModel == null) return;
        timetableModel.setRowCount(0);

        String sql = "SELECT room_code, s_code, t_id, sl_code, sl_day FROM timetable_output " +
                     "ORDER BY room_code, sl_day";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                timetableModel.addRow(new Object[]{
                        rs.getString("room_code"),
                        rs.getString("s_code"),
                        rs.getString("t_id"),
                        rs.getString("sl_code"),
                        rs.getString("sl_day")
                });
            }

} catch (SQLException ex) {
         ex.printStackTrace();
     }
 }

     // ========================================================
     //  REPORT GENERATION METHODS
     // ========================================================
     
     private void exportTimetableToCSV() {
         String sql = "SELECT room_code AS Room, s_code AS Subject, t_id AS Teacher, " +
                      "sl_code AS Slot, sl_day AS Day FROM timetable_output " +
                      "ORDER BY room_code, sl_day";
        
         try (Connection conn = DBConnection.getConnection();
              PreparedStatement ps = conn.prepareStatement(sql);
              ResultSet rs = ps.executeQuery()) {
                 
             // Get file path from user
             javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
             fc.setDialogTitle("Save Timetable Report as CSV");
             int userSelection = fc.showSaveDialog(this);
             
             if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
                 java.io.File fileToSave = fc.getSelectedFile();
                 // Ensure .csv extension
                 if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                     fileToSave = new java.io.File(fileToSave.getParentFile(), 
                                                   fileToSave.getName() + ".csv");
                 }
                 
                 try (java.io.BufferedWriter writer = 
                      new java.io.BufferedWriter(new java.io.FileWriter(fileToSave))) {
                     
                     // Write header
                     writer.write("Room,Subject,Teacher,Slot,Day");
                     writer.newLine();
                     
                     // Write data
                     while (rs.next()) {
                         writer.write(String.format("%s,%s,%s,%s,%s",
                             rs.getString("Room"),
                             rs.getString("Subject"),
                             rs.getString("Teacher"),
                             rs.getString("Slot"),
                             rs.getString("Day")));
                         writer.newLine();
                     }
                     
                     showInfo("Timetable exported successfully to:\n" + fileToSave.getAbsolutePath());
                 }
             }
         } catch (SQLException ex) {
             showError("Database error while exporting timetable: " + ex.getMessage());
             ex.printStackTrace();
         } catch (java.io.IOException ex) {
             showError("Error writing CSV file: " + ex.getMessage());
             ex.printStackTrace();
         }
     }
     
     private void exportTimetableToHTML() {
         String sql = "SELECT room_code AS Room, s_code AS Subject, t_id AS Teacher, " +
                      "sl_code AS Slot, sl_day AS Day FROM timetable_output " +
                      "ORDER BY room_code, sl_day";
        
         try (Connection conn = DBConnection.getConnection();
              PreparedStatement ps = conn.prepareStatement(sql);
              ResultSet rs = ps.executeQuery()) {
                 
             // Get file path from user
             javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
             fc.setDialogTitle("Save Timetable Report as HTML");
             int userSelection = fc.showSaveDialog(this);
             
             if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
                 java.io.File fileToSave = fc.getSelectedFile();
                 // Ensure .html extension
                 if (!fileToSave.getName().toLowerCase().endsWith(".html")) {
                     fileToSave = new java.io.File(fileToSave.getParentFile(), 
                                                   fileToSave.getName() + ".html");
                 }
                 
                 try (java.io.BufferedWriter writer = 
                      new java.io.BufferedWriter(new java.io.FileWriter(fileToSave))) {
                     
                     // Write HTML header
                     writer.write("<!DOCTYPE html>");
                     writer.newLine();
                     writer.write("<html>");
                     writer.newLine();
                     writer.write("<head>");
                     writer.newLine();
                     writer.write("  <meta charset=\"UTF-8\">");
                     writer.newLine();
                     writer.write("  <title>Timetable Report</title>");
                     writer.newLine();
                     writer.write("  <style>");
                     writer.newLine();
                     writer.write("    body { font-family: Arial, sans-serif; margin: 20px; }");
                     writer.newLine();
                     writer.write("    h1 { color: #2c3e50; text-align: center; }");
                     writer.newLine();
                     writer.write("    table { border-collapse: collapse; width: 100%; margin-top: 20px; }");
                     writer.newLine();
                     writer.write("    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
                     writer.newLine();
                     writer.write("    th { background-color: #f2f2f2; }");
                     writer.newLine();
                     writer.write("    tr:nth-child(even) { background-color: #f9f9f9; }");
                     writer.newLine();
                     writer.write("  </style>");
                     writer.newLine();
                     writer.write("</head>");
                     writer.newLine();
                     writer.write("<body>");
                     writer.newLine();
                     writer.write("  <h1>Timetable Report</h1>");
                     writer.newLine();
                     writer.write("  <table>");
                     writer.newLine();
                     writer.write("    <thead>");
                     writer.newLine();
                     writer.write("      <tr>");
                     writer.newLine();
                     writer.write("        <th>Room</th>");
                     writer.newLine();
                     writer.write("        <th>Subject</th>");
                     writer.newLine();
                     writer.write("        <th>Teacher</th>");
                     writer.newLine();
                     writer.write("        <th>Slot</th>");
                     writer.newLine();
                     writer.write("        <th>Day</th>");
                     writer.newLine();
                     writer.write("      </tr>");
                     writer.newLine();
                     writer.write("    </thead>");
                     writer.newLine();
                     writer.write("    <tbody>");
                     writer.newLine();
                     
                     // Write data rows
                     while (rs.next()) {
                         writer.write("      <tr>");
                         writer.newLine();
                         writer.write("        <td>" + escapeHTML(rs.getString("Room")) + "</td>");
                         writer.newLine();
                         writer.write("        <td>" + escapeHTML(rs.getString("Subject")) + "</td>");
                         writer.newLine();
                         writer.write("        <td>" + escapeHTML(rs.getString("Teacher")) + "</td>");
                         writer.newLine();
                         writer.write("        <td>" + escapeHTML(rs.getString("Slot")) + "</td>");
                         writer.newLine();
                         writer.write("        <td>" + escapeHTML(rs.getString("Day")) + "</td>");
                         writer.newLine();
                         writer.write("      </tr>");
                         writer.newLine();
                     }
                     
                     // Write HTML footer
                     writer.write("    </tbody>");
                     writer.newLine();
                     writer.write("  </table>");
                     writer.newLine();
                     writer.write("</body>");
                     writer.newLine();
                     writer.write("</html>");
                     writer.newLine();
                     
                     showInfo("Timetable exported successfully to:\n" + fileToSave.getAbsolutePath());
                 }
             }
         } catch (SQLException ex) {
             showError("Database error while exporting timetable: " + ex.getMessage());
             ex.printStackTrace();
         } catch (java.io.IOException ex) {
             showError("Error writing HTML file: " + ex.getMessage());
             ex.printStackTrace();
         }
}
       
private String escapeHTML(String text) {
         // HTML escaping method (fixed)
         if (text == null) return "";
         return text.replace("&", "&" + "amp" + ";")
                    .replace("<", "&" + "lt" + ";")
                    .replace(">", "&" + "gt" + ";")
                   .replace("\"", "&quot" + ";")
                   .replace("'", "&#x27" + ";");
}
 
// ========================================================
     //  GENERATE TIMETABLE
     // ========================================================
     private void generateFinalTimetable() {
        try (Connection conn = DBConnection.getConnection()) {

             conn.createStatement().execute("DELETE FROM timetable_output;");

             List<String> rooms = new ArrayList<>();
             List<String[]> subjects = new ArrayList<>();
            List<String[]> slots = new ArrayList<>();

            ResultSet rsRooms = conn.createStatement().executeQuery(
                    "SELECT room_code FROM room_master ORDER BY room_code");
            while (rsRooms.next()) rooms.add(rsRooms.getString(1));

            ResultSet rsSub = conn.createStatement().executeQuery(
                    "SELECT s_code, t_id FROM subject_master ORDER BY s_code");
            while (rsSub.next()) {
                subjects.add(new String[]{rsSub.getString("s_code"), rsSub.getString("t_id")});
            }

            ResultSet rsSlots = conn.createStatement().executeQuery(
                    "SELECT sl_code, sl_day FROM slot_master ORDER BY sl_day, sl_code");
            while (rsSlots.next()) {
                slots.add(new String[]{rsSlots.getString("sl_code"), rsSlots.getString("sl_day")});
            }

            if (rooms.isEmpty() || subjects.isEmpty() || slots.isEmpty()) {
                showError("Please ensure at least 1 Room, 1 Subject and 1 Timeslot exist.");
                return;
            }

            String sql = "INSERT INTO timetable_output(room_code, s_code, t_id, sl_code, sl_day)" +
                         " VALUES(?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            int index = 0;
            for (String[] s : subjects) {
                if (index >= slots.size()) index = 0;

                String[] slot = slots.get(index);
                String room = rooms.get(index % rooms.size());

                ps.setString(1, room);
                ps.setString(2, s[0]);
                ps.setString(3, s[1]);
                ps.setString(4, slot[0]);
                ps.setString(5, slot[1]);
                ps.executeUpdate();

                index++;
            }

            showInfo("Timetable generated successfully.");

        } catch (SQLException ex) {
            showError(ex.getMessage());
        }
    }

    // ========================================================
    //  COMBO LOADERS
    // ========================================================
    private void loadDeptCombo(JComboBox<String> combo) {
        if (combo == null) return;
        combo.removeAllItems();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT dept_code FROM dept_master ORDER BY dept_code")) {
            while (rs.next()) {
                combo.addItem(rs.getString("dept_code"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        combo.setSelectedIndex(-1);
    }

    private void loadTeacherCombo(JComboBox<String> combo) {
        if (combo == null) return;
        combo.removeAllItems();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT t_id FROM teacher_master ORDER BY t_id")) {
            while (rs.next()) {
                combo.addItem(rs.getString("t_id"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        combo.setSelectedIndex(-1);
    }

    // ========================================================
    //  CREATE TABLES
    // ========================================================
    private void createTables() {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {

            st.execute("CREATE TABLE IF NOT EXISTS dept_master (" +
                    "dept_code TEXT PRIMARY KEY," +
                    "dept_title TEXT)");

            st.execute("CREATE TABLE IF NOT EXISTS teacher_master (" +
                    "t_id TEXT PRIMARY KEY," +
                    "t_name TEXT," +
                    "dept_code TEXT," +
                    "avail_days TEXT," +
                    "avail_slots TEXT)");

            st.execute("CREATE TABLE IF NOT EXISTS room_master (" +
                    "room_code TEXT PRIMARY KEY," +
                    "room_title TEXT," +
                    "room_capacity INTEGER," +
                    "dept_code TEXT)");

            st.execute("CREATE TABLE IF NOT EXISTS subject_master (" +
                    "s_code TEXT PRIMARY KEY," +
                    "s_title TEXT," +
                    "dept_code TEXT," +
                    "t_id TEXT," +
                    "semester INTEGER," +
                    "hours_week INTEGER)");

            st.execute("CREATE TABLE IF NOT EXISTS slot_master (" +
                    "sl_code TEXT PRIMARY KEY," +
                    "sl_day TEXT," +
                    "start_at TEXT," +
                    "end_at TEXT)");

            st.execute("CREATE TABLE IF NOT EXISTS timetable_output (" +
                    "code INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "room_code TEXT," +
                    "s_code TEXT," +
                    "t_id TEXT," +
                    "sl_code TEXT," +
                    "sl_day TEXT)");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ========================================================
    //  LIGHT THEME UI HELPERS
    // ========================================================
    private JPanel createCardPanel(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(BG);
        return p;
    }

    private JPanel createInnerCard(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(CARD);
        p.setBorder(new RoundedBorder(BORDER, 16));
        return p;
    }

    private JLabel makeHeaderLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.LEFT);
        l.setForeground(TEXT);
        l.setFont(new Font("SF Pro Display", Font.BOLD, 22));
        l.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        return l;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MUTED);
        l.setFont(new Font("SF Pro Text", Font.PLAIN, 13));
        return l;
    }

    private JTextField makeTextField() {
        JTextField t = new JTextField(18);
        t.setBackground(Color.WHITE);
        t.setForeground(TEXT);
        t.setCaretColor(TEXT);
        t.setBorder(new RoundedBorder(BORDER, 10));
        t.setFont(new Font("SF Pro Text", Font.PLAIN, 13));
        return t;
    }

    private JButton makeButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(BTN_BG);
        b.setForeground(TEXT);
        b.setFont(new Font("SF Pro Text", Font.BOLD, 13));
        b.setBorder(new RoundedBorder(BORDER, 14));
        b.setMargin(new Insets(6, 14, 6, 14));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(BTN_BG_HOVER); }
            public void mouseExited(MouseEvent e) { b.setBackground(BTN_BG); }
        });
        return b;
    }

    private JButton makePrimaryButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SF Pro Text", Font.BOLD, 13));
        b.setBorder(new RoundedBorder(ACCENT.darker(), 16));
        b.setMargin(new Insets(7, 18, 7, 18));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(ACCENT.darker()); }
            public void mouseExited(MouseEvent e) { b.setBackground(ACCENT); }
        });
        return b;
    }

    private JComboBox<String> makeComboBox() {
        JComboBox<String> c = new JComboBox<>();
        c.setBackground(Color.WHITE);
        c.setForeground(TEXT);
        c.setBorder(new RoundedBorder(BORDER, 10));
        c.setFont(new Font("SF Pro Text", Font.PLAIN, 13));
        return c;
    }

    private JCheckBox makeCheckBox(String text) {
        JCheckBox cb = new JCheckBox(text);
        cb.setBackground(CARD);
        cb.setForeground(TEXT);
        cb.setFont(new Font("SF Pro Text", Font.PLAIN, 12));
        return cb;
    }

    private JTable makeTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setBackground(Color.WHITE);
        t.setForeground(TEXT);
        t.setGridColor(BORDER);
        t.setRowHeight(24);
        t.setFont(new Font("SF Pro Text", Font.PLAIN, 13));
        t.setSelectionBackground(new Color(220, 230, 255));
        t.setSelectionForeground(TEXT);
        t.getTableHeader().setBackground(new Color(246, 247, 250));
        t.getTableHeader().setForeground(MUTED);
        t.getTableHeader().setFont(new Font("SF Pro Text", Font.BOLD, 13));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        return t;
    }

    private void styleScroll(JScrollPane sp) {
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(new RoundedBorder(BORDER, 14));
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    // ========================================================
    //  ROUNDED BORDER CLASS
    // ========================================================
    static class RoundedBorder extends LineBorder {
        private final int radius;

        public RoundedBorder(Color color, int radius) {
            super(color, 1, true);
            this.radius = radius;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(6, 10, 6, 10);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(lineColor);
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawRoundRect(x + 1, y + 1, width - 3, height - 3, radius, radius);
        }
    }

    // ========================================================
    //  MAIN
    // ========================================================
    public static void main(String[] args) {
        try {
            // Use system look and feel for more native look
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new TimetableApp().setVisible(true));
    }  // Added comment
}
