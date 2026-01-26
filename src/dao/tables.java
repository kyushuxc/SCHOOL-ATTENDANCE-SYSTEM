package dao;

import java.sql.*;
import javax.swing.JOptionPane;

public class tables {

    public static void main(String[] args) {
        Connection con = null;
        Statement st = null;
        try {
            con = ConnectionProvider.getCon();

            if (con == null) {
                JOptionPane.showMessageDialog(null, "Failed to connect to database!");
                return;
            }

            st = con.createStatement();

            // Create student table
            st.executeUpdate("CREATE TABLE IF NOT EXISTS student ("
                    + "id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                    + "name VARCHAR(100) NOT NULL, "
                    + "gender VARCHAR(10), "
                    + "section VARCHAR(50), "
                    + "adviser VARCHAR(100), "
                    + "imagename VARCHAR(255)"
                    + ")");

            // Create Grade 12 attendance table
            st.executeUpdate("CREATE TABLE IF NOT EXISTS studentAttendance ("
                    + "id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                    + "studentId BIGINT NOT NULL, "
                    + "name VARCHAR(100) NOT NULL, "
                    + "gender VARCHAR(10), "
                    + "date DATE NOT NULL, "
                    + "timeIn TIME NOT NULL, "
                    + "section VARCHAR(50), "
                    + "FOREIGN KEY (studentId) REFERENCES student(id)"
                    + ")");

            // Create Grade 11 attendance table
            st.executeUpdate("CREATE TABLE IF NOT EXISTS studentAttendancee ("
                    + "id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                    + "studentId BIGINT NOT NULL, "
                    + "name VARCHAR(100) NOT NULL, "
                    + "gender VARCHAR(10), "
                    + "date DATE NOT NULL, "
                    + "timeIn TIME NOT NULL, "
                    + "section VARCHAR(50), "
                    + "FOREIGN KEY (studentId) REFERENCES student(id)"
                    + ")");

            // Create shared archive table
            st.executeUpdate("CREATE TABLE IF NOT EXISTS studentAttendanceArchive ("
                    + "id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                    + "studentId BIGINT NOT NULL, "
                    + "name VARCHAR(100) NOT NULL, "
                    + "gender VARCHAR(10), "
                    + "date DATE NOT NULL, "
                    + "timeIn TIME NOT NULL, "
                    + "section VARCHAR(50)"
                    + ")");

            // ✅ Create attendance_summary table
            st.executeUpdate("CREATE TABLE IF NOT EXISTS attendance_summary ("
                    + "id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                    + "studentId BIGINT NOT NULL, "
                    + "date DATE NOT NULL, "
                    + "status ENUM('Present','Absent') NOT NULL, "
                    + "FOREIGN KEY (studentId) REFERENCES student(id)"
                    + ")");

            // ✅ Enable event scheduler
            st.executeUpdate("SET GLOBAL event_scheduler = ON");

            // Archive Grade 12 attendance at midnight
            st.executeUpdate("DROP EVENT IF EXISTS clear_attendance_midnight");
            st.executeUpdate(
                "CREATE EVENT clear_attendance_midnight " +
                "ON SCHEDULE EVERY 1 DAY STARTS TIMESTAMP(CURRENT_DATE + INTERVAL 1 DAY) " +
                "ON COMPLETION PRESERVE " +
                "DO " +
                "BEGIN " +
                "INSERT INTO studentAttendanceArchive (studentId, name, gender, date, timeIn, section) " +
                "SELECT studentId, name, gender, date, timeIn, section FROM studentAttendance; " +
                "DELETE FROM studentAttendance; " +
                "END"
            );

            // Archive Grade 11 attendance at midnight
            st.executeUpdate("DROP EVENT IF EXISTS clear_attendancee_midnight");
            st.executeUpdate(
                "CREATE EVENT clear_attendancee_midnight " +
                "ON SCHEDULE EVERY 1 DAY STARTS TIMESTAMP(CURRENT_DATE + INTERVAL 1 DAY) " +
                "ON COMPLETION PRESERVE " +
                "DO " +
                "BEGIN " +
                "INSERT INTO studentAttendanceArchive (studentId, name, gender, date, timeIn, section) " +
                "SELECT studentId, name, gender, date, timeIn, section FROM studentAttendancee; " +
                "DELETE FROM studentAttendancee; " +
                "END"
                    
                       
            );
            
  
 
            // ✅ Daily attendance summary event
            st.executeUpdate("DROP EVENT IF EXISTS mark_daily_attendance");
            st.executeUpdate(
                "CREATE EVENT mark_daily_attendance " +
                "ON SCHEDULE EVERY 1 DAY STARTS TIMESTAMP(CURRENT_DATE + INTERVAL 1 DAY) " +
                "ON COMPLETION PRESERVE " +
                "DO " +
                "BEGIN " +
                "INSERT INTO attendance_summary (studentId, date, status) " +
                "SELECT s.id, CURDATE(), 'Absent' " +
                "FROM student s " +
                "WHERE s.id NOT IN ( " +
                "    SELECT studentId FROM studentAttendance WHERE DATE(date) = CURDATE() " +
                "    UNION " +
                "    SELECT studentId FROM studentAttendancee WHERE DATE(date) = CURDATE() " +
                "); " +
                "INSERT INTO attendance_summary (studentId, date, status) " +
                "SELECT a.studentId, CURDATE(), 'Present' " +
                "FROM ( " +
                "    SELECT studentId FROM studentAttendance WHERE DATE(date) = CURDATE() " +
                "    UNION " +
                "    SELECT studentId FROM studentAttendancee WHERE DATE(date) = CURDATE() " +
                ") a; " +
                "END"
            );

            System.out.println("All tables and scheduled events created successfully!");
            JOptionPane.showMessageDialog(null, "Setup complete!");

        } catch (SQLException ex) {
            System.err.println("SQL Error: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "SQL Error: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        } finally {
            try {
                if (st != null) st.close();
                if (con != null) con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
