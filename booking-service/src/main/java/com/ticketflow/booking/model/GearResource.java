package com.ticketflow.booking.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@DiscriminatorValue("GEAR")
public class GearResource extends Resource {

    @Enumerated(EnumType.STRING)
    private GearCondition condition;

    @Enumerated(EnumType.STRING)
    private GearCategory category;

    protected GearResource() {
    }

    public GearCondition getCondition() { return condition; }
    public GearCategory getCategory() { return category; }

    public enum GearCondition {
        NEW,
        GOOD,
        FAIR
    }

    public enum GearCategory {
        CAMERA,
        LIGHTING,
        AUDIO,
        DISPLAY,
        MISCELLANEOUS
    }
}