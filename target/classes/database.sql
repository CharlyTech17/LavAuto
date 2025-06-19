-- Création de la base de données
CREATE DATABASE IF NOT EXISTS lavauto;
USE lavauto;

-- Table des services
CREATE TABLE IF NOT EXISTS services (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    description TEXT,
    prix DECIMAL(10,2) NOT NULL
);

-- Table des clients
CREATE TABLE IF NOT EXISTS clients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    telephone VARCHAR(20)
);

-- Table des réservations
CREATE TABLE IF NOT EXISTS reservations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    service_id INT NOT NULL,
    date_reservation DATETIME NOT NULL,
    statut ENUM('en_attente', 'en_cours', 'termine', 'annule') DEFAULT 'en_attente',
    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (service_id) REFERENCES services(id)
);

-- Table des factures
CREATE TABLE IF NOT EXISTS factures (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    date_facturation DATETIME NOT NULL,
    payee BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

-- Table de liaison entre factures et services
CREATE TABLE IF NOT EXISTS facture_services (
    facture_id INT NOT NULL,
    service_id INT NOT NULL,
    PRIMARY KEY (facture_id, service_id),
    FOREIGN KEY (facture_id) REFERENCES factures(id),
    FOREIGN KEY (service_id) REFERENCES services(id)
);

-- Insertion de quelques services par défaut
INSERT INTO services (nom, description, prix) VALUES
('Lavage extérieur', 'Lavage complet de l\'extérieur du véhicule', 15.00),
('Lavage intérieur', 'Nettoyage complet de l\'intérieur du véhicule', 20.00),
('Lavage complet', 'Lavage intérieur et extérieur', 30.00),
('Cire', 'Application de cire sur la carrosserie', 25.00); 