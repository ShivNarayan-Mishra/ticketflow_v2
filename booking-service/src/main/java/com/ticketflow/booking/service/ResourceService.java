package com.ticketflow.booking.service;

import com.ticketflow.booking.exception.ResourceFullException;
import com.ticketflow.booking.exception.ResourceNotFoundException;
import com.ticketflow.booking.model.Resource;
import com.ticketflow.booking.repository.ResourceRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Cacheable(value = "resources", key = "'all'")
    public List<Resource> findAll() {
        return resourceRepository.findAll();
    }

    @Cacheable(value = "resources", key = "#id")
    public Resource findById(String id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No resource found with id " + id));
    }


    @Transactional
    @CacheEvict(value = "resources", allEntries = true)
    public void claim(String id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No resource found with id " + id));

        if (resource.getAvailableSlots() <= 0) {
            throw new ResourceFullException("We're sorry. All slots are filled.");
        }

        resource.setAvailableSlots(resource.getAvailableSlots() - 1);
        resourceRepository.save(resource);
    }
}