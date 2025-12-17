package com.bibliotheque.repository;

import com.bibliotheque.model.Book;
import com.bibliotheque.model.Loan;
import com.bibliotheque.model.Resource;
import com.bibliotheque.model.User;
import com.bibliotheque.model.enums.Category;
import com.bibliotheque.model.enums.LoanStatus;
import com.bibliotheque.model.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;


import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoanRepository loanRepository;

    @Test
    void testFindByStatus() {
        // Setup User
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setRole(Role.ROLE_USER);
        entityManager.persist(user);

        // Setup Resource
        Resource resource = new Book();
        resource.setTitle("Test Book");
        resource.setCategory(Category.BOOK);
        resource.setAvailableCopies(1);
        entityManager.persist(resource);

        // Setup Loan
        Loan loan = new Loan();
        loan.setUser(user);
        loan.setResource(resource);
        loan.setStatus(LoanStatus.RESERVED);
        loan.setReservationDate(LocalDateTime.now());
        entityManager.persist(loan);

        entityManager.flush();

        List<Loan> found = loanRepository.findByStatus(LoanStatus.RESERVED);

        assertEquals(1, found.size());
        assertEquals(LoanStatus.RESERVED, found.get(0).getStatus());
    }

    @Test
    void testFindByUser() {
        // Setup User
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setRole(Role.ROLE_USER);
        entityManager.persist(user);

        // Setup Resource
        Resource resource = new Book();
        resource.setTitle("Test Book");
        resource.setCategory(Category.BOOK);
        resource.setAvailableCopies(1);
        entityManager.persist(resource);

        // Setup Loan
        Loan loan = new Loan();
        loan.setUser(user);
        loan.setResource(resource);
        loan.setStatus(LoanStatus.IN_PROGRESS);
        loan.setReservationDate(LocalDateTime.now());
        entityManager.persist(loan);

        entityManager.flush();

        List<Loan> found = loanRepository.findByUser(user);

        assertEquals(1, found.size());
        assertEquals(user.getUsername(), found.get(0).getUser().getUsername());
    }
}
