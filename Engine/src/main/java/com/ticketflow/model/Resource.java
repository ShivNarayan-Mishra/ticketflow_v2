package com.ticketflow.model;
import com.ticketflow.exception.*;

public abstract class Resource implements Cancellable,Claimable {

        private final String id;
        private final String name;
        private int availableSlots;
        private final int totalSlots;

        // The constructor sets up the item when it is first created
        protected Resource(String id,String name,int totalSlots) {
            this.id = id;
            this.availableSlots = totalSlots;
            this.totalSlots=totalSlots;
            this.name=name;
        }

        // Getters so other parts of your code can read these values
        public String getId() {
            return id;
        }

        public int getAvailableSlots() {
            return availableSlots;
        }

        public String getName(){return name;}

        // A helper method to reduce the count when someone successfully books it
        public void decreaseSlots() {

            this.availableSlots--;
            //resourceful exceptions go here

    }
    public void increaseSlots(){
            this.availableSlots++;
    }
    public int getTotalSlots(){
            return totalSlots;

    }

    @Override
    public void resourceStatus(Claim claim) {
        if(this.availableSlots <= 0){
            
            throw new ResourceFullException("We're sorry. All slots are filled.");
        }
        decreaseSlots();
        claim.setStatus(Claim.ClaimStatus.CONFIRMED);
        }

    @Override
    public void cancel(String id) {
            if(id==null||id.isBlank()){
                throw new InvalidClaimException("INVALID ID");

            }
        increaseSlots();

    }
    public void setAvailableSlots(int availableSlots) {
        if (availableSlots < 0 || availableSlots > totalSlots) {
            throw new IllegalArgumentException(
                    "availableSlots must be between 0 and " + totalSlots + ", got " + availableSlots);
        }
        this.availableSlots = availableSlots;
    }

}
