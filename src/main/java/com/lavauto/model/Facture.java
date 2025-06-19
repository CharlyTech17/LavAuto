package com.lavauto.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Facture {
    private int id;
    private Client client;
    private List<Service> services;
    private LocalDateTime dateFacturation;
    private double montantTotal;
    private boolean payee;
    
    public Facture(int id, Client client, List<Service> services, LocalDateTime dateFacturation) {
        this.id = id;
        this.client = client;
        this.services = services;
        this.dateFacturation = dateFacturation;
        this.montantTotal = calculerMontantTotal();
        this.payee = false;
    }
    
    private double calculerMontantTotal() {
        return services.stream().mapToDouble(Service::getPrix).sum();
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
    
    public List<Service> getServices() {
        return services;
    }
    
    public void setServices(List<Service> services) {
        this.services = services;
        this.montantTotal = calculerMontantTotal();
    }
    
    public LocalDateTime getDateFacturation() {
        return dateFacturation;
    }
    
    public void setDateFacturation(LocalDateTime dateFacturation) {
        this.dateFacturation = dateFacturation;
    }
    
    public double getMontantTotal() {
        return montantTotal;
    }
    
    public boolean isPayee() {
        return payee;
    }
    
    public void setPayee(boolean payee) {
        this.payee = payee;
    }
    
    @Override
    public String toString() {
        return "Facture #" + id + " - " + client + " - " + montantTotal + "€";
    }
} 