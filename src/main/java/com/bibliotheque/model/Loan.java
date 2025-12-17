package com.bibliotheque.model;

import com.bibliotheque.model.enums.LoanStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_number", unique = true, nullable = false, updatable = false)
    private String loanNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    @NotNull
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id")
    private Library library;

    @Column(name = "reservation_date")
    private LocalDateTime reservationDate;

    @Column(name = "loan_date")
    private LocalDateTime loanDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate; 

    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate;

    @Column(name = "renewal_count")
    private Integer renewalCount = 0;

    @Column(name = "late_fee")
    private Double lateFee = 0.0;

    @Column(name = "condition_before")
    private String conditionBefore;

    @Column(name = "condition_after")
    private String conditionAfter;

    @Column(name = "notes")
    private String notes;

    @Column(name = "user_feedback")
    private String userFeedback;

    @Column(name = "user_rating")
    private Integer userRating;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private LoanStatus status = LoanStatus.RESERVED;

    @PrePersist
    public void prePersist() {
        if (this.loanNumber == null) {
            this.loanNumber = UUID.randomUUID().toString();
        }
        if (this.status == null) {
            this.status = LoanStatus.RESERVED;
        }
        if (this.reservationDate == null) {
            this.reservationDate = LocalDateTime.now();
        }
    }
}
