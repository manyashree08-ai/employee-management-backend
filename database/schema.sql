-- Employee Management System - MySQL setup script
-- OPTIONAL: the application creates the database and tables automatically on startup
-- (spring.jpa.hibernate.ddl-auto=update + createDatabaseIfNotExist=true).
-- Use this script if you prefer to prepare the database manually:
--   mysql -u root -p < database/schema.sql

CREATE DATABASE IF NOT EXISTS employee_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE employee_db;

-- -------------------------------------------------------------
-- Users table for authentication
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app_users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_app_users_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Optional initial administrator (BCrypt hash of 'admin123')
-- The application also seeds this automatically if the table is empty.
INSERT IGNORE INTO app_users (username, password_hash, full_name)
VALUES ('admin', '$2a$10$wTf31JbX7eI3fN8Rz5Kq.Oq0X3g7Vp4WwEaYzU4L1kK7M9h5B6y2O', 'Administrator');

-- -------------------------------------------------------------
-- Employees table
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS employees (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    employee_id     VARCHAR(20)   NULL,
    full_name       VARCHAR(100)  NOT NULL,
    email           VARCHAR(150)  NOT NULL,
    phone_number    VARCHAR(20)   NOT NULL,
    department      VARCHAR(100)  NOT NULL,
    designation     VARCHAR(100)  NOT NULL,
    date_of_joining DATE          NOT NULL,
    salary          DECIMAL(12,2) NOT NULL,
    status          VARCHAR(10)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_employees_employee_id UNIQUE (employee_id),
    CONSTRAINT uk_employees_email UNIQUE (email),
    CONSTRAINT chk_employees_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    INDEX idx_employee_department (department),
    INDEX idx_employee_status (status),
    INDEX idx_employee_full_name (full_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Optional sample rows (the app also seeds demo data automatically when the table is empty).
-- Remove the "IGNORE" keyword or the block entirely if you do not want them.
INSERT IGNORE INTO employees
    (employee_id, full_name, email, phone_number, department, designation, date_of_joining, salary, status)
VALUES
    ('EMP0001', 'Aarav Sharma', 'aarav.sharma@example.com', '+91 98765 43210', 'Engineering', 'Senior Software Engineer', '2021-03-15', 1800000.00, 'ACTIVE'),
    ('EMP0002', 'Priya Nair',   'priya.nair@example.com',   '+91 98765 43211', 'Engineering', 'Backend Developer',        '2022-07-01', 1200000.00, 'ACTIVE'),
    ('EMP0003', 'Sneha Iyer',   'sneha.iyer@example.com',   '+91 98765 43213', 'Human Resources', 'HR Manager',           '2019-11-20', 1500000.00, 'ACTIVE');
