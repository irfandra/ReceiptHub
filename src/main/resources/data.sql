-- ReceiptHub Database Initialization Script
-- Tables are automatically created by Spring Boot JPA
-- This script only inserts initial data

-- Insert sample users with BCrypt hashed passwords
-- Password for all users: "password"
INSERT IGNORE INTO users (name, email, phone_number, telegram_chat_id, role, password, created_at) VALUES
('Admin User', 'admin@receipthub.com', '+6599999999', null, 'ADMIN', '$2a$10$3g4uHW9UY1ybAzEXqAL62eQ8AfbJltCwvF2SbW2gDRDztPMh3ZzKu', NOW()),
('Irfan', 'irfan@company.com', '+6591234567', null, 'EMPLOYEE', '$2a$10$3g4uHW9UY1ybAzEXqAL62eQ8AfbJltCwvF2SbW2gDRDztPMh3ZzKu', NOW()),
('Jane Smith', 'jane.smith@company.com', '+6588888888', null, 'EMPLOYEE', '$2a$10$3g4uHW9UY1ybAzEXqAL62eQ8AfbJltCwvF2SbW2gDRDztPMh3ZzKu', NOW());

-- Insert sample receipts with various statuses and dates
INSERT IGNORE INTO receipts (image_url, merchant_name, amount, transaction_date, uploaded_at, ocr_status) VALUES
-- Month 3 (4 months ago) - 10 receipts
('/uploads/receipts/receipt-30.jpg', 'DHL', 35.75, DATE_SUB(NOW(), INTERVAL 4 MONTH), DATE_SUB(NOW(), INTERVAL 4 MONTH), 'COMPLETED'),

