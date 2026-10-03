package com.ticketflow.booking.repository;

import com.ticketflow.booking.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

// One repository, works for BOTH GearResource and RoomResource.
public interface ResourceRepository extends JpaRepository<Resource, String> {
}