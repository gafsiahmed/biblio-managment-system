package com.bibliotheque.repository;

import com.bibliotheque.model.Loan;
import com.bibliotheque.model.User;
import com.bibliotheque.model.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    List<Loan> findByUser(User user);
    
    Page<Loan> findByUser(User user, Pageable pageable);
    
    List<Loan> findByStatus(LoanStatus status);
    
    List<Loan> findByStatusIn(List<LoanStatus> statuses);

    List<Loan> findByDueDateBeforeAndStatus(LocalDateTime date, LoanStatus status);

    long countByUserAndStatusIn(User user, List<LoanStatus> statuses);

    // Stats
    long countByStatus(LoanStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT l.resource, COUNT(l) as c FROM Loan l GROUP BY l.resource ORDER BY c DESC")
    List<Object[]> findTopBorrowedResources(Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT l.user, COUNT(l) as c FROM Loan l GROUP BY l.user ORDER BY c DESC")
    List<Object[]> findMostActiveUsers(Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT l.resource.category, COUNT(l) FROM Loan l GROUP BY l.resource.category")
    List<Object[]> countLoansByCategory();

    @org.springframework.data.jpa.repository.Query("SELECT l.status, COUNT(l) FROM Loan l GROUP BY l.status")
    List<Object[]> countLoansByStatus();

    @org.springframework.data.jpa.repository.Query("SELECT FUNCTION('MONTH', l.loanDate) as m, COUNT(l) FROM Loan l WHERE FUNCTION('YEAR', l.loanDate) = :year GROUP BY FUNCTION('MONTH', l.loanDate)")
    List<Object[]> countLoansByMonth(int year);

    List<Loan> findByLoanDateBetween(LocalDateTime start, LocalDateTime end);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(l.lateFee) FROM Loan l WHERE l.lateFee > 0")
    Double sumLateFees();

    @org.springframework.data.jpa.repository.Query("SELECT l.resource.category, COUNT(l) FROM Loan l WHERE l.user = :user GROUP BY l.resource.category ORDER BY COUNT(l) DESC")
    List<Object[]> findFavoriteCategoriesByUser(User user, Pageable pageable);
}
