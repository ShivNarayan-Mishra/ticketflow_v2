package com.ticketflow;

import com.ticketflow.db.ClaimDAO;
import com.ticketflow.db.ConnectionPool;
import com.ticketflow.db.ResourceDAO;
import com.ticketflow.exception.ResourceFullException;
import com.ticketflow.manager.BookingManager;
import com.ticketflow.model.Claim;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DbConcurrencyStressRunner {

    private static final String GEAR_ID = "GEAR-001"; // 1 total slot
    private static final String ROOM_ID = "ROOM-101"; // 2 total slots
    private static final int THREADS_PER_RESOURCE = 50;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- DB-Backed Concurrency Stress Test (Gear + Room) ---");

        resetSlots();

        ResourceDAO resourceDAO = new ResourceDAO();
        ClaimDAO claimDAO = new ClaimDAO();
        BookingManager bookingManager = new BookingManager(resourceDAO, claimDAO);

        int totalThreads = THREADS_PER_RESOURCE * 2;
        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger gearSuccess = new AtomicInteger(0);
        AtomicInteger gearFailure = new AtomicInteger(0);
        AtomicInteger roomSuccess = new AtomicInteger(0);
        AtomicInteger roomFailure = new AtomicInteger(0);

        long start = System.currentTimeMillis();

        submitWorkers(executor, latch, bookingManager, GEAR_ID, THREADS_PER_RESOURCE, gearSuccess, gearFailure);
        submitWorkers(executor, latch, bookingManager, ROOM_ID, THREADS_PER_RESOURCE, roomSuccess, roomFailure);

        latch.countDown(); // fire all threads across both resources at once
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        long elapsedMs = System.currentTimeMillis() - start;

        System.out.println("\n--- GEAR-001 (GearResource, 1 slot) ---");
        System.out.println("Successes: " + gearSuccess.get() + " (expected: 1)");
        System.out.println("Failures:  " + gearFailure.get() + " (expected: 49)");

        System.out.println("\n--- ROOM-101 (RoomResource, 2 slots) ---");
        System.out.println("Successes: " + roomSuccess.get() + " (expected: 2)");
        System.out.println("Failures:  " + roomFailure.get() + " (expected: 48)");

        System.out.println("\nElapsed: " + elapsedMs + " ms (100 threads total, both resources contended simultaneously)");

        int gearClaimRows = countClaimRowsForResource(GEAR_ID);
        int gearSlots = getAvailableSlots(GEAR_ID);
        int roomClaimRows = countClaimRowsForResource(ROOM_ID);
        int roomSlots = getAvailableSlots(ROOM_ID);

        System.out.println("\nIndependently verified against Postgres (bypassing the DAO):");
        System.out.println("  GEAR-001 claims rows: " + gearClaimRows + " (expected: 1), available_slots: " + gearSlots + " (expected: 0)");
        System.out.println("  ROOM-101 claims rows: " + roomClaimRows + " (expected: 2), available_slots: " + roomSlots + " (expected: 0)");

        boolean pass = gearSuccess.get() == 1 && gearFailure.get() == 49
                && roomSuccess.get() == 2 && roomFailure.get() == 48
                && gearClaimRows == 1 && gearSlots == 0
                && roomClaimRows == 2 && roomSlots == 0;

        System.out.println("\n" + (pass ? "PASS - guarantee holds across resource types under concurrent DB writes" : "FAIL - investigate"));

        ConnectionPool.shutdown();
    }

    private static void submitWorkers(ExecutorService executor, CountDownLatch latch, BookingManager bookingManager,
                                      String resourceId, int count, AtomicInteger successCount, AtomicInteger failureCount) {
        for (int i = 0; i < count; i++) {
            final String email = resourceId + "-user" + i + "@test.com";
            executor.submit(() -> {
                try {
                    latch.await();
                    Claim claim = new Claim(
                            resourceId, email, Claim.ClaimStatus.PENDING,
                            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2)
                    );
                    bookingManager.claimResource(resourceId, claim);
                    successCount.incrementAndGet();
                } catch (ResourceFullException e) {
                    failureCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    private static void resetSlots() {
        try (Connection conn = ConnectionPool.getConnection()) {
            try (PreparedStatement clearClaims = conn.prepareStatement(
                    "DELETE FROM claims WHERE resource_id IN (?, ?)")) {
                clearClaims.setString(1, GEAR_ID);
                clearClaims.setString(2, ROOM_ID);
                clearClaims.executeUpdate();
            }
            try (PreparedStatement resetGear = conn.prepareStatement(
                    "UPDATE resources SET available_slots = 1 WHERE id = ?")) {
                resetGear.setString(1, GEAR_ID);
                resetGear.executeUpdate();
            }
            try (PreparedStatement resetRoom = conn.prepareStatement(
                    "UPDATE resources SET available_slots = 2 WHERE id = ?")) {
                resetRoom.setString(1, ROOM_ID);
                resetRoom.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset test state", e);
        }
    }

    private static int countClaimRowsForResource(String resourceId) {
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM claims WHERE resource_id = ?")) {
            stmt.setString(1, resourceId);
            try (var rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to count claim rows", e);
        }
    }

    private static int getAvailableSlots(String resourceId) {
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT available_slots FROM resources WHERE id = ?")) {
            stmt.setString(1, resourceId);
            try (var rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read available_slots", e);
        }
    }
}