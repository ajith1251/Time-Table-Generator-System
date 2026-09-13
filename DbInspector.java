import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbInspector {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlite:timetable.db");
            
            DatabaseMetaData meta = conn.getMetaData();
            
            System.out.println("=== DATABASE SCHEMA ===");
            
            // Get tables
            ResultSet tables = meta.getTables(null, null, null, new String[]{"TABLE"});
            System.out.println("Tables:");
            while (tables.next()) {
                System.out.println("  - " + tables.getString("TABLE_NAME"));
            }
            
            // For each table, get columns
            ResultSet tables2 = meta.getTables(null, null, null, new String[]{"TABLE"});
            while (tables2.next()) {
                String tableName = tables2.getString("TABLE_NAME");
                System.out.println("\nColumns in table '" + tableName + "':");
                ResultSet columns = meta.getColumns(null, null, tableName, null);
                while (columns.next()) {
                    System.out.println("  - " + columns.getString("COLUMN_NAME") + 
                                       " (" + columns.getString("TYPE_NAME") + ")");
                }
            }
            
            // Show some sample data
            System.out.println("\n=== SAMPLE DATA ===");
            Statement stmt = conn.createStatement();
            
            ResultSet rs = stmt.executeQuery("SELECT * FROM dept_master LIMIT 5");
            System.out.println("Departments:");
            while (rs.next()) {
                System.out.println("  Code: " + rs.getString("dept_code") + 
                                   ", Title: " + rs.getString("dept_title"));
            }
            
            rs = stmt.executeQuery("SELECT * FROM teacher_master LIMIT 5");
            System.out.println("\nTeachers:");
            while (rs.next()) {
                System.out.println("  ID: " + rs.getString("t_id") + 
                                   ", Name: " + rs.getString("t_name") +
                                   ", Dept: " + rs.getString("dept_code"));
            }
            
            rs = stmt.executeQuery("SELECT * FROM room_master LIMIT 5");
            System.out.println("\nClassrooms:");
            while (rs.next()) {
                System.out.println("  Code: " + rs.getString("room_code") + 
                                   ", Name: " + rs.getString("room_title") +
                                   ", Capacity: " + rs.getInt("room_capacity") +
                                   ", Dept: " + rs.getString("dept_code"));
            }
            
            rs = stmt.executeQuery("SELECT * FROM subject_master LIMIT 5");
            System.out.println("\nSubjects:");
            while (rs.next()) {
                System.out.println("  Code: " + rs.getString("s_code") + 
                                   ", Title: " + rs.getString("s_title") +
                                   ", Dept: " + rs.getString("dept_code") +
                                   ", Teacher: " + rs.getString("t_id") +
                                   ", Semester: " + rs.getInt("semester") +
                                   ", Hours/Week: " + rs.getInt("hours_week"));
            }
            
            rs = stmt.executeQuery("SELECT * FROM slot_master LIMIT 5");
            System.out.println("\nTimeslots:");
            while (rs.next()) {
                System.out.println("  Code: " + rs.getString("sl_code") + 
                                   ", Day: " + rs.getString("sl_day") +
                                   ", Start: " + rs.getString("start_at") +
                                   ", End: " + rs.getString("end_at"));
            }
            
            rs = stmt.executeQuery("SELECT * FROM timetable_output LIMIT 10");
            System.out.println("\nGenerated Timetable (first 10 entries):");
            while (rs.next()) {
                System.out.println("  Room: " + rs.getString("room_code") + 
                                   ", Subject: " + rs.getString("s_code") +
                                   ", Teacher: " + rs.getString("t_id") +
                                   ", Slot: " + rs.getString("sl_code") +
                                   ", Day: " + rs.getString("sl_day"));
            }
            
            conn.close();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found. Make sure sqlite-jdbc-*.jar is in classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database error:");
            e.printStackTrace();
        }
    }
}