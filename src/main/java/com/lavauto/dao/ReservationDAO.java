package com.lavauto.dao;

import com.lavauto.db.DatabaseConnection;
import com.lavauto.model.Reservation;
import com.lavauto.model.Client;
import com.lavauto.model.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    private ClientDAO clientDAO;
    private ServiceDAO serviceDAO;
    
    public ReservationDAO() {
        this.clientDAO = new ClientDAO();
        this.serviceDAO = new ServiceDAO();
    }
    
    public List<Reservation> getAllReservations() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservations";
        
        // Charger tous les clients et services en mémoire
        List<Client> clients = clientDAO.getAllClients();
        List<Service> services = serviceDAO.getAllServices();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                int reservationId = rs.getInt("id");
                int clientId = rs.getInt("client_id");
                int serviceId = rs.getInt("service_id");
                Timestamp dateReservation = rs.getTimestamp("date_reservation");
                String statut = rs.getString("statut");

                // Recherche en mémoire
                Client client = clients.stream().filter(c -> c.getId() == clientId).findFirst().orElse(null);
                Service service = services.stream().filter(s -> s.getId() == serviceId).findFirst().orElse(null);

                Reservation reservation = new Reservation(
                    reservationId,
                    client,
                    service,
                    dateReservation.toLocalDateTime(),
                    statut
                );
                reservations.add(reservation);
            }
        }
        return reservations;
    }
    
    public void addReservation(Reservation reservation) throws SQLException {
        String query = "INSERT INTO reservations (client_id, service_id, date_reservation, statut) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, reservation.getClient().getId());
            pstmt.setInt(2, reservation.getService().getId());
            pstmt.setTimestamp(3, Timestamp.valueOf(reservation.getDateReservation()));
            pstmt.setString(4, reservation.getStatut());
            
            try {
                pstmt.executeUpdate();
            } catch (SQLException e) {
                if (e.getMessage().contains("Data truncated")) {
                    // Si l'erreur est due à la troncature, on modifie la structure et on réessaie
                    updateTableStructure();
                    pstmt.executeUpdate();
                } else {
                    throw e;
                }
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reservation.setId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    private void updateTableStructure() throws SQLException {
        String alterTable = "ALTER TABLE reservations MODIFY COLUMN statut VARCHAR(20)";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(alterTable);
        }
    }
    
    public void updateReservation(Reservation reservation) throws SQLException {
        String query = "UPDATE reservations SET client_id = ?, service_id = ?, date_reservation = ?, statut = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, reservation.getClient().getId());
            pstmt.setInt(2, reservation.getService().getId());
            pstmt.setTimestamp(3, Timestamp.valueOf(reservation.getDateReservation()));
            pstmt.setString(4, reservation.getStatut());
            pstmt.setInt(5, reservation.getId());
            
            try {
                pstmt.executeUpdate();
            } catch (SQLException e) {
                if (e.getMessage().contains("Data truncated")) {
                    // Si l'erreur est due à la troncature, on modifie la structure et on réessaie
                    updateTableStructure();
                    pstmt.executeUpdate();
                } else {
                    throw e;
                }
            }
        }
    }
    
    public void deleteReservation(int id) throws SQLException {
        String query = "DELETE FROM reservations WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    
    public Reservation getReservationById(int id) throws SQLException {
        String query = "SELECT * FROM reservations WHERE id = ?";

        // Charger tous les clients et services en mémoire
        List<Client> clients = clientDAO.getAllClients();
        List<Service> services = serviceDAO.getAllServices();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int clientId = rs.getInt("client_id");
                    int serviceId = rs.getInt("service_id");
                    Timestamp dateReservation = rs.getTimestamp("date_reservation");
                    String statut = rs.getString("statut");

                    // Recherche en mémoire
                    Client client = clients.stream().filter(c -> c.getId() == clientId).findFirst().orElse(null);
                    Service service = services.stream().filter(s -> s.getId() == serviceId).findFirst().orElse(null);

                    return new Reservation(
                        rs.getInt("id"),
                        client,
                        service,
                        dateReservation.toLocalDateTime(),
                        statut
                    );
                }
            }
        }
        return null;
    }
} 