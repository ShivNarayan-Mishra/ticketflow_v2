package com.ticketflow.model;

public class RoomResource extends Resource {
    private String location;

    // The constructor takes the details and passes them UP to the parent vault using super()
    public RoomResource(String id,String name, int totalSlots,String location) {

        super(id,name,totalSlots);
        this.location=location;
    }
    /*
    private void setLocation(String location){
        this.location=location;
    }

     */
    public String getLocation(){
        return this.location;
    }
}
