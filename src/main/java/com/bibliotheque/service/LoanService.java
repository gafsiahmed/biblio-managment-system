package com.bibliotheque.service;

import com.bibliotheque.model.Loan;
import com.bibliotheque.model.Resource;
import com.bibliotheque.model.User;
import com.bibliotheque.model.enums.LoanStatus;
import com.bibliotheque.repository.LoanRepository;
import com.bibliotheque.repository.ResourceRepository;
import com.bibliotheque.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final ReservationService reservationService;
    private final EmailService emailService;

    public Optional<Loan> findById(Long id) {
        return loanRepository.findById(id);
    }

    public List<Loan> findByUser(User user) {
        return loanRepository.findByUser(user);
    }

    public Page<Loan> findByUser(User user, Pageable pageable) {
        return loanRepository.findByUser(user, pageable);
    }

    public List<Loan> findPendingLoans() {
        return loanRepository.findByStatus(LoanStatus.RESERVED);
    }

    public List<Loan> findOverdueLoans() {
        return loanRepository.findByStatus(LoanStatus.OVERDUE);
    }

    public List<Loan> findAll() {
        return loanRepository.findAll();
    }

    @Transactional
    public Loan updateLoan(Long id, Loan updatedLoan) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
        
        loan.setDueDate(updatedLoan.getDueDate());
        loan.setStatus(updatedLoan.getStatus());
        loan.setLateFee(updatedLoan.getLateFee());
        
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan reserve(User user, Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        if (resource.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No copies available for reservation");
        }

        resource.setAvailableCopies(resource.getAvailableCopies() - 1);
        resourceRepository.save(resource);

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setResource(resource);
        loan.setLibrary(resource.getLibrary());
        loan.setReservationDate(LocalDateTime.now());
        loan.setStatus(LoanStatus.RESERVED);
        
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan approve(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
        
        transitionStatus(loan, LoanStatus.IN_PROGRESS);
        
        loan.setLoanDate(LocalDateTime.now());
        loan.setDueDate(calculateDueDate(loan.getLoanDate()));
        
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan returnLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        loan.setActualReturnDate(LocalDateTime.now());
        loan.setReturnDate(loan.getActualReturnDate());
        
        double fee = calculateLateFee(loan);
        loan.setLateFee(fee);

        if (fee > 0) {
            transitionStatus(loan, LoanStatus.RETURNED);
        } else {
            transitionStatus(loan, LoanStatus.CLOSED);
        }

        Resource resource = loan.getResource();
        resource.setAvailableCopies(resource.getAvailableCopies() + 1);
        resourceRepository.save(resource);
        
        reservationService.processReturn(resource);
        
        emailService.sendReturnConfirmation(
            loan.getUser().getEmail(),
            loan.getUser().getFirstName(),
            resource.getTitle()
        );

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan renew(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        validateRenewal(loan);
        
        loan.setRenewalCount(loan.getRenewalCount() + 1);
        loan.setDueDate(loan.getDueDate().plusDays(15)); 
        
        return loanRepository.save(loan);
    }

    private LocalDateTime calculateDueDate(LocalDateTime loanDate) {
        return loanDate.plusDays(15);
    }

    private Double calculateEstimatedFee(Loan loan) {
        if (loan.getDueDate() == null) {
            return 0.0;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(loan.getDueDate())) {
            long daysLate = ChronoUnit.DAYS.between(loan.getDueDate(), now);
            if (daysLate > 0) {
                double fee = daysLate * 1.0; 
                return Math.min(fee, 30.0);
            }
        }
        return 0.0;
    }

    private Double calculateLateFee(Loan loan) {
        if (loan.getDueDate() == null || loan.getActualReturnDate() == null) {
            return 0.0;
        }
        
        // If returned AFTER due date
        if (loan.getActualReturnDate().isAfter(loan.getDueDate())) {
            long daysLate = ChronoUnit.DAYS.between(loan.getDueDate(), loan.getActualReturnDate());
            if (daysLate > 0) {
                double fee = daysLate * 1.0; 
                return Math.min(fee, 30.0);
            }
        }
        return 0.0;
    }

    private void validateRenewal(Loan loan) {
        if (loan.getRenewalCount() >= 2) {
            throw new IllegalStateException("Maximum renewal limit (2) reached");
        }
        if (loan.getStatus() != LoanStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only active loans can be renewed");
        }
        if (loan.getDueDate().isBefore(LocalDateTime.now())) {
        }
    }

    private void transitionStatus(Loan loan, LoanStatus newStatus) {
        loan.setStatus(newStatus);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
    @Transactional
    public void checkOverdue() {
        List<Loan> activeLoans = loanRepository.findByStatus(LoanStatus.IN_PROGRESS);
        LocalDateTime now = LocalDateTime.now();
        
        for (Loan loan : activeLoans) {
            if (loan.getDueDate() != null && loan.getDueDate().isBefore(now)) {
                loan.setStatus(LoanStatus.OVERDUE);
                loanRepository.save(loan);
                
                emailService.sendOverdueAlert(
                    loan.getUser().getEmail(),
                    loan.getUser().getFirstName(),
                    loan.getResource().getTitle(),
                    loan.getDueDate().toString(),
                    calculateEstimatedFee(loan) // Estimated fee
                );
            }
        }
    }

    @Scheduled(cron = "0 0 9 * * ?") // Daily at 9 AM
    public void checkDueSoon() {
        List<Loan> activeLoans = loanRepository.findByStatus(LoanStatus.IN_PROGRESS);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysLater = now.plusDays(3);
        
        for (Loan loan : activeLoans) {
            if (loan.getDueDate() != null && 
                loan.getDueDate().isAfter(now) && 
                loan.getDueDate().isBefore(threeDaysLater)) {
                
                emailService.sendDueDateReminder(
                    loan.getUser().getEmail(),
                    loan.getUser().getFirstName(),
                    loan.getResource().getTitle(),
                    loan.getDueDate().toString()
                );
            }
        }
    }
}
