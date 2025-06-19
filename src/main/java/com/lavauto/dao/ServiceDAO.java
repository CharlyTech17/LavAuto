package com.lavauto.dao;

import com.lavauto.db.DatabaseConnection;
import com.lavauto.model.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {
    
    public List<Service> getAllServices() throws SQLException {
        List<Service> services = new ArrayList<>();
        String query = "SELECT * FROM services";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Service service = new Service(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("description"),
                    rs.getDouble("prix")
                );
                services.add(service);
            }
        }
        return services;
    }
    
    public void addService(Service service) throws SQLException {
        String query = "INSERT INTO services (nom, description, prix) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, service.getNom());
            pstmt.setString(2, service.getDescription());
            pstmt.setDouble(3, service.getPrix());
            pstmt.executeUpdate();
        }
    }
    
    public void updateService(Service service) throws SQLException {
        String query = "UPDATE services SET nom = ?, description = ?, prix = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, service.getNom());
            pstmt.setString(2, service.getDescription());
            pstmt.setDouble(3, service.getPrix());
            pstmt.setInt(4, service.getId());
            pstmt.executeUpdate();
        }
    }
    
    public void deleteService(int id) throws SQLException {
        String query = "DELETE FROM services WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    
    public Service getServiceById(int id) throws SQLException {
        String query = "SELECT * FROM services WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Service(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getDouble("prix")
                    );
                }
            }
        }
        return null;
    }
} 