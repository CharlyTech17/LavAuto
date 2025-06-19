package com.lavauto.ui;

import com.lavauto.dao.ReservationDAO;
import com.lavauto.dao.ClientDAO;
import com.lavauto.dao.ServiceDAO;
import com.lavauto.model.Reservation;
import com.lavauto.model.Client;
import com.lavauto.model.Service;
import com.lavauto.dao.FactureDAO;
import com.lavauto.model.Facture;
import com.lavauto.ui.FacturePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ArrayList;

public class ReservationPanel extends JPanel {
    private JTable reservationTable;
    private DefaultTableModel tableModel;
    private ReservationDAO reservationDAO;
    private ClientDAO clientDAO;
    private ServiceDAO serviceDAO;
    private FacturePanel facturePanel;
    
    public ReservationPanel() {
        this(null);
    }

    public ReservationPanel(FacturePanel facturePanel) {
        this.facturePanel = facturePanel;
        reservationDAO = new ReservationDAO();
        clientDAO = new ClientDAO();
        serviceDAO = new ServiceDAO();
        setLayout(new BorderLayout());
        setBackground(new Color(240, 248, 255)); // Bleu très clair
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("Gestion des Réservations");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(30, 60, 120));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        
        // Création du modèle de table
        String[] columns = {"ID", "Client", "Service", "Date", "Statut"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Création de la table
        reservationTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(reservationTable);
        
        // Panneau de boutons
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Ajouter");
        JButton editButton = new JButton("Modifier");
        JButton deleteButton = new JButton("Supprimer");
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        // Style du tableau
        reservationTable.getTableHeader().setBackground(new Color(30, 60, 120));
        reservationTable.getTableHeader().setForeground(Color.WHITE);
        reservationTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        reservationTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reservationTable.setRowHeight(24);
        // Style des boutons
        Color btnColor = new Color(100, 149, 237);
        Color btnHover = new Color(65, 105, 225);
        JButton[] buttons = {addButton, editButton, deleteButton};
        for (JButton btn : buttons) {
            btn.setBackground(btnColor);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(btnHover);
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(btnColor);
                }
            });
        }
        buttonPanel.setBackground(new Color(240, 248, 255));
        scrollPane.setBackground(new Color(240, 248, 255));
        
        // Ajout des composants
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Chargement des données
        loadReservations();
        
        // Gestion des événements
        addButton.addActionListener(e -> showReservationDialog(null));
        editButton.addActionListener(e -> {
            int selectedRow = reservationTable.getSelectedRow();
            if (selectedRow >= 0) {
                try {
                    int id = (int) tableModel.getValueAt(selectedRow, 0);
                    Reservation reservation = reservationDAO.getReservationById(id);
                    if (reservation != null) {
                        showReservationDialog(reservation);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erreur lors de la récupération de la réservation: " + ex.getMessage());
                }
            }
        });
        
        deleteButton.addActionListener(e -> {
            int selectedRow = reservationTable.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                try {
                    reservationDAO.deleteReservation(id);
                    loadReservations();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erreur lors de la suppression: " + ex.getMessage());
                }
            }
        });
    }
    
    private void loadReservations() {
        tableModel.setRowCount(0);
        try {
            List<Reservation> reservations = reservationDAO.getAllReservations();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            NumberFormat nf = NumberFormat.getInstance(Locale.FRANCE);
            
            // Vérifier les factures manquantes pour les réservations terminées
            FactureDAO factureDAO = new FactureDAO();
            List<Facture> factures = factureDAO.getAllFactures();
            
            for (Reservation reservation : reservations) {
                // Ajouter à la table
                tableModel.addRow(new Object[]{
                    reservation.getId(),
                    reservation.getClient(),
                    reservation.getService() != null ? reservation.getService().getNom() + " (" + nf.format(reservation.getService().getPrix()) + " FCFA)" : "",
                    reservation.getDateReservation().format(formatter),
                    reservation.getStatut()
                });
                
                // Vérifier si une facture est nécessaire
                if (reservation.getStatut().equals("termine")) {
                    // Vérifier si une facture existe déjà pour cette réservation
                    boolean factureExists = factures.stream().anyMatch(f -> 
                        f.getClient().getId() == reservation.getClient().getId() &&
                        f.getServices().stream().anyMatch(s -> 
                            s.getId() == reservation.getService().getId()
                        )
                    );
                    
                    // Si pas de facture, en créer une
                    if (!factureExists) {
                        createFactureForReservation(reservation.getClient(), reservation.getService());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des réservations: " + e.getMessage());
        }
    }
    
    private void showReservationDialog(Reservation reservation) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            reservation == null ? "Nouvelle Réservation" : "Modifier Réservation", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(240, 248, 255));
        dialog.setSize(420, 300);
        dialog.setResizable(false);
        JPanel contentPanel = new JPanel(new GridLayout(4, 2, 15, 15));
        contentPanel.setBackground(new Color(240, 248, 255));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JLabel titleLabel = new JLabel(reservation == null ? "Nouvelle Réservation" : "Modifier Réservation");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(30, 60, 120));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.add(titleLabel, BorderLayout.NORTH);

        JComboBox<Client> clientCombo = new JComboBox<>();
        try {
            List<Client> clients = clientDAO.getAllClients();
            for (Client client : clients) {
                clientCombo.addItem(client);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Erreur lors du chargement des clients: " + e.getMessage());
        }
        clientCombo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        clientCombo.setBackground(Color.WHITE);
        clientCombo.setBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 1));

        JComboBox<Service> serviceCombo = new JComboBox<>();
        try {
            List<Service> services = serviceDAO.getAllServices();
            for (Service service : services) {
                serviceCombo.addItem(service);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Erreur lors du chargement des services: " + e.getMessage());
        }
        serviceCombo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        serviceCombo.setBackground(Color.WHITE);
        serviceCombo.setBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 1));

        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy HH:mm");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        dateSpinner.setBackground(Color.WHITE);
        dateSpinner.setBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 1));

        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"en attente", "en cours", "termine"});
        if (reservation != null) {
            statusComboBox.setSelectedItem(reservation.getStatut());
        }
        statusComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        statusComboBox.setBackground(Color.WHITE);
        statusComboBox.setBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 1));

        contentPanel.add(new JLabel("Client:", SwingConstants.RIGHT));
        contentPanel.add(clientCombo);
        contentPanel.add(new JLabel("Service:", SwingConstants.RIGHT));
        contentPanel.add(serviceCombo);
        contentPanel.add(new JLabel("Date:", SwingConstants.RIGHT));
        contentPanel.add(dateSpinner);
        contentPanel.add(new JLabel("Statut:", SwingConstants.RIGHT));
        contentPanel.add(statusComboBox);
        dialog.add(contentPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Enregistrer");
        Color btnColor = new Color(100, 149, 237);
        Color btnHover = new Color(65, 105, 225);
        saveButton.setBackground(btnColor);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        saveButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        saveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { saveButton.setBackground(btnHover); }
            public void mouseExited(java.awt.event.MouseEvent evt) { saveButton.setBackground(btnColor); }
        });
        JPanel savePanel = new JPanel();
        savePanel.setBackground(new Color(240, 248, 255));
        savePanel.add(saveButton);
        dialog.add(savePanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> {
            try {
                Client selectedClient = (Client) clientCombo.getSelectedItem();
                Service selectedService = (Service) serviceCombo.getSelectedItem();
                LocalDateTime date = ((java.util.Date) dateSpinner.getValue()).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
                String status = (String) statusComboBox.getSelectedItem();

                if (reservation == null) {
                    // Nouvelle réservation
                    Reservation newReservation = new Reservation(0, selectedClient, selectedService, date, status);
                    reservationDAO.addReservation(newReservation);
                    if (status.equals("termine")) {
                        createFactureForReservation(selectedClient, selectedService);
                    }
                } else {
                    // Modification de réservation
                    String oldStatus = reservation.getStatut();
                    reservation.setClient(selectedClient);
                    reservation.setService(selectedService);
                    reservation.setDateReservation(date);
                    reservation.setStatut(status);
                    reservationDAO.updateReservation(reservation);
                    
                    // Si le statut passe à "termine", créer une facture
                    if (status.equals("termine") && !oldStatus.equals("termine")) {
                        createFactureForReservation(selectedClient, selectedService);
                    }
                }
                loadReservations();
                dialog.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Erreur lors de la sauvegarde: " + ex.getMessage());
            }
        });
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public void createFactureForReservation(Client client, Service service) {
        try {
            FactureDAO factureDAO = new FactureDAO();
            List<Service> services = new ArrayList<>();
            services.add(service);
            Facture newFacture = new Facture(0, client, services, LocalDateTime.now());
            factureDAO.addFacture(newFacture);
            
            // Rafraîchir le panneau des factures si disponible
            if (facturePanel != null) {
                SwingUtilities.invokeLater(() -> {
                    facturePanel.loadFactures();
                });
            }
            
            JOptionPane.showMessageDialog(this, 
                "Réservation terminée. Une facture a été créée automatiquement.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de la création de la facture: " + ex.getMessage());
        }
    }
} 