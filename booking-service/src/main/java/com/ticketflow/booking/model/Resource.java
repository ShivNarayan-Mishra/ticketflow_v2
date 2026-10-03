package com.ticketflow.booking.model;

import jakarta.persistence.*;

// Single-table inheritance: this + its two subclasses all map onto the SAME
// "resources" table your raw-JDBC ResourceDAO already uses. @DiscriminatorColumn
// tells Hibernate to use resource_type to decide which subclass to build when
// reading a row back - this is the framework doing automatically what
// ResourceDAO.mapRow() does by hand with an if/else on that same column.
@Entity
@Table(name = "resources")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "resource_type")
public abstract class Resource {

    @Id
    private String id;

    private String name;

    @Column(name = "total_slots")
    private int totalSlots;

    @Column(name = "available_slots")
    private int availableSlots;

    // The optimistic-locking column, shared by every resource type since it
    // lives on the base class. Same mechanism regardless of Gear vs Room.
    @Version
    private Long version;

    protected Resource() {
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getTotalSlots() { return totalSlots; }
    public int getAvailableSlots() { return availableSlots; }
    public Long getVersion() { return version; }

    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }
}