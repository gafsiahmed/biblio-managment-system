package com.bibliotheque.service;

import com.bibliotheque.model.Book;
import com.bibliotheque.model.Library;
import com.bibliotheque.model.Loan;
import com.bibliotheque.model.Resource;
import com.bibliotheque.model.User;
import com.bibliotheque.model.enums.LoanStatus;
import com.bibliotheque.repository.LoanRepository;
import com.bibliotheque.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private ReservationService reservationService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private LoanService loanService;

    @Test
    void testReserve_Success() {
        User user = new User();
        Resource resource = new Book();
        resource.setId(1L);
        resource.setAvailableCopies(5);
        Library library = new Library();
        resource.setLibrary(library);

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan l = invocation.getArgument(0);
            l.setId(100L);
            return l;
        });

        Loan result = loanService.reserve(user, 1L);

        assertNotNull(result);
        assertEquals(LoanStatus.RESERVED, result.getStatus());
        assertEquals(4, resource.getAvailableCopies());
        verify(resourceRepository).save(resource);
    }

    @Test
    void testReserve_NoCopies() {
        User user = new User();
        Resource resource = new Book();
        resource.setId(1L);
        resource.setAvailableCopies(0);

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        assertThrows(IllegalStateException.class, () -> loanService.reserve(user, 1L));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void testReturnLoan_OnTime() {
        User user = new User();
        user.setEmail("test@test.com");
        
        Resource resource = new Book();
        resource.setTitle("Test Book");
        resource.setAvailableCopies(0);

        Loan loan = new Loan();
        loan.setId(1L);
        loan.setUser(user);
        loan.setResource(resource);
        loan.setStatus(LoanStatus.IN_PROGRESS);
        loan.setDueDate(LocalDateTime.now().plusDays(1)); // Due tomorrow

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Loan result = loanService.returnLoan(1L);

        assertEquals(LoanStatus.CLOSED, result.getStatus());
        assertNotNull(result.getActualReturnDate());
        assertEquals(0.0, result.getLateFee());
        assertEquals(1, resource.getAvailableCopies());
        
        verify(reservationService).processReturn(resource);
        verify(emailService).sendReturnConfirmation(eq("test@test.com"), any(), eq("Test Book"));
    }

    @Test
    void testReturnLoan_Late() {
        User user = new User();
        user.setEmail("test@test.com");

        Resource resource = new Book();
        resource.setTitle("Test Book");
        resource.setAvailableCopies(0);

        Loan loan = new Loan();
        loan.setId(1L);
        loan.setUser(user);
        loan.setResource(resource);
        loan.setStatus(LoanStatus.IN_PROGRESS);
        loan.setDueDate(LocalDateTime.now().minusDays(5)); // 5 days late

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Loan result = loanService.returnLoan(1L);

        assertEquals(LoanStatus.RETURNED, result.getStatus()); // Should be RETURNED pending payment
        assertTrue(result.getLateFee() > 0);
        assertEquals(1, resource.getAvailableCopies());
        
        verify(emailService).sendReturnConfirmation(eq("test@test.com"), any(), eq("Test Book"));
    }
}
