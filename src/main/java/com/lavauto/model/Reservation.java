package com.lavauto.model;

import java.time.LocalDateTime;

public class Reservation {
    private int id;
    private Client client;
    private Service service;
    private LocalDateTime dateReservation;
    private String statut;
    
    public Reservation(int id, Client client, Service service, LocalDateTime dateReservation, String statut) {
        this.id = id;
        this.client = client;
        this.service = service;
        this.dateReservation = dateReservation;
        this.statut = statut;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Client getClient() {
        return client;
    }
    
    public void setClient(Client client) {
        this.client = client;
    }
    
    public Service getService() {
        return service;
    }
    
    public void setService(Service service) {
        this.service = service;
    }
    
    public LocalDateTime getDateReservation() {
        return dateReservation;
    }
    
    public void setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
    }
    
    public String getStatut() {
        return statut;
    }
    
    public void setStatut(String statut) {
        this.statut = statut;
    }
    
    @Override
    public String toString() {
        return "Réservation #" + id + " - " + client + " - " + service + " - " + dateReservation;
    }
} 