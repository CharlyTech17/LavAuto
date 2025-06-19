package com.lavauto.ui;

import com.lavauto.dao.ServiceDAO;
import com.lavauto.model.Service;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ServicePanel extends JPanel {
    private JTable serviceTable;
    private DefaultTableModel tableModel;
    private ServiceDAO serviceDAO;
    
    public ServicePanel() {
        serviceDAO = new ServiceDAO();
        setLayout(new BorderLayout());
        setBackground(new Color(240, 248, 255)); // Bleu très clair
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("Gestion des Services");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(30, 60, 120));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        
        // Création du modèle de table
        String[] columns = {"ID", "Nom", "Description", "Prix"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Création de la table
        serviceTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(serviceTable);
        
        // Panneau de boutons
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Ajouter");
        JButton editButton = new JButton("Modifier");
        JButton deleteButton = new JButton("Supprimer");
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        // Style du tableau
        serviceTable.getTableHeader().setBackground(new Color(30, 60, 120));
        serviceTable.getTableHeader().setForeground(Color.WHITE);
        serviceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        serviceTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        serviceTable.setRowHeight(24);
        
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
        loadServices();
        
        // Gestion des événements
        addButton.addActionListener(e -> showServiceDialog(null));
        editButton.addActionListener(e -> {
            int selectedRow = serviceTable.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                String nom = (String) tableModel.getValueAt(selectedRow, 1);
                String description = (String) tableModel.getValueAt(selectedRow, 2);
                String prixStr = (String) tableModel.getValueAt(selectedRow, 3);
                double prix = Double.parseDouble(prixStr.replace(" FCFA", "").replace(" ", ""));
                Service service = new Service(id, nom, description, prix);
                showServiceDialog(service);
            }
        });
        
        deleteButton.addActionListener(e -> {
            int selectedRow = serviceTable.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                try {
                    serviceDAO.deleteService(id);
                    loadServices();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Erreur lors de la suppression: " + ex.getMessage());
                }
            }
        });
    }
    
    private void loadServices() {
        tableModel.setRowCount(0);
        try {
            List<Service> services = serviceDAO.getAllServices();
            NumberFormat nf = NumberFormat.getInstance(Locale.FRANCE);
            for (Service service : services) {
                tableModel.addRow(new Object[]{
                    service.getId(),
                    service.getNom(),
                    service.getDescription(),
                    nf.format(service.getPrix()) + " FCFA"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des services: " + e.getMessage());
        }
    }
    
    private void showServiceDialog(Service service) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            service == null ? "Nouveau Service" : "Modifier Service", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(240, 248, 255));
        dialog.setSize(400, 250);
        dialog.setResizable(false);
        JPanel contentPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        contentPanel.setBackground(new Color(240, 248, 255));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JLabel titleLabel = new JLabel(service == null ? "Nouveau Service" : "Modifier Service");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(30, 60, 120));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.add(titleLabel, BorderLayout.NORTH);

        JTextField nomField = new JTextField(service != null ? service.getNom() : "");
        JTextField descriptionField = new JTextField(service != null ? service.getDescription() : "");
        JTextField prixField = new JTextField(service != null ? String.valueOf(service.getPrix()) : "");
        JTextField[] fields = {nomField, descriptionField, prixField};
        for (JTextField field : fields) {
            field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            field.setBackground(Color.WHITE);
            field.setBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 1));
        }
        contentPanel.add(new JLabel("Nom:", SwingConstants.RIGHT));
        contentPanel.add(nomField);
        contentPanel.add(new JLabel("Description:", SwingConstants.RIGHT));
        contentPanel.add(descriptionField);
        contentPanel.add(new JLabel("Prix (FCFA):", SwingConstants.RIGHT));
        contentPanel.add(prixField);
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
                double prix = Double.parseDouble(prixField.getText());
                Service newService = new Service(
                    service != null ? service.getId() : 0,
                    nomField.getText(),
                    descriptionField.getText(),
                    prix
                );
                if (service == null) {
                    serviceDAO.addService(newService);
                } else {
                    serviceDAO.updateService(newService);
                }
                loadServices();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Le prix doit être un nombre valide");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur lors de l'enregistrement: " + ex.getMessage());
            }
        });
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
} 