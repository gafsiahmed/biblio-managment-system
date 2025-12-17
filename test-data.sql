-- Initial Data for Testing

-- Libraries
INSERT INTO libraries (id, name, address, phone) VALUES (1, 'Bibliothèque Centrale', '123 Rue de la Paix, Paris', '0123456789');

-- Users (password: password)
INSERT INTO users (id, username, email, password, first_name, last_name, role, enabled, email_verified, library_id) 
VALUES (1, 'admin', 'admin@biblio.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Admin', 'System', 'ROLE_ADMIN', true, true, 1);

INSERT INTO users (id, username, email, password, first_name, last_name, role, enabled, email_verified, library_id) 
VALUES (2, 'librarian', 'librarian@biblio.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Marie', 'Curie', 'ROLE_LIBRARIAN', true, true, 1);

INSERT INTO users (id, username, email, password, first_name, last_name, role, enabled, email_verified) 
VALUES (3, 'user', 'user@biblio.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Jean', 'Dupont', 'ROLE_USER', true, true);

-- Resources
INSERT INTO resources (resource_type, title, author, isbn, category, available_copies, total_copies, library_id, is_active)
VALUES ('BOOK', 'Les Misérables', 'Victor Hugo', '978-0140444308', 'BOOK', 5, 5, 1, true);

INSERT INTO resources (resource_type, title, author, isbn, category, available_copies, total_copies, library_id, is_active)
VALUES ('BOOK', '1984', 'George Orwell', '978-0451524935', 'BOOK', 3, 3, 1, true);

INSERT INTO resources (resource_type, title, author, isbn, category, available_copies, total_copies, library_id, is_active)
VALUES ('BOOK', 'Le Petit Prince', 'Antoine de Saint-Exupéry', '978-0156012195', 'BOOK', 2, 2, 1, true);
