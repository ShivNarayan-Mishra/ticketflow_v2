package com.ticketflow.db;

import com.ticketflow.exception.ResourceNotFoundException;
import com.ticketflow.model.Claim;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ClaimDAO {

    public void insert(Claim claim) {
        String sql = "INSERT INTO claims (id, resource_id, user_email, start_time, end_time, status, booked_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, claim.getUUID());
            stmt.setString(2, claim.getResourceID());
            stmt.setString(3, claim.getUserEmail());
            stmt.setTimestamp(4, Timestamp.valueOf(claim.getStartTime()));
            stmt.setTimestamp(5, Timestamp.valueOf(claim.getEndTime()));
            stmt.setString(6, claim.getCurrentStatus().name());
            stmt.setTimestamp(7, Timestamp.valueOf(claim.getBookedAt()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert claim " + claim.getUUID(), e);
        }
    }

    public Claim findById(String id) {
        String sql = "SELECT * FROM claims WHERE id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new ResourceNotFoundException("No claim found with id " + id);
                }
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch claim " + id, e);
        }
    }

    public void updateStatus(String id, Claim.ClaimStatus status) {
        String sql = "UPDATE claims SET status = ? WHERE id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setString(2, id);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new ResourceNotFoundException("No claim found with id " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update status for claim " + id, e);
        }
    }

    private Claim mapRow(ResultSet rs) throws SQLException {
        Claim claim = new Claim(
                rs.getString("resource_id"),
                rs.getString("user_email"),
                Claim.ClaimStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("start_time").toLocalDateTime(),
                rs.getTimestamp("end_time").toLocalDateTime()
        );

        // The constructor always generates a fresh random id; overwrite it with the
        // one actually persisted in the database so the object matches its row.
        claim.setId(rs.getString("id"));
        return claim;
    }
}