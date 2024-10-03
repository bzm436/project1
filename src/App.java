import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;


public class App extends JFrame {

    
    private static final String DATABASE_PATH = "jdbc:sqlite:C:\\Projects\\tools\\sqlite\\student.db";
    private JTextField studentIdField;
    private JTextField filterStudentIdField;
    private JTextField filterDateField;
    private JTextField filterStartTimeField;
    private JTextField filterEndTimeField;
    private static boolean isAdminUser = false;

    public App() {
        setTitle("SUN Lab Access System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // Swipe Badge Tab
        JPanel swipePanel = new JPanel();
        studentIdField = new JTextField(10);
        JButton enterButton = new JButton("Enter");
        JButton exitButton = new JButton("Exit");

        // Label

        JLabel jl = new JLabel("Student ID:");
       // jl.setSize(200, 10);
        swipePanel.add(jl);

        swipePanel.add(studentIdField);

        //Dimension buttonSize = new Dimension(75, 25);
        //enterButton.setPreferredSize(buttonSize);
        swipePanel.add(enterButton);

        //exitButton.setPreferredSize(buttonSize);
        swipePanel.add(exitButton);


        enterButton.addActionListener(new SwipeActionListener("entry"));
        exitButton.addActionListener(new SwipeActionListener("exit"));

        tabbedPane.addTab("Swipe Badge", swipePanel);

        // View Access Log Tab
        JPanel logPanel = new JPanel(new BorderLayout());
        JPanel filterPanel = new JPanel(new GridLayout(5, 2));

        filterPanel.add(new JLabel("Filter by Student ID:"));
        filterStudentIdField = new JTextField();
        filterPanel.add(filterStudentIdField);

        filterPanel.add(new JLabel("Filter by Date (yyyy-MM-dd):"));
        filterDateField = new JTextField();
        filterPanel.add(filterDateField);

        filterPanel.add(new JLabel("Filter by Start Time (HH:mm:ss):"));
        filterStartTimeField = new JTextField();
        filterPanel.add(filterStartTimeField);

        filterPanel.add(new JLabel("Filter by End Time (HH:mm:ss):"));
        filterEndTimeField = new JTextField();
        filterPanel.add(filterEndTimeField);

        JButton filterButton = new JButton("Show Filtered Results");
        filterButton.setBackground(Color.LIGHT_GRAY);
        filterPanel.add(filterButton);

        logPanel.add(filterPanel, BorderLayout.NORTH);
        String[] columnNames = {"Student ID","Name", "User Type", "Access Timestamp", "Event (entry/exit)"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);

        filterButton.addActionListener(e -> {
            showAccessLogs(logPanel, tableModel);
        });

        JScrollPane scrollPane = new JScrollPane(table);
        logPanel.add(scrollPane, BorderLayout.CENTER);

        tabbedPane.addChangeListener(e -> {
            if (!isAdminUser && tabbedPane.getSelectedIndex() == 1) {
                String password = JOptionPane.showInputDialog(App.this, "Enter Admin Password:");
                if (!"tarzan".equals(password)) {
                    JOptionPane.showMessageDialog(App.this, "Invalid password.");
                    tabbedPane.setSelectedIndex(0);
                }else{
                    isAdminUser = true;
                }
            }
        });

        tabbedPane.addTab("View Access Log", logPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private class SwipeActionListener implements ActionListener {
        private String eventType;

        public SwipeActionListener(String eventType) {
            this.eventType = eventType;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String studentId = studentIdField.getText();
            if (studentId.isEmpty()) {
                JOptionPane.showMessageDialog(App.this, "Please enter a student ID.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(DATABASE_PATH)) {
                String query = "SELECT * FROM Users WHERE student_id = ? AND user_type_id = 1";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, studentId);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        String insertLog = "INSERT INTO AccessLogs (user_id, event_type) VALUES (?, ?)";
                        try (PreparedStatement logPstmt = conn.prepareStatement(insertLog)) {
                            logPstmt.setInt(1, rs.getInt("student_id"));
                            logPstmt.setString(2, eventType);
                            logPstmt.executeUpdate();
                            JOptionPane.showMessageDialog(App.this, "Access " + eventType + " recorded.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(App.this, "Invalid student ID.");
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(App.this, "Database error: " + ex.getMessage());
            }
        }
    }

    private void showAccessLogs(JPanel logPanel, DefaultTableModel tableModel) {
        tableModel.setRowCount(0); // Clear previous data

        String studentId = filterStudentIdField.getText().trim();
        String date = filterDateField.getText().trim();
        String startTime = filterStartTimeField.getText().trim();
        String endTime = filterEndTimeField.getText().trim();

        StringBuilder query = new StringBuilder("SELECT AccessLogs.user_id, Users.name, UserTypes.user_type_name, AccessLogs.access_time, AccessLogs.event_type "
                + "FROM AccessLogs "
                + "JOIN Users ON AccessLogs.user_id = Users.student_id "
                + "JOIN UserTypes ON Users.user_type_id = UserTypes.user_type_id WHERE 1=1");

        if (!studentId.isEmpty()) {
            query.append(" AND Users.student_id = ? ");
        }
        if (!date.isEmpty()) {
            query.append(" AND DATE(AccessLogs.access_time) = DATE(?) ");
        }
        if (!startTime.isEmpty()) {
            query.append(" AND TIME(AccessLogs.access_time) >= TIME(?) ");
        }
        if (!endTime.isEmpty()) {
            query.append(" AND TIME(AccessLogs.access_time) <= TIME(?) ");
        }

        try (Connection conn = DriverManager.getConnection(DATABASE_PATH);
             PreparedStatement pstmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;

            if (!studentId.isEmpty()) {
                pstmt.setString(paramIndex++, studentId);
            }
            if (!date.isEmpty()) {
                pstmt.setString(paramIndex++, date);
            }
            if (!startTime.isEmpty()) {
                pstmt.setString(paramIndex++, startTime);
            }
            if (!endTime.isEmpty()) {
                pstmt.setString(paramIndex++, endTime);
            }

            ResultSet rs = pstmt.executeQuery();
            
            if (rs.isBeforeFirst()){
                while (rs.next()) {
                    Integer id = rs.getInt("user_id");
                    String name = rs.getString("name");
                    String userType = rs.getString("user_type_name");
                    Timestamp timestamp = rs.getTimestamp("access_time");
                    String event = rs.getString("event_type");
                    tableModel.addRow(new Object[]{id, name, userType, timestamp, event});
                }    
            }else{
                JOptionPane.showMessageDialog(App.this, "No matching records found.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(App.this, "Database error: " + ex.getMessage());
        }

        logPanel.revalidate();
        logPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            App app = new App();
            app.setVisible(true);
        });
    }
}
