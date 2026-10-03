package com.ticketflow.model;

public class GearResource extends Resource {
    private final GearCondition condition;
    private final GearCategory category;


    public GearResource(String id, String name, int totalSlots, GearCondition condition, GearCategory category) {
        super(id, name, totalSlots);
        this.condition=condition;
        this.category=category;


    }

    public GearCategory getCategory() {return category;}

    public GearCondition getCondition() {return condition;}


    //enums
    public enum GearCategory {
        CAMERA,
        LIGHTING,
        AUDIO,
        DISPLAY,
        MISCELLANEOUS
    }
    public enum GearCondition {
        NEW,
        GOOD,
        FAIR
    }
}
