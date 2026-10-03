package com.ticketflow.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseHelper {

    private DatabaseHelper() {
        // static-only class, never instantiated
    }

    // Closes JDBC resources in the correct order without letting a close() failure
    // mask the real exception that was already in flight.
    public static void closeQuietly(ResultSet rs, PreparedStatement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                System.err.println("Failed to close ResultSet: " + e.getMessage());
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                System.err.println("Failed to close PreparedStatement: " + e.getMessage());
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                System.err.println("Failed to close Connection: " + e.getMessage());
            }
        }
    }
}