package com.bibliotheque.controller;

import com.bibliotheque.dto.ChangePasswordDto;
import com.bibliotheque.dto.UserProfileDto;
import com.bibliotheque.model.Loan;
import com.bibliotheque.model.Reservation;
import com.bibliotheque.model.User;
import com.bibliotheque.model.enums.Category;
import com.bibliotheque.model.enums.LoanStatus;
import com.bibliotheque.model.enums.ReservationStatus;
import com.bibliotheque.repository.LoanRepository;
import com.bibliotheque.repository.ReservationRepository;
import com.bibliotheque.repository.ResourceRepository;
import com.bibliotheque.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "Profil", description = "Gestion du profil utilisateur")
public class ProfileController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;

    @GetMapping
    public String index() {
        return "redirect:/user/dashboard";
    }

    @Operation(summary = "Tableau de bord Utilisateur", description = "Affiche les prêts en cours, réservations et historique")
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);

        List<Loan> activeLoans = loanRepository.findByUser(user).stream()
            .filter(l -> l.getStatus() == LoanStatus.IN_PROGRESS || l.getStatus() == LoanStatus.OVERDUE)
            .collect(Collectors.toList());
        model.addAttribute("activeLoans", activeLoans);

        List<Reservation> reservations = reservationRepository.findByUser(user).stream()
             .filter(r -> r.getStatus() == ReservationStatus.PENDING || r.getStatus() == ReservationStatus.APPROVED)
             .collect(Collectors.toList());
        model.addAttribute("reservations", reservations);

        List<Loan> history = loanRepository.findByUser(user).stream()
             .filter(l -> l.getStatus() == LoanStatus.RETURNED || l.getStatus() == LoanStatus.CLOSED)
             .sorted((a, b) -> {
                 if (b.getReturnDate() == null) return -1;
                 if (a.getReturnDate() == null) return 1;
                 return b.getReturnDate().compareTo(a.getReturnDate());
             })
             .limit(5)
             .collect(Collectors.toList());
        model.addAttribute("history", history);
        
        long totalLoans = loanRepository.findByUser(user).size();
        long overdueCount = loanRepository.findByUser(user).stream()
                .filter(l -> l.getStatus() == LoanStatus.OVERDUE).count();
        model.addAttribute("totalLoans", totalLoans);
        model.addAttribute("overdueCount", overdueCount);

        List<Object[]> favoriteCategories = loanRepository.findFavoriteCategoriesByUser(user, PageRequest.of(0, 1));
        if (!favoriteCategories.isEmpty()) {
            Category favoriteCategory = (Category) favoriteCategories.get(0)[0];
            model.addAttribute("suggestions", resourceRepository.findByCategory(favoriteCategory, PageRequest.of(0, 3)).getContent());
        } else {
            model.addAttribute("suggestions", resourceRepository.findAll().stream().limit(3).collect(Collectors.toList()));
        }

        return "user/dashboard";
    }

    @Operation(summary = "Formulaire profil", description = "Affiche le formulaire de modification du profil")
    @GetMapping("/profile")
    public String profileForm(Model model) {
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserProfileDto dto = new UserProfileDto();
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setDateOfBirth(user.getDateOfBirth());
        
        model.addAttribute("profile", dto);
        return "user/profile";
    }

    @Operation(summary = "Mettre à jour le profil", description = "Enregistre les modifications du profil utilisateur")
    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("profile") UserProfileDto dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "user/profile";
        }

        User user = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setDateOfBirth(dto.getDateOfBirth());

        userService.updateProfile(user);
        return "redirect:/user/dashboard";
    }

    @Operation(summary = "Formulaire mot de passe", description = "Affiche le formulaire de changement de mot de passe")
    @GetMapping("/password")
    public String passwordForm(Model model) {
        model.addAttribute("passwordForm", new ChangePasswordDto());
        return "user/password";
    }

    @Operation(summary = "Changer le mot de passe", description = "Met à jour le mot de passe de l'utilisateur")
    @PostMapping("/password")
    public String updatePassword(@Valid @ModelAttribute("passwordForm") ChangePasswordDto form, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "user/password";
        }

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.passwordForm", "Les mots de passe ne correspondent pas");
            return "user/password";
        }

        User user = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
            result.rejectValue("currentPassword", "error.passwordForm", "Mot de passe actuel incorrect");
            return "user/password";
        }

        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userService.save(user);

        return "redirect:/user/dashboard?passwordChanged";
    }
}
