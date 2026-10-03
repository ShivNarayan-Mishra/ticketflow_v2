package com.ticketflow.booking;

import com.ticketflow.booking.service.ResourceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ResourceServiceConcurrencyTest {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testFiftyConcurrentClaims() throws InterruptedException {
        // 1. Force the database state: 1 available slot, version 0
        String targetResource = "GEAR-001";
        jdbcTemplate.update("UPDATE resources SET available_slots = 1, version = 0 WHERE id = ?", targetResource);

        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // The startLatch acts as a starting gun so all 50 threads fire at the exact same millisecond
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 2. Queue up the 50 threads
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Thread waits here until the latch is released
                    resourceService.claim(targetResource);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Catches both OptimisticLockingFailureException and ResourceFullException
                    failCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 3. Fire all threads simultaneously
        startLatch.countDown();

        // 4. Wait for all 50 threads to finish execution
        endLatch.await();

        // 5. Verify the results
        assertEquals(1, successCount.get(), "Exactly one claim should succeed");
        assertEquals(49, failCount.get(), "49 claims should fail due to optimistic locking or zero slots");

        // Verify the database recorded the slot reduction
        Integer remainingSlots = jdbcTemplate.queryForObject(
                "SELECT available_slots FROM resources WHERE id = ?", Integer.class, targetResource);
        assertEquals(0, remainingSlots, "The final available slots should be zero");
    }
}