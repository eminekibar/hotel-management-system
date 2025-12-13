CREATE TABLE IF NOT EXISTS customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(150) UNIQUE,
    phone VARCHAR(20),
    national_id VARCHAR(11) UNIQUE,
    password_hash VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS staff (
    staff_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(150) UNIQUE,
    national_id VARCHAR(11) UNIQUE,
    password_hash VARCHAR(255),
    role VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rooms (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    room_number VARCHAR(20) UNIQUE,
    room_type VARCHAR(50),
    capacity INT,
    price_per_night DECIMAL(10,2),
    status ENUM('available','reserved','occupied','maintenance','inactive') DEFAULT 'available'
);

CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT,
    room_id INT,
    start_date DATE,
    end_date DATE,
    total_price DECIMAL(10,2),
    payment_status ENUM('unpaid','paid','refunded') DEFAULT 'unpaid',
    status ENUM('pending','active','checked_in','completed','canceled') DEFAULT 'pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (room_id) REFERENCES rooms(room_id)
);

CREATE TABLE IF NOT EXISTS reservation_actions (
    action_id INT PRIMARY KEY AUTO_INCREMENT,
    reservation_id INT,
    staff_id INT,
    action_type ENUM('check_in','check_out','cancel'),
    action_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id),
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
);

CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    user_type ENUM('customer','staff'),
    user_id INT,
    message VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE
);

INSERT INTO staff (username, first_name, last_name, email, national_id, password_hash, role) VALUES
('admin', 'Staff', 'Admin', 'staff@mail.com', '90000000001', '10176e7b7b24d317acfcf8d2064cfd2f24e154f7b5a96603077d5ef813d6a6b6', 'admin'),
('staff2', 'Staff', 'Two', 'staff2@mail.com', '90000000002', 'e6c2627cb811ddcf2824dec6c9fdb842d2b48739f53801a018ce80cbbef09086', 'reception');

INSERT INTO rooms (room_number, room_type, capacity, price_per_night, status) VALUES
('101', 'standard', 1, 75.00, 'available'),
('202', 'family', 3, 120.00, 'available'),
('303', 'suite', 4, 250.00, 'maintenance');

INSERT INTO customers (username, first_name, last_name, email, phone, national_id, password_hash, is_active) VALUES
('ayse', 'Ayse', 'Yilmaz', 'ayse.yilmaz@mail.com', '+90-530-111-1111', '10000000001', '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c', TRUE),
('mehmet', 'Mehmet', 'Demir', 'mehmet.demir@mail.com', '+90-532-222-2222', '10000000002', '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c', TRUE),
('elif', 'Elif', 'Kaya', 'elif.kaya@mail.com', '+90-534-333-3333', '10000000003', '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c', TRUE);
