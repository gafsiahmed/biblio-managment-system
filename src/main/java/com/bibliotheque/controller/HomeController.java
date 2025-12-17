package com.bibliotheque.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Tag(name = "Accueil", description = "Page d'accueil de l'application")
public class HomeController {
  @Operation(summary = "Page d'accueil", description = "Affiche la page principale avec le statut de connexion")
  @GetMapping("/")
  public String home(Model model) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean authenticated = auth != null && auth.isAuthenticated() && !("anonymousUser".equals(String.valueOf(auth.getPrincipal())));
    String username = authenticated ? auth.getName() : null;
    model.addAttribute("authenticated", authenticated);
    model.addAttribute("username", username);
    return "home";
  }
}
