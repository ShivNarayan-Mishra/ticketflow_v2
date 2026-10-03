package com.ticketflow.manager;

import com.ticketflow.exception.CancellationWindowClosedException;
import com.ticketflow.exception.ResourceNotFoundException;
import com.ticketflow.model.Claim;
import com.ticketflow.model.Claim.ClaimStatus;
import com.ticketflow.model.GearResource;
import com.ticketflow.model.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class CancellationManagerTest {

    private CancellationManager cancellationManager;
    private GearResource gearResource;

    @BeforeEach
    void setUp() {
        ConcurrentHashMap<String, Resource> testMap = new ConcurrentHashMap<>();

        gearResource = new GearResource("GEAR-1", "Camera", 1, GearResource.GearCondition.GOOD, GearResource.GearCategory.CAMERA);
        gearResource.decreaseSlots();
        testMap.put("GEAR-1", gearResource);

        cancellationManager = new CancellationManager(testMap);
    }

    @Test
    void testSuccessfulCancellation() {
        Claim futureClaim = new Claim(
                "GEAR-1",
                "student@university.edu",
                ClaimStatus.CONFIRMED,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        cancellationManager.cancelClaim(futureClaim);

        assertEquals(1, gearResource.getAvailableSlots(), "Slots should increment back to 1");
    }

    @Test
    void testCancellationWindowClosedException() {
        Claim expiredClaim = new Claim(
                "GEAR-1",
                "student@university.edu",
                ClaimStatus.CONFIRMED,
                LocalDateTime.now().minusDays(1), // Started yesterday!
                LocalDateTime.now().plusDays(1)
        );
        // Explicitly building the Executable instead of using a lambda
        assertThrows(CancellationWindowClosedException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                cancellationManager.cancelClaim(expiredClaim);
            }
        });
    }

    @Test
    void testResourceNotFoundException() {
        Claim ghostClaim = new Claim(
                "FAKE-GEAR-99",
                "student@university.edu",
                ClaimStatus.CONFIRMED,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        //assertThrows(ResourceNotFoundException.class, () -> cancellationManager.cancelClaim(ghostClaim));
        // Explicitly building the Executable instead of using a lambda
        assertThrows(ResourceNotFoundException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                cancellationManager.cancelClaim(ghostClaim);
            }
        });
    }
}