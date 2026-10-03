package com.ticketflow.model;

import com.ticketflow.exception.InvalidClaimException;

import java.util.UUID;
import java.time.LocalDateTime;

public class Claim {
    private String id;
    private String resourceId;
    private String userEmail;
    private ClaimStatus currentStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private final LocalDateTime bookedAt;

    public Claim(String resourceId,String userEmail,ClaimStatus currentStatus,LocalDateTime startTime,LocalDateTime endTime){
        this.id= UUID.randomUUID().toString();//make unique ids automatically in the backend not by caller
        this.resourceId=resourceId;
        this.userEmail=userEmail;
        this.currentStatus=currentStatus;
        this.startTime=startTime;
        this.endTime=endTime;
        this.bookedAt=LocalDateTime.now();


    }
    //getters
    public String getUUID(){return id;}
    public String getResourceID(){
        return resourceId;
    }
    public String getUserEmail(){
        return userEmail;
    }
    public ClaimStatus getCurrentStatus(){
        return currentStatus;
    }
    public LocalDateTime getBookedAt() { return bookedAt;    }
    public LocalDateTime getStartTime(){return startTime;}
    public LocalDateTime getEndTime(){return endTime;}




    public enum ClaimStatus {
        PENDING,
        CONFIRMED,
        CANCELLED
    }

    public void setStatus(ClaimStatus newStatus) {
        if (this.currentStatus == ClaimStatus.CANCELLED && newStatus != ClaimStatus.CANCELLED) {
            throw new InvalidClaimException("Cannot change a cancelled claim's status to " + newStatus);
        }
        if (this.currentStatus == ClaimStatus.CONFIRMED && newStatus == ClaimStatus.PENDING) {
            throw new InvalidClaimException("Cannot revert a confirmed claim back to PENDING");
        }
        this.currentStatus = newStatus;
    }
    public void setId(String id) {
        this.id = id;
    }


}
