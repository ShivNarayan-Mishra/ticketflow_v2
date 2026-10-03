package com.ticketflow.manager;

import com.ticketflow.exception.ResourceFullException;
import com.ticketflow.model.Claim;
import com.ticketflow.model.GearResource;
import com.ticketflow.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import com.ticketflow.model.Claim.ClaimStatus;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookingManagerTest {
    private BookingManager bookingManager;
    private GearResource gearResource;
    @BeforeEach
    void setUp() {
        ConcurrentHashMap<String, Resource> testMap = new ConcurrentHashMap<>();

        // Loop 10 times to create 10 distinct projectors
        for (int i = 1; i <= 10; i++) {
            String gearId = "GEAR-" + i;
            testMap.put(gearId, new GearResource(gearId, "Projector " + i, 1, GearResource.GearCondition.GOOD, GearResource.GearCategory.CAMERA));
        }

        bookingManager = new BookingManager(testMap);
    }
    @Test
    void testConcurrentClaims() throws InterruptedException {
        int totalGears = 10;
        int threadsPerGear = 50;
        int totalThreads = totalGears * threadsPerGear; // 500 total threads

        ExecutorService executorService = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        Claim testClaim = new Claim(
                "DUMMY-ID", // The specific ID doesn't matter for the lock test
                "test.student@university.edu",
                ClaimStatus.PENDING,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        // OUTER LOOP: For each of the 10 gears...
        for (int gearIndex = 1; gearIndex <= totalGears; gearIndex++) {
            String targetGearId = "GEAR-" + gearIndex;

            // INNER LOOP: ...queue up 50 workers to attack that specific gear
            for (int i = 0; i < threadsPerGear; i++) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            latch.await();
                            // Every worker tries to book their assigned targetGearId
                            bookingManager.claimResource(targetGearId, testClaim);
                            successCount.incrementAndGet();
                        } catch (ResourceFullException e) {
                            failureCount.incrementAndGet();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                });
            }
        }

        // Fire the starting gun for all 500 threads at once!
        latch.countDown();

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // Assert the massive scale results
        assertEquals(10, successCount.get(), "Exactly 10 threads should succeed (1 per gear)");
        assertEquals(490, failureCount.get(), "490 threads should fail with ResourceFullException");
    }

}
