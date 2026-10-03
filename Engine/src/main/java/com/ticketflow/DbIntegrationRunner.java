package com.ticketflow;

import com.ticketflow.db.ClaimDAO;
import com.ticketflow.db.ConnectionPool;
import com.ticketflow.db.ResourceDAO;
import com.ticketflow.exception.ResourceFullException;
import com.ticketflow.manager.BookingManager;
import com.ticketflow.model.Claim;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

// Proves the DB-backed path works end to end against a real Postgres instance.
// This is the persistence-layer equivalent of what Main.java proves in pure memory.
public class DbIntegrationRunner {

    public static void main(String[] args) {
        System.out.println("--- TicketFlow DB Integration Runner ---");

        ResourceDAO resourceDAO = new ResourceDAO();
        ClaimDAO claimDAO = new ClaimDAO();

        // This constructor loads every row from `resources` into a ConcurrentHashMap cache
        BookingManager bookingManager = new BookingManager(resourceDAO, claimDAO);

        System.out.println("\nBefore claim:");
        printResourceRow("GEAR-001");

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);

        Claim claim1 = new Claim("GEAR-001", "trapp@gmail.com", Claim.ClaimStatus.PENDING, start, end);

        System.out.println("\nAttempting Claim 1 on GEAR-001 (1 slot total)...");
        bookingManager.claimResource("GEAR-001", claim1);
        System.out.println("Success! Claim persisted with id: " + claim1.getUUID());

        System.out.println("\nAfter claim 1:");
        printResourceRow("GEAR-001");

        Claim claim2 = new Claim("GEAR-001", "alice@gmail.com", Claim.ClaimStatus.PENDING, start, end);

        System.out.println("\nAttempting Claim 2 on the now-empty GEAR-001 - this should be rejected...");
        try {
            bookingManager.claimResource("GEAR-001", claim2);
            System.out.println("ERROR: this should not have succeeded!");
        } catch (ResourceFullException e) {
            System.out.println("EXPECTED ERROR CAUGHT: " + e.getMessage());
        }

        System.out.println("\nFinal state (should be unchanged from after claim 1):");
        printResourceRow("GEAR-001");

        ConnectionPool.shutdown();
        System.out.println("\n--- Done ---");
    }

    private static void printResourceRow(String id) {
        String sql = "SELECT available_slots, total_slots FROM resources WHERE id = ?";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("  " + id + ": " + rs.getInt("available_slots")
                            + " / " + rs.getInt("total_slots") + " slots available (from DB)");
                } else {
                    System.out.println("  " + id + " not found");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read resource row", e);
        }
    }
}