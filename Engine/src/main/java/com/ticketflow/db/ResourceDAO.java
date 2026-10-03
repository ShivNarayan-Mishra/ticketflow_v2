package com.ticketflow.db;

import com.ticketflow.exception.ResourceNotFoundException;
import com.ticketflow.model.GearResource;
import com.ticketflow.model.Resource;
import com.ticketflow.model.RoomResource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResourceDAO {

    public Resource findById(String id) {
        String sql = "SELECT * FROM resources WHERE id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new ResourceNotFoundException("No resource found with id " + id);
                }
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch resource " + id, e);
        }
    }

    public List<Resource> findAll() {
        String sql = "SELECT * FROM resources";
        List<Resource> resources = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                resources.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch resources", e);
        }
        return resources;
    }

    // Persists the current in-memory slot count for a resource after a claim or cancellation
    public void updateAvailableSlots(String id, int availableSlots) {
        String sql = "UPDATE resources SET available_slots = ? WHERE id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, availableSlots);
            stmt.setString(2, id);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new ResourceNotFoundException("No resource found with id " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update slots for " + id, e);
        }
    }

    // resource_type in the row decides which subclass to reconstruct
    private Resource mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String type = rs.getString("resource_type");
        String name = rs.getString("name");
        int totalSlots = rs.getInt("total_slots");
        int availableSlots = rs.getInt("available_slots");

        Resource resource;
        if ("ROOM".equals(type)) {
            String location = rs.getString("location");
            resource = new RoomResource(id, name, totalSlots, location);
        } else if ("GEAR".equals(type)) {
            GearResource.GearCondition condition = GearResource.GearCondition.valueOf(rs.getString("condition"));
            GearResource.GearCategory category = GearResource.GearCategory.valueOf(rs.getString("category"));
            resource = new GearResource(id, name, totalSlots, condition, category);
        } else {
            throw new IllegalStateException("Unknown resource_type in database: " + type);
        }

        // The constructor always sets availableSlots = totalSlots (a brand-new resource).
        // Overwrite it with the real persisted value.
        resource.setAvailableSlots(availableSlots);
        return resource;
    }
}