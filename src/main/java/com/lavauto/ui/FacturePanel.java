package com.lavauto.ui;

import com.lavauto.dao.FactureDAO;
import com.lavauto.dao.ClientDAO;
import com.lavauto.dao.ServiceDAO;
import com.lavauto.model.Facture;
import com.lavauto.model.Client;
import com.lavauto.model.Service;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class FacturePanel extends JPanel {
    private JTable factureTable;
    private DefaultTableModel tableModel;
    private FactureDAO factureDAO;
    private ClientDAO clientDAO;
    private ServiceDAO serviceDAO;
    
    public FacturePanel() {
        setBackground(new Color(240, 248, 255)); // Bleu très clair
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("Gestion des Factures");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(30, 60, 120));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        
        factureDAO = new FactureDAO();
        clientDAO = new ClientDAO();
        serviceDAO = new ServiceDAO();
        setLayout(new BorderLayout());
        
        // Création du modèle de table
        String[] columns = {"ID", "Client", "Date", "Montant", "Payée"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Création de la table
        factureTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(factureTable);
        
        // Panneau de boutons
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Ajouter");
        JButton editButton = new JButton("Modifier");
        JButton deleteButton = new JButton("Supprimer");
        JButton viewButton = new JButton("Voir détails");
        JButton payButton = new JButton("Payer");
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(payButton);
        
        // Ajout des composants
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Chargement des données
        loadFactures();
        
        // Gestion des événements
        addButton.addActionListener(e -> showFactureDialog(null));
        editButton.addActionListener(e -> {
            int selectedRow = factureTable.getSelectedRow();
            if (selectedRow >= 0) {
                try {
                    int id = (int) tableModel.getValueAt(selectedRow, 0);
                    Facture facture = factureDAO.getFactureById(id);
                    if (facture != null) {
                        showFactureDialog(facture);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erreur lors de la récupération de la facture: " + ex.getMessage());
                }
            }
        });
        
        deleteButton.addActionListener(e -> {
            int selectedRow = factureTable.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                try {
                    factureDAO.deleteFacture(id);
                    loadFactures();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erreur lors de la suppression: " + ex.getMessage());
                }
            }
        });
        
        viewButton.addActionListener(e -> {
            int selectedRow = factureTable.getSelectedRow();
            if (selectedRow >= 0) {
                try {
                    int id = (int) tableModel.getValueAt(selectedRow, 0);
                    Facture facture = factureDAO.getFactureById(id);
                    if (facture != null) {
                        showFactureDetails(facture);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erreur lors de la récupération de la facture: " + ex.getMessage());
                }
            }
        });
        
        payButton.addActionListener(e -> {
            int selectedRow = factureTable.getSelectedRow();
            if (selectedRow >= 0) {
                try {
                    int id = (int) tableModel.getValueAt(selectedRow, 0);
                    Facture facture = factureDAO.getFactureById(id);
                    if (facture != null) {
                        if (!facture.isPayee()) {
                            int confirm = JOptionPane.showConfirmDialog(this,
                                "Confirmer le paiement de la facture #" + id + " ?\nMontant : " + 
                                NumberFormat.getInstance(Locale.FRANCE).format(facture.getMontantTotal()) + " FCFA",
                                "Confirmation de paiement",
                                JOptionPane.YES_NO_OPTION);
                            if (confirm == JOptionPane.YES_OPTION) {
                                facture.setPayee(true);
                                factureDAO.updateFacture(facture);
                                loadFactures();
                                JOptionPane.showMessageDialog(this, "Facture payée avec succès !");
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "Cette facture est déjà payée.");
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erreur lors du paiement: " + ex.getMessage());
                }
            }
        });
        
        // Style du tableau
        factureTable.getTableHeader().setBackground(new Color(30, 60, 120));
        factureTable.getTableHeader().setForeground(Color.WHITE);
        factureTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        factureTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        factureTable.setRowHeight(24);
        // Style des boutons
        Color btnColor = new Color(100, 149, 237);
        Color btnHover = new Color(65, 105, 225);
        Color paidColor = new Color(50, 205, 50); // Vert pour le bouton Payer
        Color paidHover = new Color(34, 139, 34);
        JButton[] buttons = {addButton, editButton, deleteButton, viewButton, payButton};
        for (JButton btn : buttons) {
            btn.setBackground(btnColor);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (btn == payButton) {
                        btn.setBackground(paidHover);
                    } else {
                        btn.setBackground(btnHover);
                    }
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (btn == payButton) {
                        btn.setBackground(paidColor);
                    } else {
                        btn.setBackground(btnColor);
                    }
                }
            });
        }
        payButton.setBackground(paidColor);
        buttonPanel.setBackground(new Color(240, 248, 255));
        scrollPane.setBackground(new Color(240, 248, 255));
        
        // Modifier le style du tableau pour les factures payées
        factureTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                boolean isPayee = table.getValueAt(row, 4).equals("Oui");
                if (isPayee) {
                    c.setBackground(new Color(240, 255, 240)); // Vert très clair
                } else {
                    c.setBackground(Color.WHITE);
                }
                if (isSelected) {
                    c.setBackground(new Color(200, 200, 255));
                }
                return c;
            }
        });
    }
    
    /**
     * Charge et affiche toutes les factures dans le tableau
     */
    public void loadFactures() {
        tableModel.setRowCount(0);
        try {
            List<Facture> factures = factureDAO.getAllFactures();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            NumberFormat nf = NumberFormat.getInstance(Locale.FRANCE);
            for (Facture facture : factures) {
                tableModel.addRow(new Object[]{
                    facture.getId(),
                    facture.getClient(),
                    facture.getDateFacturation().format(formatter),
                    nf.format(facture.getMontantTotal()) + " FCFA",
                    facture.isPayee() ? "Oui" : "Non"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des factures: " + e.getMessage());
        }
    }
    
    private void showFactureDialog(Facture facture) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            facture == null ? "Nouvelle Facture" : "Modifier Facture", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(240, 248, 255));
        dialog.setSize(500, 350);
        dialog.setResizable(false);
        JPanel contentPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        contentPanel.setBackground(new Color(240, 248, 255));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JLabel titleLabel = new JLabel(facture == null ? "Nouvelle Facture" : "Modifier Facture");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(30, 60, 120));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.add(titleLabel, BorderLayout.NORTH);

        // Liste déroulante des clients
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

        // Liste des services sélectionnés
        DefaultListModel<Service> selectedServicesModel = new DefaultListModel<>();
        JList<Service> selectedServicesList = new JList<>(selectedServicesModel);
        selectedServicesList.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        selectedServicesList.setBackground(Color.WHITE);
        JScrollPane selectedServicesScroll = new JScrollPane(selectedServicesList);
        selectedServicesScroll.setBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 1));

        // Liste des services disponibles
        DefaultListModel<Service> availableServicesModel = new DefaultListModel<>();
        JList<Service> availableServicesList = new JList<>(availableServicesModel);
        availableServicesList.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        availableServicesList.setBackground(Color.WHITE);
        JScrollPane availableServicesScroll = new JScrollPane(availableServicesList);
        availableServicesScroll.setBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 1));

        try {
            List<Service> services = serviceDAO.getAllServices();
            for (Service service : services) {
                availableServicesModel.addElement(service);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Erreur lors du chargement des services: " + e.getMessage());
        }

        // Boutons pour ajouter/supprimer des services
        JButton addServiceButton = new JButton("Ajouter >");
        JButton removeServiceButton = new JButton("< Supprimer");
        Color btnColor = new Color(100, 149, 237);
        Color btnHover = new Color(65, 105, 225);
        JButton[] serviceBtns = {addServiceButton, removeServiceButton};
        for (JButton btn : serviceBtns) {
            btn.setBackground(btnColor);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(btnHover); }
                public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(btnColor); }
            });
        }
        addServiceButton.addActionListener(e -> {
            Service selected = availableServicesList.getSelectedValue();
            if (selected != null) {
                availableServicesModel.removeElement(selected);
                selectedServicesModel.addElement(selected);
            }
        });
        removeServiceButton.addActionListener(e -> {
            Service selected = selectedServicesList.getSelectedValue();
            if (selected != null) {
                selectedServicesModel.removeElement(selected);
                availableServicesModel.addElement(selected);
            }
        });

        // Panneau pour les services
        JPanel servicesPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        servicesPanel.setBackground(new Color(240, 248, 255));
        servicesPanel.add(availableServicesScroll);
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        buttonsPanel.setBackground(new Color(240, 248, 255));
        buttonsPanel.add(addServiceButton);
        buttonsPanel.add(removeServiceButton);
        servicesPanel.add(buttonsPanel);
        servicesPanel.add(selectedServicesScroll);

        contentPanel.add(new JLabel("Client:", SwingConstants.RIGHT));
        contentPanel.add(clientCombo);
        contentPanel.add(new JLabel("Services:", SwingConstants.RIGHT));
        contentPanel.add(servicesPanel);
        dialog.add(contentPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Enregistrer");
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
                List<Service> selectedServices = new ArrayList<>();
                for (int i = 0; i < selectedServicesModel.size(); i++) {
                    selectedServices.add(selectedServicesModel.getElementAt(i));
                }
                Facture newFacture = new Facture(
                    facture != null ? facture.getId() : 0,
                    selectedClient,
                    selectedServices,
                    LocalDateTime.now()
                );
                if (facture == null) {
                    factureDAO.addFacture(newFacture);
                } else {
                    factureDAO.updateFacture(newFacture);
                }
                loadFactures();
                dialog.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Erreur lors de l'enregistrement: " + ex.getMessage());
            }
        });
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void showFactureDetails(Facture facture) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Détails de la facture #" + facture.getId(), true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(240, 248, 255));
        dialog.setSize(420, 400);
        dialog.setResizable(false);
        JLabel titleLabel = new JLabel("Détails de la facture #" + facture.getId());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(30, 60, 120));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.add(titleLabel, BorderLayout.NORTH);
        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        detailsArea.setBackground(new Color(230, 240, 255));
        detailsArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        NumberFormat nf = NumberFormat.getInstance(Locale.FRANCE);
        StringBuilder details = new StringBuilder();
        details.append("Facture #").append(facture.getId()).append("\n\n");
        details.append("Client: ").append(facture.getClient()).append("\n");
        details.append("Date: ").append(facture.getDateFacturation().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");
        details.append("Services:\n");
        for (Service service : facture.getServices()) {
            details.append("- ").append(service.getNom())
                  .append(" (").append(nf.format(service.getPrix())).append(" FCFA)\n");
        }
        details.append("\nTotal: ").append(nf.format(facture.getMontantTotal())).append(" FCFA\n");
        details.append("Statut: ").append(facture.isPayee() ? "Payée" : "Non payée");
        detailsArea.setText(details.toString());
        dialog.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        JButton closeButton = new JButton("Fermer");
        closeButton.setBackground(new Color(100, 149, 237));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeButton.setBorder(BorderFactory.createEmptyBorder(8, 30, 8, 30));
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { closeButton.setBackground(new Color(65, 105, 225)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { closeButton.setBackground(new Color(100, 149, 237)); }
        });
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 248, 255));
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
} 