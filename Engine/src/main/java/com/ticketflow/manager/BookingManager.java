package com.ticketflow.manager;

import com.ticketflow.model.Resource;
import com.ticketflow.model.Claim;
import com.ticketflow.exception.ResourceNotFoundException;
import com.ticketflow.db.ResourceDAO;
import com.ticketflow.db.ClaimDAO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BookingManager {

    private final Map<String, Resource> resources;
    private final ResourceDAO resourceDAO; // null in memory-only mode
    private final ClaimDAO claimDAO;       // null in memory-only mode

    // In-memory only — used by unit tests, no database involved
    public BookingManager(Map<String, Resource> resources) {
        this.resources = resources;
        this.resourceDAO = null;
        this.claimDAO = null;
    }

    // Persistence-enabled — loads the cache from Postgres, writes through on every claim
    public BookingManager(ResourceDAO resourceDAO, ClaimDAO claimDAO) {
        this.resourceDAO = resourceDAO;
        this.claimDAO = claimDAO;
        this.resources = new ConcurrentHashMap<>();
        for (Resource r : resourceDAO.findAll()) {
            this.resources.put(r.getId(), r);
        }
    }

    public synchronized void claimResource(String resourceId, Claim claim) {
        Resource resource = resources.get(resourceId);
        if (resource == null) {
            throw new ResourceNotFoundException(resourceId);
        }

        resource.resourceStatus(claim);

        if (resourceDAO != null && claimDAO != null) {
            resourceDAO.updateAvailableSlots(resourceId, resource.getAvailableSlots());
            claimDAO.insert(claim);
        }
    }
}