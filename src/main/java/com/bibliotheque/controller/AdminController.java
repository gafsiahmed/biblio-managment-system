package com.bibliotheque.controller;

import com.bibliotheque.model.enums.LoanStatus;
import com.bibliotheque.model.enums.Role;
import com.bibliotheque.repository.LoanRepository;
import com.bibliotheque.repository.ResourceRepository;
import com.bibliotheque.repository.UserRepository;
import com.bibliotheque.service.ReportService;
import com.bibliotheque.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Administration", description = "Tableau de bord et gestion administrative")
public class AdminController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final ResourceRepository resourceRepository;
    private final ReportService reportService;

    @Operation(summary = "Tableau de bord Admin", description = "Affiche les statistiques globales et la liste des utilisateurs")
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("roles", Role.values());

        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalResources", resourceRepository.count());
        model.addAttribute("activeLoans", loanRepository.countByStatus(LoanStatus.IN_PROGRESS));
        model.addAttribute("revenue", loanRepository.sumLateFees() != null ? loanRepository.sumLateFees() : 0.0);

        List<Object[]> categoryStats = loanRepository.countLoansByCategory();
        StringBuilder catLabels = new StringBuilder("[");
        StringBuilder catData = new StringBuilder("[");
        for (int i = 0; i < categoryStats.size(); i++) {
            Object[] row = categoryStats.get(i);
            catLabels.append("'").append(row[0]).append("'");
            catData.append(row[1]);
            if (i < categoryStats.size() - 1) {
                catLabels.append(",");
                catData.append(",");
            }
        }
        catLabels.append("]");
        catData.append("]");
        model.addAttribute("catLabels", catLabels.toString());
        model.addAttribute("catData", catData.toString());

        // Chart Data: Loans over Time (Line)
        List<Object[]> monthStats = loanRepository.countLoansByMonth(java.time.LocalDate.now().getYear());
        long[] monthlyData = new long[12];
        for (Object[] row : monthStats) {
            int month = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();
            if (month >= 1 && month <= 12) {
                monthlyData[month - 1] = count;
            }
        }
        model.addAttribute("monthlyData", java.util.Arrays.toString(monthlyData));

        List<Object[]> statusStats = loanRepository.countLoansByStatus();
        StringBuilder statusLabels = new StringBuilder("[");
        StringBuilder statusData = new StringBuilder("[");
        for (int i = 0; i < statusStats.size(); i++) {
            Object[] row = statusStats.get(i);
            statusLabels.append("'").append(row[0]).append("'");
            statusData.append(row[1]);
            if (i < statusStats.size() - 1) {
                statusLabels.append(",");
                statusData.append(",");
            }
        }
        statusLabels.append("]");
        statusData.append("]");
        model.addAttribute("statusLabels", statusLabels.toString());
        model.addAttribute("statusData", statusData.toString());

        // Top Resources
        List<Object[]> topResources = loanRepository.findTopBorrowedResources(PageRequest.of(0, 5));
        model.addAttribute("topResources", topResources);

        return "admin/dashboard";
    }

    @Operation(summary = "Changer le rôle d'un utilisateur", description = "Modifie les droits d'accès d'un utilisateur")
    @PostMapping("/users/{id}/role")
    public String changeRole(@Parameter(description = "ID de l'utilisateur") @PathVariable Long id, 
                             @Parameter(description = "Nouveau rôle") @RequestParam Role role) {
        userService.findById(id).ifPresent(u -> {
            u.setRole(role);
            userService.save(u);
        });
        return "redirect:/admin";
    }

    @Operation(summary = "Export CSV des prêts", description = "Télécharge l'historique des prêts au format CSV")
    @GetMapping("/reports/loans/csv")
    public void exportLoansCsv(HttpServletResponse response) throws IOException {
        reportService.exportLoansToCSV(response, LocalDateTime.now().minusYears(1), LocalDateTime.now());
    }

    @Operation(summary = "Export PDF des prêts", description = "Télécharge l'historique des prêts au format PDF")
    @GetMapping("/reports/loans/pdf")
    public void exportLoansPdf(HttpServletResponse response) throws IOException {
        reportService.exportLoansToPDF(response, LocalDateTime.now().minusYears(1), LocalDateTime.now());
    }

    @Operation(summary = "Export CSV activité utilisateurs", description = "Télécharge l'activité des utilisateurs au format CSV")
    @GetMapping("/reports/users/csv")
    public void exportUsersCsv(HttpServletResponse response) throws IOException {
        reportService.exportUserActivityToCSV(response);
    }

    @Operation(summary = "Export CSV usage ressources", description = "Télécharge les statistiques d'usage des ressources au format CSV")
    @GetMapping("/reports/resources/csv")
    public void exportResourcesCsv(HttpServletResponse response) throws IOException {
        reportService.exportResourceUsageToCSV(response);
    }
}
