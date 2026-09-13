-- 1. Create the database (if it doesn't exist)
CREATE DATABASE IF NOT EXISTS timetable_db;

-- 2. Use the new database
USE timetable_db;

-- 3. Create the tables based on your ER diagram

-- Department Table
CREATE TABLE IF NOT EXISTS Department (
    dep_id VARCHAR(50) PRIMARY KEY,
    dep_name VARCHAR(255) NOT NULL
);

-- Teacher Table
CREATE TABLE IF NOT EXISTS Teacher (
    teacher_id VARCHAR(50) PRIMARY KEY,
    teacher_name VARCHAR(255) NOT NULL,
    dep_id VARCHAR(50),
    available_days VARCHAR(255),  -- e.g., "Monday,Tuesday,Friday"
    available_slots VARCHAR(255), -- e.g., "1,2,4"
    FOREIGN KEY (dep_id) REFERENCES Department(dep_id)
);

-- Classroom Table
CREATE TABLE IF NOT EXISTS Classroom (
    class_id VARCHAR(50) PRIMARY KEY,
    class_name VARCHAR(255),
    capacity INT NOT NULL,
    dep_id VARCHAR(50),
    FOREIGN KEY (dep_id) REFERENCES Department(dep_id)
);

-- Subject Table
CREATE TABLE IF NOT EXISTS Subject (
    sub_id VARCHAR(50) PRIMARY KEY,
    sub_name VARCHAR(255) NOT NULL,
    dep_id VARCHAR(50),
    teacher_id VARCHAR(50),
    sem INT,
    hours_per_week INT,
    FOREIGN KEY (dep_id) REFERENCES Department(dep_id),
    FOREIGN KEY (teacher_id) REFERENCES Teacher(teacher_id)
);

-- Timeslot Table
CREATE TABLE IF NOT EXISTS Timeslot (
    slot_id VARCHAR(50) PRIMARY KEY,
    day VARCHAR(100) NOT NULL, -- e.g., "Monday"
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
);

-- Timetable Table (This is the output table)
CREATE TABLE IF NOT EXISTS Timetable (
    timetable_id INT AUTO_INCREMENT PRIMARY KEY,
    class_id VARCHAR(50),
    subject_id VARCHAR(50),
    teacher_id VARCHAR(50),
    slot_id VARCHAR(50),
    day VARCHAR(100),
    FOREIGN KEY (class_id) REFERENCES Classroom(class_id),
    FOREIGN KEY (subject_id) REFERENCES Subject(sub_id),
    FOREIGN KEY (teacher_id) REFERENCES Teacher(teacher_id),
    FOREIGN KEY (slot_id) REFERENCES Timeslot(slot_id)
);

-- You can add some sample data to start
INSERT INTO Department (dep_id, dep_name) VALUES
('CSE-AIML-01', 'CSE (AI & ML)'),
('CSE-CORE-02', 'CSE (Core)');