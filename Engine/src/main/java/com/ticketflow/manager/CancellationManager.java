package com.ticketflow.manager;

import com.ticketflow.exception.CancellationWindowClosedException;
import com.ticketflow.exception.ResourceNotFoundException;
import com.ticketflow.model.Claim;
import com.ticketflow.model.Resource;
import com.ticketflow.model.Claim.ClaimStatus;
import com.ticketflow.util.TimeValidator;

import java.time.LocalDateTime;
import java.util.Map;

public class CancellationManager {

    private final Map<String, Resource> resourceCancelMap;

    public CancellationManager(Map<String, Resource> resourceCancelMap) {
        this.resourceCancelMap = resourceCancelMap;
    }

    // Takes the Claim object directly. Simple and clean.
    public synchronized void cancelClaim(Claim claim) {

        // 1. Validate cancellation window
        if (!TimeValidator.isCancellable(claim.getStartTime())) {
            throw new CancellationWindowClosedException("Cancellation denied: The booking has already started.");
        }

        // 2. Fetch the gear directly from the map
        Resource resource = resourceCancelMap.get(claim.getResourceID());
        if (resource == null) {
            throw new ResourceNotFoundException("The item does not exist");
        }

        // 3. The Manager marks the ticket as cancelled
        claim.setStatus(ClaimStatus.CANCELLED);

        // 4. The Manager hands JUST the ID down to the gear to satisfy the interface
        resource.cancel(claim.getUUID());
    }
}