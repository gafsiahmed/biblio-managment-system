package com.bibliotheque.controller;

import com.bibliotheque.dto.RegisterRequest;
import com.bibliotheque.model.User;
import com.bibliotheque.model.enums.Role;
import com.bibliotheque.repository.UserRepository;
import com.bibliotheque.service.MailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;

@Controller
@Tag(name = "Authentication", description = "Endpoints d'authentification et d'inscription")
public class AuthController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final MailService mailService;

  public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, MailService mailService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.mailService = mailService;
  }

  @Operation(summary = "Page de connexion", description = "Affiche le formulaire de connexion")
  @GetMapping("/login")
  public String loginPage() {
    org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
    boolean authenticated = auth != null && auth.isAuthenticated() && !("anonymousUser".equals(String.valueOf(auth.getPrincipal())));
    if (authenticated) {
      return "redirect:/";
    }
    return "login";
  }

  @GetMapping("/register")
  public String registerPage(Model model) {
    org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
    boolean authenticated = auth != null && auth.isAuthenticated() && !("anonymousUser".equals(String.valueOf(auth.getPrincipal())));
    if (authenticated) {
      return "redirect:/";
    }
    model.addAttribute("register", new RegisterRequest());
    return "register";
  }

  @Operation(summary = "Inscription utilisateur", description = "Traite le formulaire d'inscription")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Inscription réussie ou erreur de validation"),
      @ApiResponse(responseCode = "302", description = "Redirection après succès")
  })
  @PostMapping("/register")
  public String registerSubmit(@Valid @ModelAttribute("register") RegisterRequest req, BindingResult result, Model model) {
    if (result.hasErrors()) {
      return "register";
    }
    if (userRepository.existsByEmail(req.getEmail())) {
      model.addAttribute("error", "Email déjà utilisé");
      return "register";
    }
    if (userRepository.findByUsername(req.getUsername()).isPresent()) {
      model.addAttribute("error", "Nom d'utilisateur déjà utilisé");
      return "register";
    }
    User u = new User();
    u.setUsername(req.getUsername());
    u.setEmail(req.getEmail());
    u.setFirstName(req.getFirstName());
    u.setLastName(req.getLastName());
    u.setPassword(passwordEncoder.encode(req.getPassword()));
    u.setRole(Role.ROLE_USER);
    u.setEmailVerified(false);
    String token = UUID.randomUUID().toString();
    u.setVerificationToken(token);
    userRepository.save(u);
    mailService.sendVerificationEmail(u, token);
    return "redirect:/login?registered";
  }

  @Operation(summary = "Vérification email", description = "Valide le token de vérification d'email")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "302", description = "Redirection vers login avec message de succès")
  })
  @GetMapping("/verify-email")
  public String verifyEmail(@RequestParam("token") String token) {
    return userRepository.findByVerificationToken(token)
      .map(u -> {
        u.setEmailVerified(true);
        u.setVerificationToken(null);
        userRepository.save(u);
        return "redirect:/login?verified";
      })
      .orElse("redirect:/login?invalidToken");
  }

  @Operation(summary = "Page accès refusé", description = "Affiche la page d'erreur 403")
  @GetMapping("/access-denied")
  public String accessDenied() {
    return "access-denied";
  }

  @Operation(summary = "Déconnexion", description = "Déconnecte l'utilisateur")
  @GetMapping("/logout")
  public String logoutGet() {
    return "redirect:/";
  }
}
