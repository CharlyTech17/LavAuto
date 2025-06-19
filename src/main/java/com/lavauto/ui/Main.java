package com.lavauto.ui;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main main = new Main();
            main.initializeUI();
        });
    }

    private void initializeUI() {
        setTitle("Système de Gestion de Lavage Auto");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Création des panneaux
        ClientPanel clientPanel = new ClientPanel();
        ServicePanel servicePanel = new ServicePanel();
        FacturePanel facturePanel = new FacturePanel();
        ReservationPanel reservationPanel = new ReservationPanel(facturePanel);

        // Création du panneau d'onglets
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Clients", clientPanel);
        tabbedPane.addTab("Services", servicePanel);
        tabbedPane.addTab("Réservations", reservationPanel);
        tabbedPane.addTab("Factures", facturePanel);

        // Style des onglets
        tabbedPane.setBackground(new Color(240, 248, 255));
        tabbedPane.setForeground(new Color(30, 60, 120));
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        add(tabbedPane);
        setVisible(true);
    }
} 