package com.ticketflow.booking.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ROOM")
public class RoomResource extends Resource {

    private String location;

    protected RoomResource() {
    }

    public String getLocation() { return location; }
}