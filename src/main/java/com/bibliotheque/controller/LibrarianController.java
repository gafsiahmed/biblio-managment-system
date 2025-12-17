package com.bibliotheque.controller;

import com.bibliotheque.model.Loan;
import com.bibliotheque.model.enums.LoanStatus;
import com.bibliotheque.model.enums.ReservationStatus;
import com.bibliotheque.repository.LoanRepository;
import com.bibliotheque.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Tag(name = "Bibliothécaire", description = "Espace de gestion pour les bibliothécaires")
public class LibrarianController {

    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;

    @Operation(summary = "Tableau de bord Bibliothécaire", description = "Affiche les tâches en attente (validations, retours, réservations)")
    @GetMapping("/librarian")
    public String dashboard(Model model) {
        model.addAttribute("section", "Espace bibliothécaire");
        
        // Loans to validate (RESERVED)
        List<Loan> toValidate = loanRepository.findByStatus(LoanStatus.RESERVED);
        model.addAttribute("toValidate", toValidate);

        // Returns expected today
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        
        List<Loan> returnsExpected = loanRepository.findByStatus(LoanStatus.IN_PROGRESS).stream()
            .filter(l -> l.getDueDate() != null && l.getDueDate().isAfter(startOfDay) && l.getDueDate().isBefore(endOfDay))
            .collect(Collectors.toList());
        model.addAttribute("returnsExpected", returnsExpected);

        model.addAttribute("pendingReservations", reservationRepository.findByStatus(ReservationStatus.PENDING));

        model.addAttribute("overdueLoans", loanRepository.findByStatus(LoanStatus.OVERDUE));

        model.addAttribute("totalLoans", loanRepository.count()); 
        model.addAttribute("activeLoansCount", loanRepository.countByStatus(LoanStatus.IN_PROGRESS));

        return "librarian/dashboard";
    }
}
