package com.ticketflow;

import com.ticketflow.model.Claim;
import com.ticketflow.model.RoomResource;

import com.ticketflow.exception.ResourceFullException;
import java.time.LocalDateTime;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        System.out.println("--- Starting TicketFlow Engine ---");

        // 1. Create a RoomResource with exactly 2 slots
        RoomResource lab = new RoomResource("104", "CS LAB", 2, "C004");
        System.out.println("Created Resource: " + lab.getName() + " | Slots: " + lab.getAvailableSlots());

        // 2. Set up some dummy times for our claims
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);

        // 3. Create the Claim objects using the constructor you built
        Claim claim1 = new Claim(lab.getId(), "trapp@gmail.com", Claim.ClaimStatus.PENDING, start, end);
        Claim claim2 = new Claim(lab.getId(), "alice@gmail.com", Claim.ClaimStatus.PENDING, start, end);
        Claim claim3 = new Claim(lab.getId(), "bob@gmail.com", Claim.ClaimStatus.PENDING, start, end);

        // 4. Claim the first slot
        System.out.println("\nAttempting Claim 1 (trapp@gmail.com)...");
        lab.resourceStatus(claim1);
        System.out.println("Success! Slots remaining: " + lab.getAvailableSlots());

        // 5. Claim the second slot
        System.out.println("\nAttempting Claim 2 (alice@gmail.com)...");
        lab.resourceStatus(claim2);;
        System.out.println("Success! Slots remaining: " + lab.getAvailableSlots());

        // 6. Attempt a third claim to prove your exception works
        System.out.println("\nAttempting Claim 3 (bob@gmail.com) - This should crash safely...");
        try {
            lab.resourceStatus(claim3);
        } catch (ResourceFullException e) {
            System.out.println("EXPECTED ERROR CAUGHT: " + e.getMessage());
        }
    }
}