-- Month 4 (3 months ago) - 10 receipts
('/uploads/receipts/receipt-31.jpg', 'Newegg', 275.50, DATE_SUB(NOW(), INTERVAL 3 MONTH), DATE_SUB(NOW(), INTERVAL 3 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-32.jpg', 'Peets Coffee', 10.50, DATE_SUB(NOW(), INTERVAL 3 MONTH), DATE_SUB(NOW(), INTERVAL 3 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-33.jpg', 'Taxi Service', 42.00, DATE_SUB(NOW(), INTERVAL 3 MONTH), DATE_SUB(NOW(), INTERVAL 3 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-34.jpg', 'eBay', 156.80, DATE_SUB(NOW(), INTERVAL 3 MONTH), DATE_SUB(NOW(), INTERVAL 3 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-35.jpg', 'Whole Foods', 112.60, DATE_SUB(NOW(), INTERVAL 3 MONTH), DATE_SUB(NOW(), INTERVAL 3 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-36.jpg', 'Rite Aid', 29.95, DATE_SUB(NOW(), INTERVAL 3 MONTH), DATE_SUB(NOW(), INTERVAL 3 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-37.jpg', 'HP Store', 225.00, DATE_SUB(NOW(), INTERVAL 3 MONTH), DATE_SUB(NOW(), INTERVAL 3 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-38.jpg', 'Exxon', 55.20, DATE_SUB(NOW(), INTERVAL 3 MONTH), DATE_SUB(NOW(), INTERVAL 3 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-39.jpg', 'Pizza Hut', 26.40, DATE_SUB(NOW(), INTERVAL 3 MONTH), DATE_SUB(NOW(), INTERVAL 3 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-40.jpg', 'USPS', 18.90, DATE_SUB(NOW(), INTERVAL 3 MONTH), DATE_SUB(NOW(), INTERVAL 3 MONTH), 'COMPLETED'),

-- Month 5 (2 months ago) - 10 receipts
('/uploads/receipts/receipt-41.jpg', 'B&H Photo', 340.00, DATE_SUB(NOW(), INTERVAL 2 MONTH), DATE_SUB(NOW(), INTERVAL 2 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-42.jpg', 'Tim Hortons', 9.85, DATE_SUB(NOW(), INTERVAL 2 MONTH), DATE_SUB(NOW(), INTERVAL 2 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-43.jpg', 'Grab', 28.50, DATE_SUB(NOW(), INTERVAL 2 MONTH), DATE_SUB(NOW(), INTERVAL 2 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-44.jpg', 'Newegg', 178.90, DATE_SUB(NOW(), INTERVAL 2 MONTH), DATE_SUB(NOW(), INTERVAL 2 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-45.jpg', 'Safeway', 88.75, DATE_SUB(NOW(), INTERVAL 2 MONTH), DATE_SUB(NOW(), INTERVAL 2 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-46.jpg', 'Duane Reade', 35.60, DATE_SUB(NOW(), INTERVAL 2 MONTH), DATE_SUB(NOW(), INTERVAL 2 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-47.jpg', 'Lenovo Store', 399.99, DATE_SUB(NOW(), INTERVAL 2 MONTH), DATE_SUB(NOW(), INTERVAL 2 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-48.jpg', 'Mobil', 46.80, DATE_SUB(NOW(), INTERVAL 2 MONTH), DATE_SUB(NOW(), INTERVAL 2 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-49.jpg', 'KFC', 19.95, DATE_SUB(NOW(), INTERVAL 2 MONTH), DATE_SUB(NOW(), INTERVAL 2 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-50.jpg', 'FedEx Office', 24.50, DATE_SUB(NOW(), INTERVAL 2 MONTH), DATE_SUB(NOW(), INTERVAL 2 MONTH), 'COMPLETED'),

-- Month 6 (1 month ago) - 10 receipts
('/uploads/receipts/receipt-51.jpg', 'Micro Center', 285.00, DATE_SUB(NOW(), INTERVAL 1 MONTH), DATE_SUB(NOW(), INTERVAL 1 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-52.jpg', 'Dutch Bros', 7.50, DATE_SUB(NOW(), INTERVAL 1 MONTH), DATE_SUB(NOW(), INTERVAL 1 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-53.jpg', 'Via', 31.75, DATE_SUB(NOW(), INTERVAL 1 MONTH), DATE_SUB(NOW(), INTERVAL 1 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-54.jpg', 'Etsy', 95.60, DATE_SUB(NOW(), INTERVAL 1 MONTH), DATE_SUB(NOW(), INTERVAL 1 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-55.jpg', 'Trader Joes', 72.30, DATE_SUB(NOW(), INTERVAL 1 MONTH), DATE_SUB(NOW(), INTERVAL 1 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-56.jpg', 'Bartell Drugs', 41.20, DATE_SUB(NOW(), INTERVAL 1 MONTH), DATE_SUB(NOW(), INTERVAL 1 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-57.jpg', 'Samsung Store', 349.00, DATE_SUB(NOW(), INTERVAL 1 MONTH), DATE_SUB(NOW(), INTERVAL 1 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-58.jpg', 'Arco', 39.90, DATE_SUB(NOW(), INTERVAL 1 MONTH), DATE_SUB(NOW(), INTERVAL 1 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-59.jpg', 'Taco Bell', 16.80, DATE_SUB(NOW(), INTERVAL 1 MONTH), DATE_SUB(NOW(), INTERVAL 1 MONTH), 'COMPLETED'),
('/uploads/receipts/receipt-60.jpg', 'Postal Annex', 21.40, DATE_SUB(NOW(), INTERVAL 1 MONTH), DATE_SUB(NOW(), INTERVAL 1 MONTH), 'COMPLETED');

-- Insert 60 reimbursement requests (20 PENDING, 20 APPROVED, 20 REJECTED)
-- Distributed between Irfan (ID 2) and Jane (ID 3)

-- Irfan (ID 2) - 30 requests (10 PENDING, 10 APPROVED, 10 REJECTED)
INSERT IGNORE INTO reimbursement_requests (receipt_id, submitted_by, requested_amount, description, status, submitted_at, reviewed_by, reviewed_at, review_notes) VALUES
(1, 2, 35.75, 'Office supplies for team meeting', 'APPROVED', DATE_SUB(NOW(), INTERVAL 4 MONTH), 1, DATE_SUB(NOW(), INTERVAL 4 MONTH), 'Approved'),
(2, 2, 275.50, 'Client coffee meeting', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(3, 2, 10.50, 'Printer supplies', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(4, 2, 42.00, 'Team lunch', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(5, 2, 156.80, 'Office equipment', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(6, 2, 112.60, 'Morning coffee for client', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(7, 2, 29.95, 'Computer accessories', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(8, 2, 225.00, 'Coffee supplies', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(9, 2, 55.20, 'Camera equipment for project', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(10, 2, 26.40, 'Client breakfast', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(11, 2, 18.90, 'IT equipment', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Not business related'),
(12, 2, 340.00, 'Team coffee', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Exceeds allowance'),
(13, 2, 9.85, 'Transportation', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Personal purchase'),
(14, 2, 28.50, 'Project supplies', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Not approved'),
(15, 2, 178.90, 'Client meeting', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Duplicate claim'),
(16, 2, 88.75, 'Office materials', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Not authorized'),
(17, 2, 35.60, 'Late delivery', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Should use company car'),
(18, 2, 399.99, 'Office items', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Not necessary'),
(19, 2, 46.80, 'Client visit', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Over budget'),
(20, 2, 19.95, 'Monitor', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Personal expense'),
(21, 2, 24.50, 'Lunch meeting', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),
(22, 2, 285.00, 'Transport', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),
(23, 2, 7.50, 'Materials', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),
(24, 2, 31.75, 'Equipment', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),
(25, 2, 95.60, 'Supplies', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),
(26, 2, 72.30, 'Meeting', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),
(27, 2, 41.20, 'Items', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),
(28, 2, 349.00, 'Project', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),
(29, 2, 39.90, 'Expense', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),
(30, 2, 16.80, 'Cost', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),

-- Jane Smith (ID 3) - 30 requests (10 PENDING, 10 APPROVED, 10 REJECTED)
(31, 3, 275.50, 'Office supplies', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(32, 3, 10.50, 'Client meeting', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(33, 3, 42.00, 'Transport', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(34, 3, 156.80, 'Materials', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(35, 3, 112.60, 'Equipment', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(36, 3, 29.95, 'Supplies', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(37, 3, 225.00, 'Meeting', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(38, 3, 55.20, 'Project', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(39, 3, 26.40, 'Expense', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(40, 3, 18.90, 'Cost', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 MONTH), 1, DATE_SUB(NOW(), INTERVAL 3 MONTH), 'Approved'),
(41, 3, 340.00, 'Items', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Not approved'),
(42, 3, 9.85, 'Goods', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Personal'),
(43, 3, 28.50, 'Purchase', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Duplicate'),
(44, 3, 178.90, 'Order', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Unauthorized'),
(45, 3, 88.75, 'Request', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Over budget'),
(46, 3, 35.60, 'Claim', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Not business'),
(47, 3, 399.99, 'Reimbursement', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Too expensive'),
(48, 3, 46.80, 'Payment', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Personal use'),
(49, 3, 19.95, 'Bill', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Not reimbursable'),
(50, 3, 24.50, 'Invoice', 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 MONTH), 1, DATE_SUB(NOW(), INTERVAL 2 MONTH), 'Invalid'),
(51, 3, 285.00, 'Office supplies', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),
(52, 3, 7.50, 'Client coffee', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),
(53, 3, 31.75, 'Transport', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),
(54, 3, 95.60, 'Materials', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),
(55, 3, 72.30, 'Equipment', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),
(56, 3, 41.20, 'Supplies', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),
(57, 3, 349.00, 'Meeting', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),
(58, 3, 39.90, 'Project', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),
(59, 3, 16.80, 'Expense', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL),
(60, 3, 21.40, 'Cost', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 MONTH), NULL, NULL, NULL);

-- Display initialization message
SELECT 'Database initialized successfully!' AS message;
SELECT CONCAT('Total users: ', COUNT(*)) AS info FROM users;
SELECT CONCAT('Total receipts: ', COUNT(*)) AS info FROM receipts;
SELECT CONCAT('Total requests: ', COUNT(*)) AS info FROM reimbursement_requests;
