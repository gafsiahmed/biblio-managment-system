package com.bibliotheque.controller;

import com.bibliotheque.model.Loan;
import com.bibliotheque.model.Reservation;
import com.bibliotheque.model.Resource;
import com.bibliotheque.model.User;
import com.bibliotheque.service.LoanService;
import com.bibliotheque.service.ReservationService;
import com.bibliotheque.service.ResourceService;
import com.bibliotheque.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;

@Controller
@RequestMapping("/loans")
@RequiredArgsConstructor
@Tag(name = "Prêts", description = "Gestion des emprunts et réservations")
public class LoanController {

    private final LoanService loanService;
    private final UserService userService;
    private final ReservationService reservationService;
    private final ResourceService resourceService;

    @Operation(summary = "Réserver une ressource", description = "Crée une demande de prêt ou une réservation si indisponible")
    @PostMapping("/reserve/{resourceId}")
    public String reserve(@Parameter(description = "ID de la ressource") @PathVariable Long resourceId, RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser().orElseThrow(() -> new RuntimeException("User not found"));
        try {
            Resource resource = resourceService.find(resourceId)
                    .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

            if (resource.getAvailableCopies() > 0) {
                loanService.reserve(user, resourceId);
                redirectAttributes.addFlashAttribute("success", "Réservation confirmée (Prêt créé) !");
            } else {
                Reservation reservation = reservationService.createReservation(user, resource);
                redirectAttributes.addFlashAttribute("success", "Ressource indisponible. Vous êtes en file d'attente (Position: " + reservation.getPositionInQueue() + ")");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Echec : " + e.getMessage());
        }
        return "redirect:/loans/my-loans";
    }

    @Operation(summary = "Valider un prêt", description = "Approuve une demande de prêt (Bibliothécaire/Admin)")
    @PostMapping("/{id}/approve")
    public String approve(@Parameter(description = "ID du prêt") @PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            loanService.approve(id);
            redirectAttributes.addFlashAttribute("success", "Loan approved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error approving loan: " + e.getMessage());
        }
        return "redirect:/loans/pending";
    }

    @Operation(summary = "Retourner un prêt", description = "Marque un prêt comme retourné")
    @PostMapping("/{id}/return")
    public String returnLoan(@Parameter(description = "ID du prêt") @PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            loanService.returnLoan(id);
            redirectAttributes.addFlashAttribute("success", "Loan returned successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error returning loan: " + e.getMessage());
        }
        return "redirect:/loans/my-loans"; // Or redirect to referrer if possible?
    }

    @Operation(summary = "Renouveler un prêt", description = "Prolonge la durée d'un prêt")
    @PostMapping("/{id}/renew")
    public String renew(@Parameter(description = "ID du prêt") @PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            loanService.renew(id);
            redirectAttributes.addFlashAttribute("success", "Loan renewed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Renewal failed: " + e.getMessage());
        }
        return "redirect:/loans/my-loans";
    }

    @Operation(summary = "Mes prêts", description = "Affiche la liste des prêts de l'utilisateur connecté")
    @GetMapping("/my-loans")
    public String myLoans(Model model) {
        User user = userService.getCurrentUser().orElseThrow(() -> new RuntimeException("User not found"));
        List<Loan> loans = loanService.findByUser(user);
        model.addAttribute("loans", loans);
        return "loans/my-loans";
    }

    @Operation(summary = "Prêts en attente", description = "Liste les prêts en attente de validation (Admin/Bibliothécaire)")
    @GetMapping("/pending")
    public String pendingLoans(Model model) {
        List<Loan> loans = loanService.findPendingLoans();
        model.addAttribute("loans", loans);
        return "loans/pending";
    }

    @Operation(summary = "Prêts en retard", description = "Liste les prêts en retard (Admin/Bibliothécaire)")
    @GetMapping("/overdue")
    public String overdueLoans(Model model) {
        List<Loan> loans = loanService.findOverdueLoans();
        model.addAttribute("loans", loans);
        return "loans/overdue";
    }

    @Operation(summary = "Tous les prêts", description = "Liste historique de tous les prêts (Admin/Bibliothécaire)")
    @GetMapping("/all")
    public String allLoans(Model model) {
        List<Loan> loans = loanService.findAll();
        model.addAttribute("loans", loans);
        return "loans/list";
    }

    @Operation(summary = "Editer un prêt", description = "Formulaire d'édition d'un prêt (Admin/Bibliothécaire)")
    @GetMapping("/{id}/edit")
    public String editLoan(@Parameter(description = "ID du prêt") @PathVariable Long id, Model model) {
        Loan loan = loanService.findById(id).orElseThrow(() -> new IllegalArgumentException("Loan not found"));
        model.addAttribute("loan", loan);
        model.addAttribute("statuses", com.bibliotheque.model.enums.LoanStatus.values());
        return "loans/edit";
    }

    @Operation(summary = "Mettre à jour un prêt", description = "Enregistre les modifications d'un prêt (Admin/Bibliothécaire)")
    @PostMapping("/{id}/update")
    public String updateLoan(@Parameter(description = "ID du prêt") @PathVariable Long id, @ModelAttribute Loan loan, RedirectAttributes redirectAttributes) {
        try {
            loanService.updateLoan(id, loan);
            redirectAttributes.addFlashAttribute("success", "Loan updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating loan: " + e.getMessage());
        }
        return "redirect:/loans/all";
    }
}
