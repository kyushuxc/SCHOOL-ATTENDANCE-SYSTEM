/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.*;
import javax.swing.JOptionPane;

public class tables {

    public static void main(String[] args) {
        Connection con = null;
        Statement st = null;
        try{
            con = ConnectionProvider.getCon();
            
           
            if (con == null) {
                JOptionPane.showMessageDialog(null, "Failed to connect to database!");
                return;
            }
            
            st = con.createStatement();
            
   
st.executeUpdate("CREATE TABLE IF NOT EXISTS student ("
    + "id BIGINT PRIMARY KEY AUTO_INCREMENT, "
    + "name VARCHAR(100) NOT NULL, "
    + "gender VARCHAR(10), "
    + "section VARCHAR(50), "
    + "adviser VARCHAR(100), "
    + "imagename VARCHAR(255)"
    + ")");


// Create studentAttendance table
st.executeUpdate("CREATE TABLE IF NOT EXISTS studentAttendance ("
    + "id BIGINT PRIMARY KEY AUTO_INCREMENT, "
    + "studentId BIGINT NOT NULL, "
    + "name VARCHAR(100) NOT NULL, "
    + "gender VARCHAR(10), "
    + "date DATE NOT NULL, "
    + "timeIn TIME NOT NULL, "
    + "section VARCHAR(50)"
    + ")");



st.executeUpdate("ALTER TABLE student MODIFY COLUMN id BIGINT AUTO_INCREMENT");
st.executeUpdate("ALTER TABLE studentAttendance MODIFY COLUMN id BIGINT AUTO_INCREMENT");
st.executeUpdate("ALTER TABLE studentAttendance MODIFY COLUMN studentId BIGINT NOT NULL");

System.out.println("StudentAttendance table created successfully!");
System.out.println("Student table created successfully!");
JOptionPane.showMessageDialog(null, "Student table created successfully!");

            
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