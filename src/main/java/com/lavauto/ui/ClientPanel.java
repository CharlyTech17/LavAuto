package com.lavauto.ui;

import com.lavauto.dao.ClientDAO;
import com.lavauto.model.Client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ClientPanel extends JPanel {
    private JTable clientTable;
    private DefaultTableModel tableModel;
    private ClientDAO clientDAO;
    
    public ClientPanel() {
        clientDAO = new ClientDAO();
        setLayout(new BorderLayout());
        setBackground(new Color(240, 248, 255)); // Bleu très clair
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("Gestion des Clients");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(30, 60, 120));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        
        // Création du modèle de table
        String[] columns = {"ID", "Nom", "Prénom", "Email", "Téléphone"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Création de la table
        clientTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(clientTable);
        
        // Panneau de boutons
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Ajouter");
        JButton editButton = new JButton("Modifier");
        JButton deleteButton = new JButton("Supprimer");
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        // Style du tableau
        clientTable.getTableHeader().setBackground(new Color(30, 60, 120));
        clientTable.getTableHeader().setForeground(Color.WHITE);
        clientTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        clientTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clientTable.setRowHeight(24);
        
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
        loadClients();
        
        // Gestion des événements
        addButton.addActionListener(e -> showClientDialog(null));
        editButton.addActionListener(e -> {
            int selectedRow = clientTable.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                String nom = (String) tableModel.getValueAt(selectedRow, 1);
                String prenom = (String) tableModel.getValueAt(selectedRow, 2);
                String email = (String) tableModel.getValueAt(selectedRow, 3);
                String telephone = (String) tableModel.getValueAt(selectedRow, 4);
                Client client = new Client(id, nom, prenom, email, telephone);
                showClientDialog(client);
            }
        });
        
        deleteButton.addActionListener(e -> {
            int selectedRow = clientTable.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                try {
                    clientDAO.deleteClient(id);
                    loadClients();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Erreur lors de la suppression: " + ex.getMessage());
                }
            }
        });
    }
    
    private void loadClients() {
        tableModel.setRowCount(0);
        try {
            List<Client> clients = clientDAO.getAllClients();
            for (Client client : clients) {
                tableModel.addRow(new Object[]{
                    client.getId(),
                    client.getNom(),
                    client.getPrenom(),
                    client.getEmail(),
                    client.getTelephone()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des clients: " + e.getMessage());
        }
    }
    
    private void showClientDialog(Client client) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            client == null ? "Nouveau Client" : "Modifier Client", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(240, 248, 255));
        dialog.setSize(400, 300);
        dialog.setResizable(false);
        JPanel contentPanel = new JPanel(new GridLayout(4, 2, 15, 15));
        contentPanel.setBackground(new Color(240, 248, 255));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JLabel titleLabel = new JLabel(client == null ? "Nouveau Client" : "Modifier Client");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(30, 60, 120));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.add(titleLabel, BorderLayout.NORTH);

        JTextField nomField = new JTextField(client != null ? client.getNom() : "");
        JTextField prenomField = new JTextField(client != null ? client.getPrenom() : "");
        JTextField emailField = new JTextField(client != null ? client.getEmail() : "");
        JTextField telephoneField = new JTextField(client != null ? client.getTelephone() : "");
        JTextField[] fields = {nomField, prenomField, emailField, telephoneField};
        for (JTextField field : fields) {
            field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            field.setBackground(Color.WHITE);
            field.setBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 1));
        }
        contentPanel.add(new JLabel("Nom:", SwingConstants.RIGHT));
        contentPanel.add(nomField);
        contentPanel.add(new JLabel("Prénom:", SwingConstants.RIGHT));
        contentPanel.add(prenomField);
        contentPanel.add(new JLabel("Email:", SwingConstants.RIGHT));
        contentPanel.add(emailField);
        contentPanel.add(new JLabel("Téléphone:", SwingConstants.RIGHT));
        contentPanel.add(telephoneField);
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
                Client newClient = new Client(
                    client != null ? client.getId() : 0,
                    nomField.getText(),
                    prenomField.getText(),
                    emailField.getText(),
                    telephoneField.getText()
                );
                if (client == null) {
                    clientDAO.addClient(newClient);
                } else {
                    clientDAO.updateClient(newClient);
                }
                loadClients();
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur lors de l'enregistrement: " + ex.getMessage());
            }
        });
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
} 