package com.ticketflow.booking.controller;

import com.ticketflow.booking.model.Resource;
import com.ticketflow.booking.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    // Returns BOTH gear and room resources mixed together, same as the raw-JDBC
    // ResourceDAO.findAll() already does - JSON serialization will show whichever
    // fields are actually present on each concrete subclass.
    @GetMapping
    public List<Resource> findAll() {
        return resourceService.findAll();
    }

    @GetMapping("/{id}")
    public Resource findById(@PathVariable String id) {
        return resourceService.findById(id);
    }

    @PostMapping("/{id}/claim")
    public ResponseEntity<Void> claim(@PathVariable String id) {
        resourceService.claim(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}