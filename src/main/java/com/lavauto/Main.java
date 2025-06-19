package com.lavauto;

import com.lavauto.ui.ServicePanel;
import com.lavauto.ui.ClientPanel;
import com.lavauto.ui.ReservationPanel;
import com.lavauto.ui.FacturePanel;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Gestion de Lavage Auto");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);
            
            // Création du panneau principal avec onglets
            JTabbedPane tabbedPane = new JTabbedPane();
            
            // Ajout des panneaux
            tabbedPane.addTab("Services", new ServicePanel());
            tabbedPane.addTab("Clients", new ClientPanel());
            tabbedPane.addTab("Réservations", new ReservationPanel());
            tabbedPane.addTab("Factures", new FacturePanel());
            
            frame.add(tabbedPane);
            frame.setVisible(true);
        });
    }
} 