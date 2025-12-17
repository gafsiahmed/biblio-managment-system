-- Libraries
INSERT IGNORE INTO libraries (id, name, address, phone, created_at) 
VALUES (1, 'Bibliothèque Centrale', '123 Rue de la Paix, Paris', '0123456789', NOW());

-- Users (password is 'password')
INSERT IGNORE INTO users (id, username, email, password, first_name, last_name, role, enabled, email_verified, library_id, created_at) 
VALUES (1, 'admin', 'admin@biblio.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Admin', 'System', 'ROLE_ADMIN', 1, 1, 1, NOW());

INSERT IGNORE INTO users (id, username, email, password, first_name, last_name, role, enabled, email_verified, library_id, created_at) 
VALUES (2, 'user', 'user@biblio.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'John', 'Doe', 'ROLE_USER', 1, 1, 1, NOW());

INSERT IGNORE INTO users (id, username, email, password, first_name, last_name, role, enabled, email_verified, library_id, created_at) 
VALUES (3, 'librarian', 'librarian@biblio.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Jane', 'Smith', 'ROLE_LIBRARIAN', 1, 1, 1, NOW());

-- Resources (Books)
INSERT IGNORE INTO resources (id, resource_type, title, author, isbn, category, available_copies, total_copies, library_id, is_active, created_at, publication_year, description)
VALUES (1, 'BOOK', 'Le Petit Prince', 'Antoine de Saint-Exupéry', '978-0156012195', 'BOOK', 5, 5, 1, 1, NOW(), 1943, 'Un conte poétique et philosophique sous l''apparence d''un conte pour enfants.');

INSERT IGNORE INTO resources (id, resource_type, title, author, isbn, category, available_copies, total_copies, library_id, is_active, created_at, publication_year, description)
VALUES (2, 'BOOK', 'L''Étranger', 'Albert Camus', '978-0679720201', 'BOOK', 3, 3, 1, 1, NOW(), 1942, 'Un roman d''Albert Camus paru en 1942.');

INSERT IGNORE INTO resources (id, resource_type, title, author, isbn, category, available_copies, total_copies, library_id, is_active, created_at, publication_year, description)
VALUES (3, 'BOOK', 'Les Misérables', 'Victor Hugo', '978-0451419439', 'BOOK', 2, 2, 1, 1, NOW(), 1862, 'Un roman de Victor Hugo paru en 1862.');

INSERT IGNORE INTO resources (id, resource_type, title, author, isbn, category, available_copies, total_copies, library_id, is_active, created_at, publication_year, description)
VALUES (4, 'BOOK', '1984', 'George Orwell', '978-0451524935', 'BOOK', 4, 4, 1, 1, NOW(), 1949, 'Un roman dystopique de George Orwell publié en 1949.');

