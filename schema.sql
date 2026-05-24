CREATE DATABASE IF NOT EXISTS bookease_db;
USE bookease_db;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    location VARCHAR(100) DEFAULT 'Davao City',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE branches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    location VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Services table
CREATE TABLE services (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Appointments table
CREATE TABLE appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    branch_id INT NOT NULL,
    service_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time VARCHAR(20) NOT NULL,
    notes TEXT,
    status ENUM('confirmed','cancelled','completed') DEFAULT 'confirmed',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (branch_id) REFERENCES branches(id),
    FOREIGN KEY (service_id) REFERENCES services(id)
);

-- Branches
INSERT INTO branches (name, address, location) VALUES
('Metrobank Bajada', 'Bajada, Davao City', 'Davao City'),
('BDO Buhangin Branch', 'Buhangin, Davao City', 'Davao City'),
('BPI Abreeza Branch', 'JP Laurel Ave, Davao City', 'Davao City'),
('Landbank Davao Main', 'Claro M. Recto St, Davao City', 'Davao City'),
('PNB Davao Branch', 'San Pedro St, Davao City', 'Davao City');
('Security Bank Davao',    'Quimpo Blvd, Davao City',         'Davao City'),
('Unionbank Davao',        'Ilustre St, Davao City',          'Davao City'),
('RCBC Davao Branch',      'CM Recto Ave, Davao City',        'Davao City'),
('EastWest Bank Davao',    'Magsaysay Ave, Davao City',       'Davao City'),
('Chinabank Davao',        'Anda St, Davao City',             'Davao City'),
('DBP Davao Branch',       'JP Laurel Ave, Davao City',       'Davao City'),
('SSS Davao Branch',       'Quimpo Blvd, Davao City',         'Davao City'),
('PhilHealth Davao',       'Monteverde St, Davao City',       'Davao City'),
('Pag-IBIG Davao',         'Dacudao Ave, Davao City',         'Davao City'),
('NBI Davao District Office', 'Arellano St, Davao City',      'Davao City'),
('DFA Davao Consular Office', 'Ecoland, Davao City',          'Davao City'),
('BIR Davao RDO 113',      'San Pedro St, Davao City',        'Davao City'),
('DOST Region XI',         'Claveria St, Davao City',         'Davao City'),
('GSIS Davao Branch',      'Claro M Recto St, Davao City',   'Davao City'),
('LTO Davao District',     'JP Laurel Ave, Davao City',       'Davao City');


-- Services
INSERT INTO services (name, description) VALUES
('Account Opening', 'Open a new bank account'),
('Loan Application', 'Apply for personal or business loan'),
('Card Services', 'Credit/debit card application or concerns'),
('Fund Transfer', 'Inter-bank or local fund transfers'),
('Foreign Exchange', 'Currency exchange services'),
('Document Request', 'Bank certificates and statements'),
('Investment Inquiry', 'Learn about investment products'),
('General Inquiry', 'Any banking-related concern');
('Online Banking Enrollment'),
('ATM Card Replacement'),
('Bank Certification'),
('Time Deposit Opening'),
('Remittance / Cash Pickup'),
('Checkbook Request'),
('Stop Payment Order'),
('Safe Deposit Box'),
('Investment Consultation'),
('Insurance Inquiry'),
('SSS Contribution Inquiry'),
('SSS Loan Application'),
('PhilHealth MDR Update'),
('PhilHealth Benefit Claim'),
('Pag-IBIG Membership'),
('Pag-IBIG Housing Loan'),
('NBI Clearance'),
('Passport Application'),
('Passport Renewal'),
('BIR TIN Application'),
('BIR Tax Clearance'),
('LTO Driver License Renewal'),
('LTO Vehicle Registration'),
('GSIS Loan Application'),
('DOST Scholarship Inquiry');