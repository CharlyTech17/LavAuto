package com.lavauto.dao;

import com.lavauto.db.DatabaseConnection;
import com.lavauto.model.Facture;
import com.lavauto.model.Client;
import com.lavauto.model.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FactureDAO {
    private ClientDAO clientDAO;
    private ServiceDAO serviceDAO;
    
    public FactureDAO() {
        this.clientDAO = new ClientDAO();
        this.serviceDAO = new ServiceDAO();
    }
    
    public List<Facture> getAllFactures() throws SQLException {
        List<Facture> factures = new ArrayList<>();
        String query = "SELECT f.*, GROUP_CONCAT(fs.service_id) as service_ids " +
                      "FROM factures f " +
                      "LEFT JOIN facture_services fs ON f.id = fs.facture_id " +
                      "GROUP BY f.id";
        
        // Charger tous les clients et services en mémoire
        List<Client> clients = clientDAO.getAllClients();
        List<Service> services = serviceDAO.getAllServices();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                int factureId = rs.getInt("id");
                int clientId = rs.getInt("client_id");
                Timestamp dateFacturation = rs.getTimestamp("date_facturation");
                boolean payee = rs.getBoolean("payee");
                String serviceIdsStr = rs.getString("service_ids");

                // Recherche en mémoire
                Client client = clients.stream().filter(c -> c.getId() == clientId).findFirst().orElse(null);

                List<Service> factureServices = new ArrayList<>();
                if (serviceIdsStr != null && !serviceIdsStr.isEmpty()) {
                    String[] serviceIds = serviceIdsStr.split(",");
                    for (String serviceIdStr : serviceIds) {
                        int serviceId = Integer.parseInt(serviceIdStr);
                        Service service = services.stream().filter(s -> s.getId() == serviceId).findFirst().orElse(null);
                        if (service != null) {
                            factureServices.add(service);
                        }
                    }
                }

                Facture facture = new Facture(
                    factureId,
                    client,
                    factureServices,
                    dateFacturation.toLocalDateTime()
                );
                facture.setPayee(payee);
                factures.add(facture);
            }
        }
        return factures;
    }
    
    private List<Service> getServicesForFacture(int factureId) throws SQLException {
        List<Service> services = new ArrayList<>();
        String query = "SELECT service_id FROM facture_services WHERE facture_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, factureId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Service service = serviceDAO.getServiceById(rs.getInt("service_id"));
                    if (service != null) {
                        services.add(service);
                    }
                }
            }
        }
        return services;
    }
    
    public void addFacture(Facture facture) throws SQLException {
        String factureQuery = "INSERT INTO factures (client_id, date_facturation, payee) VALUES (?, ?, ?)";
        String serviceQuery = "INSERT INTO facture_services (facture_id, service_id) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(factureQuery, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, facture.getClient().getId());
                pstmt.setTimestamp(2, Timestamp.valueOf(facture.getDateFacturation()));
                pstmt.setBoolean(3, facture.isPayee());
                pstmt.executeUpdate();
                
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        facture.setId(generatedKeys.getInt(1));
                    }
                }
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(serviceQuery)) {
                for (Service service : facture.getServices()) {
                    pstmt.setInt(1, facture.getId());
                    pstmt.setInt(2, service.getId());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            
            conn.commit();
        }
    }
    
    public void updateFacture(Facture facture) throws SQLException {
        String factureQuery = "UPDATE factures SET client_id = ?, date_facturation = ?, payee = ? WHERE id = ?";
        String deleteServicesQuery = "DELETE FROM facture_services WHERE facture_id = ?";
        String insertServicesQuery = "INSERT INTO facture_services (facture_id, service_id) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(factureQuery)) {
                pstmt.setInt(1, facture.getClient().getId());
                pstmt.setTimestamp(2, Timestamp.valueOf(facture.getDateFacturation()));
                pstmt.setBoolean(3, facture.isPayee());
                pstmt.setInt(4, facture.getId());
                pstmt.executeUpdate();
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(deleteServicesQuery)) {
                pstmt.setInt(1, facture.getId());
                pstmt.executeUpdate();
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(insertServicesQuery)) {
                for (Service service : facture.getServices()) {
                    pstmt.setInt(1, facture.getId());
                    pstmt.setInt(2, service.getId());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            
            conn.commit();
        }
    }
    
    public void deleteFacture(int id) throws SQLException {
        String deleteServicesQuery = "DELETE FROM facture_services WHERE facture_id = ?";
        String deleteFactureQuery = "DELETE FROM factures WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(deleteServicesQuery)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(deleteFactureQuery)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
            
            conn.commit();
        }
    }
    
    public Facture getFactureById(int id) throws SQLException {
        String query = "SELECT f.*, GROUP_CONCAT(fs.service_id) as service_ids " +
                      "FROM factures f " +
                      "LEFT JOIN facture_services fs ON f.id = fs.facture_id " +
                      "WHERE f.id = ? GROUP BY f.id";

        // Charger tous les clients et services en mémoire
        List<Client> clients = clientDAO.getAllClients();
        List<Service> services = serviceDAO.getAllServices();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int clientId = rs.getInt("client_id");
                    Timestamp dateFacturation = rs.getTimestamp("date_facturation");
                    boolean payee = rs.getBoolean("payee");
                    String serviceIdsStr = rs.getString("service_ids");

                    // Recherche en mémoire
                    Client client = clients.stream().filter(c -> c.getId() == clientId).findFirst().orElse(null);

                    List<Service> factureServices = new ArrayList<>();
                    if (serviceIdsStr != null && !serviceIdsStr.isEmpty()) {
                        String[] serviceIds = serviceIdsStr.split(",");
                        for (String serviceIdStr : serviceIds) {
                            int serviceId = Integer.parseInt(serviceIdStr);
                            Service service = services.stream().filter(s -> s.getId() == serviceId).findFirst().orElse(null);
                            if (service != null) {
                                factureServices.add(service);
                            }
                        }
                    }

                    Facture facture = new Facture(
                        rs.getInt("id"),
                        client,
                        factureServices,
                        dateFacturation.toLocalDateTime()
                    );
                    facture.setPayee(payee);
                    return facture;
                }
            }
        }
        return null;
    }
} 