/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.TimeZone;

public class ConnectionProvider {

    private static final String DB_NAME = "attendancejframebd";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "Echo";

    public static Connection getCon() {
        Connection con = null;
        try {
            // Set default timezone
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL Driver loaded successfully!");
            
            // Connection URL with all necessary parameters
            String url = "jdbc:mysql://localhost:3306/" + DB_NAME 
                    + "?useSSL=false"
                    + "&serverTimezone=UTC"
                    + "&allowPublicKeyRetrieval=true"
                    + "&createDatabaseIfNotExist=true";
            
            System.out.println("Connecting to: " + url);
            
            con = DriverManager.getConnection(url, DB_USERNAME, DB_PASSWORD);
            
            System.out.println("Successfully connected to database: " + DB_NAME);
            return con;
            
        } catch (ClassNotFoundException ex) {
            System.err.println("MySQL Driver not found!");
            ex.printStackTrace();
            return null;
        } catch (SQLException ex) {
            System.err.println("SQL Exception: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        } catch (Exception ex) {
            System.err.println("Unexpected error: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }
}
    